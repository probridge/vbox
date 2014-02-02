package com.probridge.vbox.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Home2Action implements Action {
	public static final String SUCCEED = "home2";
	private static final Logger logger = LoggerFactory
			.getLogger(Home2Action.class);

	public String execute(HttpServletRequest request,
			HttpServletResponse response) {
		logger.debug("entering home2");
		return SUCCEED;
	}
}
