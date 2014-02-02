package com.probridge.vbox.vmm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.probridge.vbox.VBoxConfig;

public class VMSessionScheduler {
	private static final Logger logger = LoggerFactory.getLogger(VMSessionScheduler.class);
	//
	private ArrayListMultimap<String, String> relationships = ArrayListMultimap
			.<String, String> create();
	//
	private HashMap<String, Long> vmShutdownCounter = new HashMap<String, Long>();
	private Object mutex = new Object();

	private static VMSessionScheduler instance;

	private VMSessionScheduler() {
		logger.info("VM Session Montior Loaded.");
	};

	public static VMSessionScheduler getInstance() {
		if (instance == null)
			instance = new VMSessionScheduler();
		return instance;
	}

	public void register(String sessionID, String vmID) {
		synchronized (mutex) {
			if (!relationships.containsEntry(sessionID, vmID))
				relationships.put(sessionID, vmID);
			vmShutdownCounter.put(vmID, 0L);
			return;
		}
	}

	public List<String> getMonitorList() {
		synchronized (mutex) {
			return new ArrayList<String>(relationships.values());
		}
	}

	public void markShutdown(String sessionID) {
		synchronized (mutex) {
			List<String> relatedVM = relationships.removeAll(sessionID);
			for (String eachVM : relatedVM)
				if (!relationships.containsValue(eachVM))
					vmShutdownCounter.put(eachVM, System.currentTimeMillis()
							+ VBoxConfig.vmShutdownDelay);
			return;
		}
	}

	public List<String> getShutdownList() {
		synchronized (mutex) {
			ArrayList<String> val = new ArrayList<String>();

			Iterator<Entry<String, Long>> it = vmShutdownCounter.entrySet()
					.iterator();
			while (it.hasNext()) {
				Entry<String, Long> item = it.next();
				long schedule = item.getValue();
				if (schedule != 0L && schedule <= System.currentTimeMillis()) {
					val.add(item.getKey());
					it.remove();
				}
			}
			return val;
		}
	}
}
