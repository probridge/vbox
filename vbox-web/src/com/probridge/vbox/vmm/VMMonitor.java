package com.probridge.vbox.vmm;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.VMMapper;
import com.probridge.vbox.model.VM;
import com.probridge.vbox.utils.Utility;
import com.probridge.vbox.vmm.wmi.HyperVVM;
import com.probridge.vbox.vmm.wmi.VMGuestStatus;
import com.probridge.vbox.vmm.wmi.VirtualMachine.VMState;
import com.probridge.vbox.vmm.wmi.utils.Utils;
import com.probridge.vbox.vmm.wmi.utils.VirtualServiceException;

public class VMMonitor implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(VMMonitor.class);

	private VMMDaemon daemon;
	private String vmid, vmName;

	private VMState state;
	private AtomicBoolean interruptted = new AtomicBoolean(false);

	private HyperVVM vm;

	public VMMonitor(HyperVVM vm, VMMDaemon vmmDaemon) throws VirtualServiceException {
		this.daemon = vmmDaemon;
		this.vm = vm;
		this.vmName = vm.getName();
		this.vmid = vm.getID();
	}

	@Override
	public void run() {
		Thread.currentThread().setName("vBox Monitor Thread");
		logger.info("Monitor starting.");
		try {
			VMState previousState = VMState.Unknown;
			VMGuestStatus previousGuestState = null;
			while (true) {
				VMGuestStatus guestState = vm.getVMGuestStatus();
				state = vm.getState();
				//
				if (!state.equals(previousState) || !guestState.equals(previousGuestState)) {
					logger.info("Status change detected for " + vmid + " (" + vmName + ")");
					logger.info("Got VM" + vmName + "[" + vmid + "] new state: " + state.getName() + ", hb: "
							+ guestState.getHeartBeat().getName() + ", pwd: " + guestState.getCredidential() + ", ip="
							+ guestState.getIPAddress());
					// save new state to DB;
					SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
					VMMapper mapper = session.getMapper(VMMapper.class);
					VM thisVM = mapper.selectByPrimaryKey(vmid);
					if (thisVM != null) {
						thisVM.setVmIpAddress(guestState.getIPAddress());
						thisVM.setVmHeartbeat(guestState.getHeartBeat().getValue());
						thisVM.setVmStatus(state.getValue());
						thisVM.setVmGuestPassword(guestState.getCredidential());
						thisVM.setVmLastUpdateTimestamp(new Date());
						if (!Utility.isEmptyOrNull(guestState.getCredidential()))
							thisVM.setVmInitFlag("N");
						mapper.updateByPrimaryKey(thisVM);
						session.commit();
					}
					session.close();
					previousState = state;
					previousGuestState = guestState;
				} else { // nothing changed
					Utils.sleepCheck(10, interruptted);
					continue;
				}
			}
		} catch (InterruptedException e) {
			logger.info("Monitor for VM" + vmName + "[" + vmid + "] interrupted.");
		} catch (Exception e) {
			logger.error("error while monitoring vm " + vmName, e);
		} finally {
			daemon.unregisterMonitor(vmid);
			logger.info("Monitor for VM" + vmName + "[" + vmid + "] exiting");
		}
	}

	public void notifyInterrupt() {
		interruptted.set(true);
	}
}
