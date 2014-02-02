package com.probridge.vbox.vmm;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.VMMapper;
import com.probridge.vbox.model.VM;
import com.probridge.vbox.vmm.wmi.HyperVVM;
import com.probridge.vbox.vmm.wmi.HyperVVMM;
import com.probridge.vbox.vmm.wmi.utils.Utils;
import com.probridge.vbox.vmm.wmi.utils.VirtualServiceException;

public class VMMDaemon implements Runnable {
	private static ConcurrentHashMap<String, VMMonitor> montiorThreads = new ConcurrentHashMap<String, VMMonitor>();
	private static final Logger logger = LoggerFactory.getLogger(VMMDaemon.class);
	private ExecutorService executor;
	private AtomicBoolean isInterrupted = new AtomicBoolean(false);

	@Override
	public void run() {
		Thread.currentThread().setName("vBox Management Daemon Thread");
		logger.info("Daemon Starting");
		// init
		logger.debug("Setting up thread pool of size " + VBoxConfig.vmMonitorThreadPoolSize);
		executor = Executors.newFixedThreadPool(VBoxConfig.vmMonitorThreadPoolSize);
		while (true) {
			try {
				List<String> shutdownList = VMSessionScheduler.getInstance().getShutdownList();
				if (shutdownList.size() > 0)
					logger.debug("Discovered " + shutdownList.size() + " VMs to be shutdown.");
				//
				for (HyperVVMM eachVMM : HyperVVMM.hypervisors) {
					Collection<HyperVVM> vmList = eachVMM.getVirtualMachines();
					//
					for (HyperVVM eachVM : vmList) {
						//
						if (!isManagedVM(eachVM.getID())) {
							// last chance fuse to cancel thread
							VMMonitor monitorThread = montiorThreads.get(eachVM.getID());
							if (monitorThread != null)
								monitorThread.notifyInterrupt();
							continue; // skip
						}
						//
						if (shutdownList.contains(eachVM.getID())) {
							logger.info("Suspending VM" + eachVM.getName() + "[" + eachVM.getID()
									+ "] due to shutdown timer expired.");
							if (isDynamicVM(eachVM.getID()))
								eachVM.suspend();
							else
								logger.info("VM " + eachVM.getName() + " is persistent. suspend ignored");
							//
							continue;
						}
						//
						if (montiorThreads.get(eachVM.getID()) == null) {
							logger.info("Setting up monitor for VM" + eachVM.getName() + "[" + eachVM.getID() + "].");
							VMMonitor monitor = new VMMonitor(eachVM, this);
							executor.submit(monitor);
							montiorThreads.put(eachVM.getID(), monitor);
						}
					}
				}
				Utils.sleepCheck(30, isInterrupted);
			} catch (VirtualServiceException e) {
				logger.error("error while deamon is scanning the VMs, retrying in 30sec", e);
				try {
					Utils.sleepCheck(30, isInterrupted);
				} catch (InterruptedException e1) {
					logger.info("Daemon interrupted during retry period, shutting down thread pool.");
					shutdownDaemon();
					break;
				}
			} catch (InterruptedException e) {
				logger.info("Daemon interrupted, shutting down thread pool.");
				shutdownDaemon();
				break;
			}
		}
		logger.info("Daemon exiting.");
	}

	public void notifyInterrupt() {
		isInterrupted.set(true);
	}

	private void shutdownDaemon() {
		executor.shutdown();
		//
		logger.info("Notifying all monitor thread to stop.");
		Iterator<VMMonitor> it = montiorThreads.values().iterator();
		while (it.hasNext()) {
			it.next().notifyInterrupt();
		}
		logger.info("Waiting for all monitor thread stop status.");
		try {
			if (executor.awaitTermination(15, TimeUnit.SECONDS) == false) {
				executor.shutdownNow();
			}
		} catch (InterruptedException e1) {
		}
		logger.info("Daemon shutted down.");
	}

	public void unregisterMonitor(String vmid) {
		montiorThreads.remove(vmid);
	}

	private boolean isManagedVM(String uuid) {
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		VMMapper mapper = session.getMapper(VMMapper.class);
		VM thisVM = mapper.selectByPrimaryKey(uuid);
		session.close();
		return thisVM != null;
	}

	private boolean isDynamicVM(String uuid) {
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		VMMapper mapper = session.getMapper(VMMapper.class);
		VM thisVM = mapper.selectByPrimaryKey(uuid);
		session.close();
		return "1".equals(thisVM.getVmPersistance());
	}
}
