package com.probridge.vbox.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.vmm.wmi.HyperVVM;
import com.probridge.vbox.vmm.wmi.HyperVVMM;
import com.probridge.vbox.vmm.wmi.VirtualMachine.HeartBeat;
import com.probridge.vbox.vmm.wmi.VirtualMachine.VMState;
import com.probridge.vbox.vmm.wmi.utils.VirtualServiceException;
import com.probridge.vbox.zk.AdminTaskManager;

public class ShutdownVboxTask extends VMTask {
	private static final Logger logger = LoggerFactory.getLogger(ShutdownVboxTask.class);
	//
	private String uuid;

	public ShutdownVboxTask(String sid, String opid, String uuid) {
		super(sid, opid);
		this.uuid = uuid;
	}

	@Override
	public void run() {
		super.run();
		logger.debug("Starting to process shutdown vbox..");
		ops.setMsg("关闭vBox");
		try {
			HyperVVM vm = HyperVVMM.locateVM(uuid);
			ops.setMsg("正在关闭vBox操作系统");
			//
			if (vm.getHeartBeat() == HeartBeat.OK) {
				vm.shutdown();
				ops.setMsg("正在等待vBox操作系统关闭状态");
				if (!vm.waitFor(VMState.PoweredOff))
					vm.powerOff();
			} else
				vm.powerOff();
			// Wait powered off status
			ops.setMsg("正在等待vBox关闭状态");
			logger.debug("Waiting vm in stopped status");
			//
			if (!vm.waitFor(VMState.PoweredOff))
				throw new VirtualServiceException("无法关闭vBox，请联系我们");
			ops.setMsg("vBox:" + vm.getName() + "已关闭");
			logger.debug("Finished");
			ops.setRetval(0);
		} catch (VirtualServiceException e) {
			ops.setMsg("操作失败：" + e.getMessage());
			ops.setRetval(1);
			logger.error("error shutting down vBox " + uuid, e);
		} finally {
			AdminTaskManager.getInstance().getThreadlist().remove(sid);
		}
	}
}
