package com.probridge.vbox.vmm.wmi;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.vmm.wmi.utils.NotifierListener;
import com.probridge.vbox.vmm.wmi.utils.UpdateServiceEvent;

/**
 * Class designed to automate network and remote service connection lifecycle.
 * 
 */
public class WeakHyperVVMM extends WeakReference<HyperVVMM> implements NotifierListener<HyperVVMM, UpdateServiceEvent> {

	/** The queue associated with freed objects. */
	private static ReferenceQueue<HyperVVMM> refQueue = new ReferenceQueue<HyperVVMM>();
	/** The allow the freed XenServerVMM to be enqueued */
	private static ArrayList<WeakHyperVVMM> vmms = new ArrayList<WeakHyperVVMM>();
	/** The class' logger */
	private static final Logger logger = LoggerFactory.getLogger(WeakHyperVVMM.class);

	private VirtualizationServiceLocator service;

	public WeakHyperVVMM(HyperVVMM referent) {
		super(referent, refQueue);
		vmms.add(this);
		this.service = referent.getServiceLocator();
	}

	public void update(HyperVVMM notifier, UpdateServiceEvent event) {
		this.service = notifier.getServiceLocator();
		logger.debug("Service Instance updated from " + notifier);
	}

	protected void disconnect() {
		service.destroySession();
	}

	/**
	 * This static bloc is in charge of starting the cleaning thread. It waits
	 * for a IVirtualBox to be finalized and then close every opened resources.
	 * It also adds a shutdown hook to disconnect the object running at the top
	 * level (directly within the main method).
	 */
	static {
		Thread myTh = new Thread() {
			@Override
			public void run() {
				Thread.currentThread().setName("vBox Hypervisor Reference Cleaner Thread");
				WeakHyperVVMM vmm = null;
				while (true) {
					try {
						logger.debug("Waiting for weak references to be enqueued");
						vmm = (WeakHyperVVMM) refQueue.remove();
						vmm.disconnect();
					} catch (Exception e) {
					}
				}
			}
		};
		myTh.setDaemon(true);
		myTh.start();
		Runtime.getRuntime().addShutdownHook(new VMMShutdownHook(vmms));
	}
	
	public static VMMShutdownHook shutdownHook = new VMMShutdownHook(vmms);
}
