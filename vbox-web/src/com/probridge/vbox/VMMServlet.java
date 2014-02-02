package com.probridge.vbox;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.codec.Hex;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.dao.VMMapper;
import com.probridge.vbox.model.VM;
import com.probridge.vbox.servlet.FixVboxTask;
import com.probridge.vbox.servlet.OpStatus;
import com.probridge.vbox.servlet.PreAppoveTask;
import com.probridge.vbox.servlet.VMSwitchTask;
import com.probridge.vbox.utils.Utility;
import com.probridge.vbox.vmm.wmi.VirtualMachine.VMState;
import com.probridge.vbox.zk.AdminTaskManager;

public class VMMServlet extends HttpServlet {

	private static final long serialVersionUID = -1830587443062161958L;
	private static final Logger logger = LoggerFactory.getLogger(VMMServlet.class);
	private static final AdminTaskManager taskManager = AdminTaskManager.getInstance();

	//
	public VMMServlet() {
		logger.debug("Initializing vmm-servlet");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		request.setCharacterEncoding("utf-8");
		String fn = request.getParameter("fn");
		//
		if (fn == null || fn.trim().length() == 0) {
			response.sendError(400);
			return;
		}
		fn = fn.toLowerCase();
		//
		if ("ping".equals(fn)) {
			try {
				JSONObject json = new JSONObject();
				json.put("pong", "ok");
				response.setContentType("application/json");
				response.setCharacterEncoding("utf-8");
				PrintWriter writer = response.getWriter();
				writer.write(json.toString());
				writer.close();
			} catch (JSONException e) {
			}
		} else if ("getstatus".equals(fn)) {
			try {
				String uuid = request.getParameter("uuid");
				logger.trace("retreive status for: " + uuid);
				SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
				VMMapper mapper = session.getMapper(VMMapper.class);
				VM selectedVM = mapper.selectByPrimaryKey(uuid);
				session.close();

				if (selectedVM != null) {
					JSONObject json = new JSONObject();
					json.put("uuid", selectedVM.getVmId());
					json.put("hb", selectedVM.getVmHeartbeat());
					json.put("state", VMState.getItem(selectedVM.getVmStatus()).getName());
					json.put("ip", selectedVM.getVmIpAddress());
					json.put("pwd", Utility.isEmptyOrNull(selectedVM.getVmGuestPassword()) ? "no" : "yes");
					//
					if ("Y".equals(selectedVM.getVmInitFlag()))
						json.put("init", "yes");
					else
						json.put("init", "no");
					//
					response.setContentType("application/json");
					response.setCharacterEncoding("utf-8");
					PrintWriter writer = response.getWriter();
					writer.write(json.toString());
					logger.trace(json.toString());
					writer.close();
				} else
					response.sendError(404);
			} catch (JSONException e) {
				logger.error("error getting vm status", e);
				response.sendError(500);
			}
		} else if ("markfav".equals(fn)) {
			String uuid = request.getParameter("uuid");
			String unmark = request.getParameter("unmark");
			logger.trace("setting favorite for: " + uuid + " removing: " + unmark);
			SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
			VMMapper mapper = session.getMapper(VMMapper.class);
			VM setVM = mapper.selectByPrimaryKey(uuid);
			VM unsetVM = mapper.selectByPrimaryKey(unmark);
			if (setVM != null) {
				setVM.setVmPreferredCourse("Y");
				mapper.updateByPrimaryKey(setVM);
			}
			if (unsetVM != null) {
				unsetVM.setVmPreferredCourse("");
				mapper.updateByPrimaryKey(unsetVM);
			}
			session.commit();
			session.close();
			JSONObject json = new JSONObject();
			try {
				json.put("result", "ok");
			} catch (JSONException e) {
			}
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			PrintWriter writer = response.getWriter();
			writer.write(json.toString());
			logger.trace(json.toString());
			writer.close();
		} else if ("getopmsg".equals(fn)) {
			try {
				String opid = request.getParameter("opid");
				logger.debug("getting op result for opid=" + opid);
				OpStatus s = taskManager.queryStatus(opid);
				response.setContentType("application/json");
				response.setCharacterEncoding("utf-8");
				PrintWriter writer = response.getWriter();
				JSONObject json = new JSONObject();
				if (s == null) {
					logger.debug("no operation found for opid=" + opid);
					json.put("opid", opid);
					json.put("rv", 2);
					json.put("msg", Hex.encodeToString("请稍等…".getBytes("utf-8")));
				} else {
					logger.debug("got op for opid" + opid + s.toString());
					json.put("opid", opid);
					json.put("rv", s.getRetval());
					json.put("msg", Hex.encodeToString(s.getMsg().getBytes("utf-8")));
				}
				//
				writer.write(json.toString());
				writer.close();
			} catch (JSONException e) {
				logger.error("error getting op msg", e);
				response.sendError(500);
			}
		} else if ("fixvbox".equals(fn)) {
			String sid = SecurityUtils.getSubject().getSession().getId().toString();
			logger.debug("Processing FIXVBOX for SID " + sid);
			String uuid = request.getParameter("uuid");
			String opid = String.valueOf(System.currentTimeMillis());

			FixVboxTask t = new FixVboxTask(sid, opid, uuid);
			logger.debug("Submit task for fixvbox for SID " + sid);

			if (taskManager.submit(t) == null) {
				logger.warn("Found ongoing task for SID " + sid + ", quiting");
				response.sendError(503);
			} else {
				JSONObject json = new JSONObject();
				try {
					json.put("opid", opid);
					json.put("init", "yes");
				} catch (JSONException e) {
				}
				response.setContentType("application/json");
				response.setCharacterEncoding("utf-8");
				PrintWriter writer = response.getWriter();
				writer.write(json.toString());
				writer.close();
			}
		} else if ("preapprove".equals(fn)) {
			String sid = SecurityUtils.getSubject().getSession().getId().toString();
			logger.debug("Processing PREAPPROVE for SID " + sid);
			String key = request.getParameter("cmdkey");
			String opid = String.valueOf(System.currentTimeMillis());

			Object obj = SecurityUtils.getSubject().getSession().getAttribute(key);
			if (obj != null && obj instanceof HashMap) {
				@SuppressWarnings("unchecked")
				HashMap<String, Object> cmdMap = (HashMap<String, Object>) obj;

				PreAppoveTask t = new PreAppoveTask(sid, opid, cmdMap);
				logger.debug("Submit task for preapporve for SID " + sid);
				if (taskManager.submit(t) == null) {
					logger.warn("Found ongoing task for SID " + sid + ", quiting");
					response.sendError(503);
				} else {
					JSONObject json = new JSONObject();
					try {
						json.put("opid", opid);
					} catch (JSONException e) {
					}
					response.setContentType("application/json");
					response.setCharacterEncoding("utf-8");
					PrintWriter writer = response.getWriter();
					writer.write(json.toString());
					writer.close();
				}
			}
		} else if ("switchvm".equals(fn)) {
			String sid = SecurityUtils.getSubject().getSession().getId().toString();
			logger.debug("Processing SWITCHVM for SID " + sid);
			String res = request.getParameter("resume");
			String sus = request.getParameter("suspend");
			String opid = String.valueOf(System.currentTimeMillis());
			VMSwitchTask t = new VMSwitchTask(sid, opid, toArrayList(res), toArrayList(sus));
			logger.debug("Submit task for switchvm for SID " + sid + " resume:" + res + ", suspend:" + sus);
			if (taskManager.submit(t) == null) {
				logger.warn("Found ongoing task for SID " + sid + ", quiting");
				response.sendError(503);
			} else {
				SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
				VMMapper mapper = session.getMapper(VMMapper.class);
				VM selected = mapper.selectByPrimaryKey(res);
				//
				JSONObject json = new JSONObject();
				try {
					json.put("opid", opid);
					if ("Y".equals(selected.getVmInitFlag()))
						json.put("init", "yes");
					else
						json.put("init", "no");
				} catch (JSONException e) {
				}
				session.close();
				//
				response.setContentType("application/json");
				response.setCharacterEncoding("utf-8");
				PrintWriter writer = response.getWriter();
				writer.write(json.toString());
				writer.close();
				// Update current selected VM
				SecurityUtils.getSubject().getSession().setAttribute("selected", selected);
			}
		}
	}

	private ArrayList<String> toArrayList(String str) {
		ArrayList<String> ret = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(str, ",");
		while (st.hasMoreTokens())
			ret.add(st.nextToken());
		return ret;
	}
}
