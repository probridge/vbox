package com.probridge.vbox.actions;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.utils.Utility;

import edu.sjtu.jaccount.JAccountManager;

public class LogoutAction implements Action {
	public static final String SUCCEED = "index";

	public String execute(HttpServletRequest request, HttpServletResponse response) {
		String identity = Utility.getStringVal(request.getAttribute("identity"));
		SecurityUtils.getSubject().logout();
		String returnUrl = request.getRequestURI().substring(0, request.getRequestURI().lastIndexOf("/") + 1);
		if (request.getParameter("mgmt") != null)
			returnUrl += "management/index.zul";
		if (identity != null && identity.endsWith(VBoxConfig.jAccountSuffix)) {
			JAccountManager jam = new JAccountManager(VBoxConfig.jAccountSiteId, VBoxConfig.configPath);
			jam.logout(request, response, returnUrl);
		} else {
			try {
				response.sendRedirect(returnUrl);
			} catch (IOException e) {
			}
		}
		return null;
	}
}
