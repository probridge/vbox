package com.probridge.vbox.actions;

import java.io.UnsupportedEncodingException;
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
import com.probridge.vbox.dao.VboxRequestMapper;
import com.probridge.vbox.model.Users;
import com.probridge.vbox.model.VboxRequest;
import com.probridge.vbox.model.VboxRequestExample;
import com.probridge.vbox.utils.CourseUtils;
import com.probridge.vbox.utils.Utility;

public class ApplyAccessAction implements Action {
	public static final String SUCCESS = "applyaccess";
	public static final String APPLYPLACED = "applyplaced";
	public static final String INVALID = "home";
	//
	private static final Logger logger = LoggerFactory.getLogger(ApplyAccessAction.class);

	public String execute(HttpServletRequest request, HttpServletResponse response) {
		String identity = Utility.getStringVal(request.getAttribute("identity"));
		//
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		VboxRequestMapper mapper = session.getMapper(VboxRequestMapper.class);
		VboxRequestExample exp = new VboxRequestExample();
		exp.createCriteria().andVboxRequestOwnerEqualTo(identity).andVboxRequestStatusEqualTo("0");
		List<VboxRequest> previousRequests = mapper.selectByExample(exp);

		UsersMapper um = session.getMapper(UsersMapper.class);
		Users me = um.selectByPrimaryKey(identity);
		//
		session.close();
		//
		String vhdName = me.getUserVhdName();
		logger.debug("VHD:" + vhdName);
		logger.debug("Previous Requests:" + previousRequests.size());
		//
		if ("GET".equals(request.getMethod())) {
			request.setAttribute("firstTimeUser", isFirstTimeUser(me, previousRequests));
			request.setAttribute("TooManyRequests", previousRequests.size() >= VBoxConfig.CourseRequestPerUserLimit);
			request.setAttribute("RequestedQuota", "1");
			//
			return SUCCESS;
		} else if ("POST".equals(request.getMethod())) {
			Session sess = SecurityUtils.getSubject().getSession();
			// do basic checking and save to session and redirect
			try {
				request.setCharacterEncoding("utf-8");
			} catch (UnsupportedEncodingException e) {
			}
			String requestedQuota = request.getParameter("RequestedQuota");
			String courseCode = request.getParameter("CourseCode");
			String justification = request.getParameter("Justification");

			logger.debug("RequestedQuota: " + requestedQuota);
			logger.debug("Justification: " + justification);
			logger.debug("CourseCode: " + courseCode);
			logger.debug("FirstTimeUser: " + isFirstTimeUser(me, previousRequests));

			sess.setAttribute("FirstTimeUser", "true");
			if (!isFirstTimeUser(me, previousRequests)) {
				sess.setAttribute("FirstTimeUser", "false");
				if (Utility.isEmptyOrNull(courseCode)) {
					request.setAttribute("errorMsg", "请输入课程代码");
					request.setAttribute("firstTimeUser", isFirstTimeUser(me, previousRequests));
					request.setAttribute("TooManyRequests", previousRequests.size() >= VBoxConfig.CourseRequestPerUserLimit);
					request.setAttribute("RequestedQuota", requestedQuota);
					request.setAttribute("CourseCode", courseCode);
					request.setAttribute("Justification", justification);
					return SUCCESS;
				}
			}

			if (!Utility.isEmptyOrNull(courseCode) && !CourseUtils.isCourseValid(courseCode)) {
				request.setAttribute("errorMsg", "课程代码无效");
				request.setAttribute("firstTimeUser", isFirstTimeUser(me, previousRequests));
				request.setAttribute("TooManyRequests", previousRequests.size() >= VBoxConfig.CourseRequestPerUserLimit);
				request.setAttribute("RequestedQuota", requestedQuota);
				request.setAttribute("CourseCode", courseCode);
				request.setAttribute("Justification", justification);
				return SUCCESS;
			}
			//
			if (!Utility.isEmptyOrNull(courseCode) && CourseUtils.isCourseAlreadyApplied(identity, courseCode)) {
				request.setAttribute("errorMsg", "请不要重复申请");
				request.setAttribute("firstTimeUser", isFirstTimeUser(me, previousRequests));
				request.setAttribute("TooManyRequests", previousRequests.size() >= VBoxConfig.CourseRequestPerUserLimit);
				request.setAttribute("RequestedQuota", requestedQuota);
				request.setAttribute("CourseCode", courseCode);
				request.setAttribute("Justification", justification);
				return SUCCESS;
			}
			//
			sess.setAttribute("RequestedQuota", requestedQuota);
			sess.setAttribute("CourseCode", courseCode);
			sess.setAttribute("Justification", justification);
			sess.setAttribute("Ticket","OK");
			return APPLYPLACED;
		}
		return INVALID;
	}

	private boolean isFirstTimeUser(Users u, List<VboxRequest> r) {
		return Utility.isEmptyOrNull(u.getUserVhdName()) && u.getUserVhdQuota()==null && r.size() == 0;
	}
}
