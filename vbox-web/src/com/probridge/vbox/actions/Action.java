package com.probridge.vbox.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Action {
	public static final String ERROR = "error";
	public static final String LANDING_MSG = "landing_msg";

	public abstract String execute(HttpServletRequest request, HttpServletResponse response);
}
