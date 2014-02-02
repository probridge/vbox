package com.probridge.vbox.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.UsersMapper;
import com.probridge.vbox.dao.VMMapper;
import com.probridge.vbox.dao.VboxRequestMapper;
import com.probridge.vbox.model.Users;
import com.probridge.vbox.model.VM;
import com.probridge.vbox.model.VMExample;
import com.probridge.vbox.model.VboxRequest;
import com.probridge.vbox.model.VboxRequestExample;
import com.probridge.vbox.utils.Utility;
import com.probridge.vbox.vmm.VMSessionScheduler;
import com.probridge.vbox.vmm.wmi.VirtualMachine.VMState;

public class LandingAction implements Action {
	public static final String SUCCEED = "home";
	public static final String EXPIRED = "expired";
	public static final String DISABLED = "problem";

	private static final Logger logger = LoggerFactory.getLogger(LandingAction.class);

	public String execute(HttpServletRequest request, HttpServletResponse response) {
		Session sess = SecurityUtils.getSubject().getSession();
		String identity = Utility.getStringVal(request.getAttribute("identity"));
		//
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		// Let's check user validation first
		UsersMapper umapper = session.getMapper(UsersMapper.class);
		Users thisUser = umapper.selectByPrimaryKey(identity);
		// Check disabled
		if (!"1".equals(thisUser.getUserEnabled())) {
			// user disabled
			return DISABLED;
		}
		//
		if (thisUser.getUserExpiration() != null && thisUser.getUserExpiration().before(new Date())) {
			// user expired - disable
			return DISABLED;
		}
		//
		if (thisUser.getUserPwdExpire() != null && thisUser.getUserPwdExpire().before(new Date())) {
			// user pwd expired - force changing password
			return EXPIRED;
		}
		//
		VMMapper mapper = session.getMapper(VMMapper.class);
		VMExample exp = new VMExample();
		exp.createCriteria().andVmOwnerEqualTo(identity);
		List<VM> myVBoxList = mapper.selectByExample(exp);
		session.close();
		if (myVBoxList != null)
			logger.debug("Got " + myVBoxList.size() + " VMs for user " + identity);
		else
			logger.debug("Got ZERO VM for user " + identity);
		//
		for (VM eachVM : myVBoxList)
			logger.debug(eachVM.toString());

		// select the last running VM or persoal vm if running
		VM selectedVM = null;
		VM personalVM = null;
		VM preferredVM = null;

		VMSessionScheduler scheduler = VMSessionScheduler.getInstance();
		String sid = sess.getId().toString();

		for (int i = 0; i < myVBoxList.size(); i++) {
			VM thisVM = myVBoxList.get(i);
			logger.debug("registering <" + thisVM.getVmId() + "," + sid + "> to monitor list");
			scheduler.register(sid, thisVM.getVmId());
		}
		//
		for (int i = 0; i < myVBoxList.size(); i++) {
			VM thisVM = myVBoxList.get(i);
			// Fail safe preferred to 1st non course VM
			if ("0".equals(thisVM.getVmType()) && preferredVM == null)
				preferredVM = thisVM;
			// Look for preferred flag
			if ("0".equals(thisVM.getVmType()) && !Utility.isEmptyOrNull(thisVM.getVmPreferredCourse()))
				preferredVM = thisVM;
			// This is personal VM
			if ("1".equals(thisVM.getVmType()))
				personalVM = thisVM;
			//
			if (VMState.Running.getValue() == thisVM.getVmStatus()
					|| VMState.Suspended.getValue() == thisVM.getVmStatus())
				selectedVM = thisVM;
		}
		//
		ArrayList<VM> extendedList = new ArrayList<VM>();
		extendedList.addAll(myVBoxList);
		extendedList.remove(personalVM);
		extendedList.remove(preferredVM);
		//
		sess.setAttribute("vboxlist", myVBoxList);
		sess.setAttribute("extended", extendedList);
		sess.setAttribute("preferred", preferredVM);
		sess.setAttribute("personal", personalVM);
		//
		if (selectedVM != null) {
			sess.setAttribute("selected", selectedVM);
			logger.debug("selectVM set to a running VM: " + selectedVM);
		} else {
			sess.setAttribute("selected", personalVM); // fallback
			logger.debug("selectVM falled back to a stopped personal VM: " + personalVM);
		}
		//
		String outputMsg = "";
		String message = Utility.getStringVal(sess.getAttribute(LANDING_MSG));
		if (!Utility.isEmptyOrNull(message)) {
			// high priority
			outputMsg += "<p>" + message + "</p>";
			sess.removeAttribute(LANDING_MSG);
		} else {
			// check other messages: user approval / vbox approval
			session = VBoxConfig.sqlSessionFactory.openSession();
			VboxRequestMapper vrmapper = session.getMapper(VboxRequestMapper.class);
			VboxRequestExample vrexp = new VboxRequestExample();
			vrexp.or().andVboxRequestOwnerEqualTo(identity).andVboxRequestStatusEqualTo("1");
			vrexp.or().andVboxRequestOwnerEqualTo(identity).andVboxRequestStatusEqualTo("2");
			List<VboxRequest> reqs = vrmapper.selectByExample(vrexp);
			if (reqs != null) {
				for (VboxRequest eachvr : reqs)
					if (eachvr.getVboxRequestApprovalInfo() != null
							&& !eachvr.getVboxRequestApprovalInfo().startsWith(".")) {
						String result = "异常";
						if ("1".equals(eachvr.getVboxRequestStatus()))
							result = "通过";
						if ("2".equals(eachvr.getVboxRequestStatus()))
							result = "失败";
						outputMsg += "<p>课程申请审核" + result + "：" + eachvr.getVboxRequestApprovalInfo() + "</p>";
						eachvr.setVboxRequestApprovalInfo("." + eachvr.getVboxRequestApprovalInfo());
						vrmapper.updateByPrimaryKey(eachvr);
					}
				session.commit();
			}
			//
			if (thisUser.getUserVhdQuota() != null && thisUser.getUserVhdQuota() == 0) {
				outputMsg += "<p>您的个人空间申请没有得到批准，如有问题请联系我们。</p>";
				umapper = session.getMapper(UsersMapper.class);
				thisUser.setUserVhdQuota(null);
				umapper.updateByPrimaryKey(thisUser);
				session.commit();
			}
			session.close();
		}
		//
		if (!Utility.isEmptyOrNull(outputMsg))
			sess.setAttribute("home_notify_info", outputMsg);
		//
		sess.setAttribute("landed", "yes");
		return SUCCEED;
	}
}
