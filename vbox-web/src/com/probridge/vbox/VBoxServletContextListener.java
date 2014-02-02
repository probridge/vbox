package com.probridge.vbox;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import jcifs.Config;

import org.jinterop.dcom.core.JISession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.AbandonedConnectionCleanupThread;
import com.probridge.vbox.vmm.ResourceMonitor;
import com.probridge.vbox.vmm.VMMDaemon;
import com.probridge.vbox.vmm.wmi.HyperVVMM;
import com.probridge.vbox.vmm.wmi.WeakHyperVVMM;

public class VBoxServletContextListener implements ServletContextListener {

	private Thread daemonThread;
	private Thread[] resMgrThread;
	private VMMDaemon daemon;

	private static final Logger logger = LoggerFactory.getLogger(VBoxServletContextListener.class);

	static {
		Thread.currentThread().setName("vBox WebApp Bootstrap Thread");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		//
		logger.info("vBox Application Starting");
		//
		HyperVVMM.initialize();
		//
		daemon = new VMMDaemon();
		daemonThread = new Thread(daemon);
		daemonThread.setDaemon(true);
		daemonThread.start();
		//
		ResourceMonitor.initialize();
		resMgrThread = new Thread[ResourceMonitor.instances.length];
		for (int i = 0; i < ResourceMonitor.instances.length; i++) {
			resMgrThread[i] = new Thread(ResourceMonitor.instances[i]);
			resMgrThread[i].setDaemon(true);
			resMgrThread[i].start();
		}
		//
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		logger.info("vBox Application Stoping");
		logger.info("Send stop request to vmm daemon.");
		daemon.notifyInterrupt();
		//
		logger.info("Send stop request to resource monitors.");
		for (ResourceMonitor eachResMgr : ResourceMonitor.instances)
			eachResMgr.notifyInterrupt();
		//
		logger.info("Waiting for inflight work to cleaning up.");
		waitTermination(daemonThread);
		for (Thread eachResMgrThd : resMgrThread)
			waitTermination(eachResMgrThd);
		//
		logger.info("Shutting down VMM manager");
		WeakHyperVVMM.shutdownHook.start();
		logger.info("Wait for VMM manager to finish.");
		waitTermination(WeakHyperVVMM.shutdownHook);
		//
		logger.info("Shutting down WMI sessions");
		JISession.requestShutdown();
		logger.info("Shutting down WMI sessions");
		logger.info("Stopping...");
		Enumeration<java.sql.Driver> drivers = DriverManager.getDrivers();
		java.sql.Driver d = null;
		while (drivers.hasMoreElements()) {
			try {
				d = drivers.nextElement();
				DriverManager.deregisterDriver(d);
				logger.warn(String.format("Driver %s deregistered", d));
			} catch (SQLException ex) {
				logger.warn(String.format("Error deregistering driver %s", d), ex);
			}
		}
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
		for (Thread t : threadArray) {
			if (t.getName().contains("Abandoned connection cleanup thread")) {
				synchronized (t) {
					try {
						AbandonedConnectionCleanupThread.shutdown();
					} catch (InterruptedException e) {
						logger.warn("SEVERE problem cleaning up: " + e.getMessage());
					}
				}
			}
		}
		//
		logger.info("vBox Application Stopped...");
	}

	private void waitTermination(Thread t) {
		int waitTime = 0;
		while (t != null && t.isAlive()) {
			try {
				Thread.sleep(1000);
				waitTime++;
				if (waitTime >= 30) {
					logger.error("Could not terminate thread, sending hard interrupt.");
					t.interrupt();
					break;
				}
			} catch (InterruptedException e) {
				break;
			}
		}
		return;
	}
}
