package com.probridge.vbox.servlet;

import org.apache.ibatis.session.SqlSession;
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

public class FixVboxTask extends VMTask {
	private static final Logger logger = LoggerFactory.getLogger(FixVboxTask.class);
	//
	private String uuid;

	public FixVboxTask(String sid, String opid, String uuid) {
		super(sid, opid);
		this.uuid = uuid;
	}

	@Override
	public void run() {
		super.run();
		logger.debug("Starting to process fixvbox..");
		ops.setMsg("开始重置vBox");
		SqlSession session = null;
		try {
			HyperVVM vm = HyperVVMM.locateVM(uuid);
			ops.setMsg("正在关闭vBox的操作系统");
			if (vm.getHeartBeat() == HeartBeat.OK) {
				vm.shutdown();
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
			//
			logger.debug("Powered off now");
			//
			session = VBoxConfig.sqlSessionFactory.openSession();
			VMMapper mapper = session.getMapper(VMMapper.class);
			VM thisVM = mapper.selectByPrimaryKey(uuid);
			session.close();

			if ("0".equals(thisVM.getVmVhdGmType()))
				thisVM.setVmVhdGmFilename(VBoxConfig.gmVhdPrefix + thisVM.getVmName() + "_"
						+ System.currentTimeMillis() + ".vhd");
			else if ("1".equals(thisVM.getVmVhdGmType()))
				thisVM.setVmVhdGmFilename(VBoxConfig.gmVhdPrefix + thisVM.getVmName() + "_"
						+ System.currentTimeMillis() + "_Clone.vhd");

			ops.setMsg("正在重置vBox处理资源");
			vm.modifyConfiguration(thisVM.getVmCores(), thisVM.getVmMemory(), thisVM.getVmMemory());
			ops.setMsg("正在重置vBox存储");
			vm.modifyStorage(thisVM.getVmVhdGmImage(), Integer.parseInt(thisVM.getVmVhdGmType()),
					thisVM.getVmVhdGmFilename(), thisVM.getVmVhdUserFilename());
			ops.setMsg("正在初始化vBox网络");
			vm.modifyNetwork(Integer.parseInt(thisVM.getVmNetworkType()));
			//
			ops.setMsg("正在保存vBox设置");
			session = VBoxConfig.sqlSessionFactory.openSession();
			mapper = session.getMapper(VMMapper.class);
			thisVM.setVmInitFlag("Y");
			mapper.updateByPrimaryKeySelective(thisVM);
			//
			session.commit();
			// restart vm
			ops.setMsg("正在重新开启vBox");
			vm.powerOn();
			ops.setMsg("vBox正在重新启动，请稍等其可用状态。");
			logger.debug("Finished");
			ops.setRetval(0);
		} catch (Exception e) {
			ops.setMsg("操作失败:" + e.getMessage());
			ops.setRetval(1);
			logger.error("error fixing vbox " + uuid, e);
		} finally {
			AdminTaskManager.getInstance().getThreadlist().remove(sid);
			if (session != null)
				session.close();
		}
	}
}
