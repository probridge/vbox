package com.probridge.vbox.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.utils.Utility;

public class LoginAction implements Action {
	public static final String SUCCEED = "login";
	private static final Logger logger = LoggerFactory.getLogger(LoginAction.class);

	public String execute(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("LoginAction: " + request.getMethod() + " remembered: " + SecurityUtils.getSubject().isRemembered());
		String email = request.getParameter("inputEmail");
		if (!Utility.isEmptyOrNull(email))
			request.setAttribute("email", email);
		else if (SecurityUtils.getSubject().isRemembered())
			request.setAttribute("email", SecurityUtils.getSubject().getPrincipal());
		//
		return SUCCEED;
	}
}
