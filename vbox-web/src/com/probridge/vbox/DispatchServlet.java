package com.probridge.vbox;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.actions.Action;
import com.probridge.vbox.actions.ActionFactory;

/**
 * Servlet implementation class DispatchServlet
 */
public class DispatchServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7982070678512891402L;
	private static final Logger logger = LoggerFactory.getLogger(DispatchServlet.class);

	private ArrayList<String> landingNotRequired = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DispatchServlet() {
		super();
		landingNotRequired = new ArrayList<String>();
		landingNotRequired.add("landing");
		landingNotRequired.add("jaccount");
		landingNotRequired.add("index");
		landingNotRequired.add("problem");
		landingNotRequired.add("expired");
		landingNotRequired.add("register");
		landingNotRequired.add("login");
		landingNotRequired.add("logout");
		landingNotRequired.add("tutorial");
		landingNotRequired.add("about");
		landingNotRequired.add("contact");
	}

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		try {
			//
			Session sess = SecurityUtils.getSubject().getSession();
			if (SecurityUtils.getSubject().isAuthenticated())
				request.setAttribute("identity", SecurityUtils.getSubject().getPrincipal());
			else
				request.setAttribute("identity", null);
			//
			request.setAttribute("version", VBoxConfig.systemVersion);
			request.setAttribute("globalNotice", VBoxConfig.globalNotice);
			//
			String uri = request.getServletPath();
			uri = uri.substring(1, uri.indexOf(".do"));

			if (SecurityUtils.getSubject().isAuthenticated() && !"yes".equals(sess.getAttribute("landed"))
					&& !landingNotRequired.contains(uri)) {
				logger.debug("Logged, need one time init, redirecting to: landing.do");
				response.sendRedirect("landing.do");
				return;
			}

			Action action = ActionFactory.getAction(request);
			String view = action.execute(request, response);
			//
			if (view == null) {
				// no-op
			} else if (view.equals(uri)) {
				logger.debug("Forwarding to JSP processing: " + view + ".jsp via " + request.getMethod());
				request.getRequestDispatcher("WEB-INF/jsps/" + view + ".jsp").forward(request, response);
			} else {
				logger.debug("Redirecting to: " + view + " via " + request.getMethod());
				response.sendRedirect(view + ".do");
			}
		} catch (Exception e) {
			throw new ServletException("Executing action failed.", e);
		}
	}
}
