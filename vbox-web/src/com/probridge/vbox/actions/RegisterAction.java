package com.probridge.vbox.actions;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.UsersMapper;
import com.probridge.vbox.model.Users;
import com.probridge.vbox.utils.Utility;

public class RegisterAction implements Action {
	public static final String PROCEED = "register";
	public static final String SUCCEED = "landing";
	private static final Logger logger = LoggerFactory.getLogger(RegisterAction.class);

	public String execute(HttpServletRequest request, HttpServletResponse response) {
		if ("GET".equals(request.getMethod())) {
			logger.info("New user registration");
			return PROCEED;
		} else if ("POST".equals(request.getMethod())) {
			String email = request.getParameter("inputEmail");
			//
			String p1 = request.getParameter("inputPassword1");
			String p2 = request.getParameter("inputPassword2");
			//
			if (Utility.isEmptyOrNull(p1) || Utility.isEmptyOrNull(p2)) {
				request.setAttribute("error", "密码不得为空。");
				return PROCEED;
			}

			if (email.toLowerCase().endsWith(VBoxConfig.jAccountSuffix)) {
				request.setAttribute("error", "校内用户请使用统一认证平台直接登录。");
				return PROCEED;
			}

			if (!p1.equals(p2)) {
				request.setAttribute("error", "两次密码输入不同，请重新输入。");
				return PROCEED;
			}
			//
			SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
			UsersMapper umapper = session.getMapper(UsersMapper.class);
			Users thisUser = umapper.selectByPrimaryKey(email);
			//
			if (thisUser != null) {
				session.close();
				request.setAttribute("error", "用户名已经存在。");
				return PROCEED;
			}
			thisUser = new Users();
			thisUser.setUserName(email);
			thisUser.setUserPassword(p1);
			thisUser.setUserEnabled("1");
			thisUser.setUserType("0");
			thisUser.setUserRole("ROLE_USER");
			//
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.MONTH, 3);
			thisUser.setUserPwdExpire(cal.getTime());
			thisUser.setUserHypervisorId(null);
			//
			umapper.insert(thisUser);
			session.commit();
			session.close();
			//
			UsernamePasswordToken loginToken = new UsernamePasswordToken(email, p1);
			loginToken.setRememberMe(true);
			Subject currentUser = SecurityUtils.getSubject();
			try {
				currentUser.login(loginToken);
			} catch (Exception e) {
				request.setAttribute("error", "登录发生错误，请联系我们。" + e.getMessage());
				return PROCEED;
			}
			//
			Session sess = SecurityUtils.getSubject().getSession();
			sess.setAttribute(LANDING_MSG, "欢迎来到vBox, 您的注册已经成功，请申请您第一个vBox吧！");
			return SUCCEED;
		}
		return ERROR;
	}
}
