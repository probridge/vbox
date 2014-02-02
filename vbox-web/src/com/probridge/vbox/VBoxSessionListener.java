package com.probridge.vbox;

import java.util.Date;

import javax.servlet.http.HttpSessionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.http.HttpSessionListener;

import com.probridge.vbox.vmm.VMSessionScheduler;

public class VBoxSessionListener extends HttpSessionListener {
	
	private static final Logger logger = LoggerFactory
			.getLogger(VBoxSessionListener.class);
		
	@Override
	public void sessionCreated(HttpSessionEvent evt) {
		logger.debug("vBox session created! [" + this + "]"
				+ evt.getSession().getId() + " @ " + (new Date()));
		super.sessionCreated(evt);
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent evt) {
		super.sessionDestroyed(evt);
		VMSessionScheduler.getInstance().markShutdown(evt.getSession().getId());
		logger.debug("vBox session destory! [" + this + "]"
				+ evt.getSession().getId() + " @ " + (new Date()));
	}
}
