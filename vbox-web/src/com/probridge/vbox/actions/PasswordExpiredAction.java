package com.probridge.vbox.actions;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.UsersMapper;
import com.probridge.vbox.model.Users;
import com.probridge.vbox.utils.Utility;

public class PasswordExpiredAction implements Action {
	public static final String PROCEED = "expired";
	public static final String SUCCEED = "landing";
	private static final Logger logger = LoggerFactory.getLogger(PasswordExpiredAction.class);

	public String execute(HttpServletRequest request, HttpServletResponse response) {
		if ("GET".equals(request.getMethod())) {
			logger.info("Password expired, force resetting:" + SecurityUtils.getSubject().getPrincipal().toString());
			return PROCEED;
		} else if ("POST".equals(request.getMethod())) {
			String identity = Utility.getStringVal(request.getAttribute("identity"));
			//
			String p1 = request.getParameter("inputPassword1");
			String p2 = request.getParameter("inputPassword2");
			//
			if (Utility.isEmptyOrNull(p1) || Utility.isEmptyOrNull(p2)) {
				request.setAttribute("error", "密码不得为空。");
				return PROCEED;
			}

			if (!p1.equals(p2)) {
				request.setAttribute("error", "两次密码输入不同，请重新输入。");
				return PROCEED;
			}
			//
			SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
			UsersMapper umapper = session.getMapper(UsersMapper.class);
			Users thisUser = umapper.selectByPrimaryKey(identity);
			//
			if (p1.equals(thisUser.getUserPassword())) {
				session.close();
				request.setAttribute("error", "请不要重复使用相同的密码。");
				return PROCEED;
			}
			// set to 3 months later
			thisUser.setUserPassword(p1);
			if (thisUser.getUserPwdExpire() != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				cal.add(Calendar.MONTH, 3);
				thisUser.setUserPwdExpire(cal.getTime());
			}
			umapper.updateByPrimaryKey(thisUser);
			session.commit();
			session.close();
			//
			Session sess = SecurityUtils.getSubject().getSession();
			sess.setAttribute(LANDING_MSG, "密码已经成功修改");
			return SUCCEED;
		}
		return ERROR;
	}
}
