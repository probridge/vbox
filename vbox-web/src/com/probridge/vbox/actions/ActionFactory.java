package com.probridge.vbox.actions;

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionFactory {
	private static final Logger logger = LoggerFactory.getLogger(ActionFactory.class);

	private static ConcurrentHashMap<String, Action> urimap = new ConcurrentHashMap<String, Action>();

	static {
		urimap.put("index", new IndexAction());
		urimap.put("home", new HomeAction());
		urimap.put("jaccount", new JAccountAction());
		urimap.put("register", new RegisterAction());
		urimap.put("detail", new DetailAction());
		urimap.put("problem", new AccountProblemAction());
		urimap.put("expired", new PasswordExpiredAction());
		urimap.put("landing", new LandingAction());
		urimap.put("applyaccess", new ApplyAccessAction());
		urimap.put("applyplaced", new ApplyPlacedAction());
		urimap.put("viewbox", new ViewboxAction());
		urimap.put("viewbox2", new Viewbox2Action());
		urimap.put("myfiles", new MyFileAction());
		urimap.put("login", new LoginAction());
		urimap.put("logout", new LogoutAction());
		urimap.put("tutorial", new TutorialAction());
		urimap.put("about", new AboutAction());
		urimap.put("contact", new ContactAction());
	}

	public static Action getAction(HttpServletRequest request) {
		String url = request.getServletPath();
		logger.debug("Full url is:" + url);
		if (url != null && url.length() > 0) {
			String uri = url.substring(1, url.indexOf(".do"));
			logger.debug("Looking up Action for URI: " + uri);
			return urimap.get(uri);
		}
		return null;
	}

	public static Action getAction(String action) {
		return urimap.get(action);
	}
}
