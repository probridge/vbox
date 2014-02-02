package com.probridge.vbox.servlet;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.vmm.wmi.HyperVVM;
import com.probridge.vbox.vmm.wmi.HyperVVMM;
import com.probridge.vbox.vmm.wmi.VirtualMachine.HeartBeat;
import com.probridge.vbox.vmm.wmi.VirtualMachine.VMState;
import com.probridge.vbox.vmm.wmi.utils.VirtualServiceException;
import com.probridge.vbox.zk.AdminTaskManager;

public class VMSwitchTask extends VMTask {
	private static final Logger logger = LoggerFactory.getLogger(VMSwitchTask.class);
	//
	private ArrayList<String> suspend;
	private ArrayList<String> resume;

	public VMSwitchTask(String sid, String opid, ArrayList<String> resume, ArrayList<String> suspend) {
		super(sid, opid);
		this.resume = resume;
		this.suspend = suspend;
	}

	@Override
	public void run() {
		super.run();
		logger.debug("Starting to switch");
		ops.setMsg("开始切换vBox");
		for (String eachResume : resume)
			if (suspend.contains(eachResume))
				suspend.remove(eachResume);

		try {
			ops.setMsg("正在分配资源");
			for (String eachSuspend : suspend) {
				HyperVVM thisVM = HyperVVMM.locateVM(eachSuspend);
				logger.debug("Powering off " + eachSuspend);
				//
				if (thisVM.getHeartBeat() == HeartBeat.OK) {
					thisVM.shutdown();
					ops.setMsg("正在等待vBox操作系统关闭状态");
					if (!thisVM.waitFor(VMState.PoweredOff))
						thisVM.powerOff();
				} else
					thisVM.powerOff();
				// Wait powered off status
				ops.setMsg("正在等待vBox关闭状态");
				logger.debug("Waiting vm in stopped status");
				//
				if (!thisVM.waitFor(VMState.PoweredOff))
					throw new VirtualServiceException("无法关闭vBox，请联系我们");
				//
			}
			ops.setMsg("正在启动vBox");
			for (String eachResume : resume) {
				HyperVVM thisVM = HyperVVMM.locateVM(eachResume);
				logger.debug("Powering on " + eachResume);
				thisVM.powerOn();
				if (!thisVM.waitFor(VMState.Running))
					throw new VirtualServiceException("无法启动vBox，请联系我们");
			}
			ops.setMsg("启动命令成功，请等待vBox可用状态");
			logger.debug("Finished");
			ops.setRetval(0);
		} catch (VirtualServiceException e) {
			ops.setMsg("操作失败");
			ops.setRetval(1);
			logger.error("error switching vm to " + resume.get(0), e);
		} finally {
			AdminTaskManager.getInstance().getThreadlist().remove(sid);
		}
	}
}
