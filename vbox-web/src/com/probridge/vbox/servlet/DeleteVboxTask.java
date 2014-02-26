package com.probridge.vbox.servlet;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.VMMapper;
import com.probridge.vbox.vmm.wmi.HyperVVM;
import com.probridge.vbox.vmm.wmi.HyperVVMM;
import com.probridge.vbox.vmm.wmi.VirtualMachine.HeartBeat;
import com.probridge.vbox.vmm.wmi.VirtualMachine.VMState;
import com.probridge.vbox.vmm.wmi.utils.VirtualServiceException;
import com.probridge.vbox.zk.AdminTaskManager;

public class DeleteVboxTask extends VMTask {
	private static final Logger logger = LoggerFactory.getLogger(DeleteVboxTask.class);
	//
	private String uuid;

	public DeleteVboxTask(String sid, String opid, String uuid) {
		super(sid, opid);
		this.uuid = uuid;
	}

	@Override
	public void run() {
		super.run();
		logger.debug("Starting to process delete vbox..");
		ops.setMsg("开始删除vBox");
		try {
			HyperVVM vm = HyperVVMM.locateVM(uuid);
			ops.setMsg("正在关闭vBox");
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
			if (!vm.waitFor(VMState.PoweredOff))
				throw new VirtualServiceException("无法关闭vBox，请联系我们");
			logger.debug("Powered off now");
			//
			ops.setMsg("正在删除vBox");
			vm.destroy(); //
			logger.debug("vBox Deleted");
			ops.setMsg("正在保存设置");

			SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
			VMMapper mapper = session.getMapper(VMMapper.class);
			mapper.deleteByPrimaryKey(uuid);
			session.commit();
			session.close();
			//
			ops.setMsg("vBox已经删除");
			logger.debug("Finished");
			ops.setRetval(0);
		} catch (Exception e) {
			ops.setMsg("操作失败:" + e.getMessage());
			ops.setRetval(1);
			logger.error("error deleting vbox " + uuid, e);
		} finally {
			AdminTaskManager.getInstance().getThreadlist().remove(sid);
		}
	}
}
