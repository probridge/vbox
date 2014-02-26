package com.probridge.vbox.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ContactAction implements Action {
	public static final String SUCCEED = "contact";
	public String execute(HttpServletRequest request,
			HttpServletResponse response) {
		return SUCCEED;
	}
}
