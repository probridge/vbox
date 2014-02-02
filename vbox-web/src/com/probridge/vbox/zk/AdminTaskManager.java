package com.probridge.vbox.zk;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.servlet.OpStatus;
import com.probridge.vbox.servlet.VMTask;

public class AdminTaskManager {
	private static final Logger logger = LoggerFactory.getLogger(AdminTaskManager.class);
	//
	private final ConcurrentHashMap<String, Thread> threadList = new ConcurrentHashMap<String, Thread>();
	private final ConcurrentHashMap<String, OpStatus> opResults = new ConcurrentHashMap<String, OpStatus>();
	private final ExecutorService executor = Executors.newFixedThreadPool(VBoxConfig.vmAdminTaskThreadPoolSize);
	private static AdminTaskManager instance;

	//
	public ConcurrentHashMap<String, Thread> getThreadlist() {
		return threadList;
	}

	public ConcurrentHashMap<String, OpStatus> getOpResults() {
		return opResults;
	}

	private AdminTaskManager() {
		logger.debug("Setting up Admin Task Manager thread pool of size " + VBoxConfig.vmAdminTaskThreadPoolSize);
	}

	public static AdminTaskManager getInstance() {
		if (instance == null)
			instance = new AdminTaskManager();
		return instance;
	}

	public String submit(VMTask task) {
		String sid = task.getSid();
		logger.debug("Processing " + task.getClass().toString() + " for SID " + sid);
		if (threadList.get(sid) != null) {
			logger.warn("Found ongoing task for SID " + sid + ", quiting");
			return null;
		} else {
			String opid = String.valueOf(System.currentTimeMillis());
			logger.debug("Submit task " + task.getClass().toString() + " for SID " + sid);
			executor.submit(task);
			return opid;
		}
	}

	public OpStatus queryStatus(String opid) {
		logger.debug("getting op result for opid=" + opid);
		OpStatus s = opResults.get(opid);
		if (s == null) {
			logger.debug("no operation found for opid=" + opid);
			s = new OpStatus(opid, "请稍等");
			s.setRetval(2);
		} else {
			logger.debug("got op for opid" + opid + s.toString());
		}
		//
		return s;
	}
}
