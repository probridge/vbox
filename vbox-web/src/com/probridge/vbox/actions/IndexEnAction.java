package com.probridge.vbox.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class IndexEnAction implements Action {
	public static final String SUCCEED = "index-en";

	public String execute(HttpServletRequest request,
			HttpServletResponse response) {
		return SUCCEED;
	}
}
