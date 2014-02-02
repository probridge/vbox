package com.probridge.vbox.servlet;

import org.apache.ibatis.session.SqlSession;
import org.jinterop.dcom.common.JIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.VMMapper;
import com.probridge.vbox.model.VM;
import com.probridge.vbox.vmm.wmi.HyperVVM;
import com.probridge.vbox.vmm.wmi.HyperVVMM;
import com.probridge.vbox.vmm.wmi.VirtualMachine.HeartBeat;
import com.probridge.vbox.vmm.wmi.VirtualMachine.VMState;
import com.probridge.vbox.vmm.wmi.utils.VirtualServiceException;
import com.probridge.vbox.zk.AdminTaskManager;

public class SaveVMSettingsTask extends VMTask {
	private static final Logger logger = LoggerFactory.getLogger(SaveVMSettingsTask.class);
	//
	private VM vm;
	private boolean saveStorage;
	private boolean saveConfig;

	public SaveVMSettingsTask(String sid, String opid, VM vmToSave, boolean saveConfig, boolean saveStorage) {
		super(sid, opid);
		this.vm = vmToSave;
		this.saveConfig = saveConfig;
		this.saveStorage = saveStorage;
		if ("--".equals(vm.getVmId())) {
			this.saveConfig = true;
			this.saveStorage = true;
		}
	}

	@Override
	public void run() {
		super.run();
		logger.debug("Starting to save vm setting..");
		ops.setMsg("保存vBox设置");
		try {
			if (saveConfig || saveStorage) {
				HyperVVMM vmm = null;
				HyperVVM thisVM = null;
				if ("--".equals(vm.getVmId())) {
					vmm = HyperVVMM.hypervisors[vm.getVmHypervisorId()];
					HyperVVM vmTemplate = vmm.getVMTemplate();
					thisVM = vmTemplate.clone(vm.getVmName());
					vm.setVmId(thisVM.getID());
					vm.setVmStatus(0);
				} else {
					thisVM = HyperVVMM.locateVM(vm.getVmId());
				}
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
					throw new VirtualServiceException("无法关闭" + thisVM.getName() + "，请联系我们");
				ops.setMsg("vBox:" + thisVM.getName() + "已关闭");
				logger.debug("Powered off now");
				//
				if ("0".equals(vm.getVmVhdGmType()))
					vm.setVmVhdGmFilename(VBoxConfig.gmVhdPrefix + vm.getVmName() + "_" + System.currentTimeMillis()
							+ ".vhd");
				else if ("1".equals(vm.getVmVhdGmType()))
					vm.setVmVhdGmFilename(VBoxConfig.gmVhdPrefix + vm.getVmName() + "_" + System.currentTimeMillis()
							+ "_Clone.vhd");
				else if ("2".equals(vm.getVmVhdGmType()))
					vm.setVmVhdGmFilename(vm.getVmVhdGmImage());
				//
				if (saveConfig) {
					ops.setMsg("正在配置vBox处理资源");
					thisVM.modifyConfiguration(vm.getVmCores(), vm.getVmMemory(), vm.getVmMemory());
					ops.setMsg("正在初始化vBox网络");
					thisVM.modifyNetwork(Integer.parseInt(vm.getVmNetworkType()));
				}
				if (saveStorage) {
					ops.setMsg("正在配置vBox存储");
					thisVM.modifyStorage(vm.getVmVhdGmImage(), Integer.parseInt(vm.getVmVhdGmType()),
							vm.getVmVhdGmFilename(), vm.getVmVhdUserFilename());
					vm.setVmInitFlag("Y");
				}
			}
			ops.setMsg("正在保存vBox设置");
			SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
			VMMapper mapper = session.getMapper(VMMapper.class);
			if (mapper.updateByPrimaryKey(vm) == 0)
				mapper.insert(vm);
			//
			session.commit();
			session.close();
			//
			ops.setMsg("操作完成");
			logger.debug("Finished");
			ops.setRetval(0);
		} catch (VirtualServiceException | NumberFormatException | JIException e) {
			ops.setMsg("操作失败:" + e.getMessage());
			ops.setRetval(1);
			logger.error("error saving vm setting " + vm.getVmName(), e);
		} finally {
			AdminTaskManager.getInstance().getThreadlist().remove(sid);
		}
	}
}
