package com.probridge.vbox.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.vmm.wmi.HyperVVM;
import com.probridge.vbox.vmm.wmi.HyperVVMM;
import com.probridge.vbox.vmm.wmi.VirtualMachine.VMState;
import com.probridge.vbox.vmm.wmi.utils.VirtualServiceException;
import com.probridge.vbox.zk.AdminTaskManager;

public class PowerOnVboxTask extends VMTask {
	private static final Logger logger = LoggerFactory.getLogger(PowerOnVboxTask.class);
	//
	private String uuid;

	public PowerOnVboxTask(String sid, String opid, String uuid) {
		super(sid, opid);
		this.uuid = uuid;
	}

	@Override
	public void run() {
		super.run();
		logger.debug("Starting to process power on vbox..");
		ops.setMsg("开启vBox");
		try {
			HyperVVM vm = HyperVVMM.locateVM(uuid);
			ops.setMsg("正在打开vBox");
			//
			vm.powerOn();
			// Wait powered off status
			ops.setMsg("正在等待vBox开启状态");
			logger.debug("Waiting for vm power on status");
			//
			if (!vm.waitFor(VMState.Running))
				throw new VirtualServiceException("无法开机vBox，请联系我们");
			ops.setMsg(vm.getName() + "已开机");
			logger.debug("Finished");
			ops.setRetval(0);
		} catch (Exception e) {
			ops.setMsg("操作失败：" + e.getMessage());
			ops.setRetval(1);
			logger.error("error powering on vBox " + uuid, e);
		} finally {
			AdminTaskManager.getInstance().getThreadlist().remove(sid);
		}
	}
}
