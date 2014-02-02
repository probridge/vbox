package com.probridge.vbox.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class IndexAction implements Action {
	public static final String SUCCEED = "index";

	public String execute(HttpServletRequest request,
			HttpServletResponse response) {
		return SUCCEED;
	}
}
