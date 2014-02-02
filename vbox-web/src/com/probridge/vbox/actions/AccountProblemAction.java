package com.probridge.vbox.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountProblemAction implements Action {
	public static final String SUCCEED = "problem";
	private static final Logger logger = LoggerFactory
			.getLogger(AccountProblemAction.class);

	public String execute(HttpServletRequest request,
			HttpServletResponse response) {
		logger.info("Account disabled or expired:" + SecurityUtils.getSubject().getPrincipal().toString());
		SecurityUtils.getSubject().logout();
		return SUCCEED;
	}
}
