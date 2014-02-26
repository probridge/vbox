package com.probridge.vbox.servlet;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.UsersMapper;
import com.probridge.vbox.dao.VMMapper;
import com.probridge.vbox.model.Users;
import com.probridge.vbox.model.VM;
import com.probridge.vbox.model.VMExample;
import com.probridge.vbox.vmm.wmi.HyperVVM;
import com.probridge.vbox.vmm.wmi.HyperVVMM;
import com.probridge.vbox.vmm.wmi.VirtualMachine.VMState;
import com.probridge.vbox.vmm.wmi.WindowsManagementServiceLocator;
import com.probridge.vbox.vmm.wmi.utils.VirtualServiceException;
import com.probridge.vbox.zk.AdminTaskManager;

public class DeleteUserTask extends VMTask {
	private static final Logger logger = LoggerFactory.getLogger(DeleteUserTask.class);
	//
	private Users user;
	private WindowsManagementServiceLocator wmServiceLocator;

	public DeleteUserTask(String sid, String opid, Users userToDelete) {
		super(sid, opid);
		this.user = userToDelete;
	}

	@Override
	public void run() {
		super.run();
		logger.debug("Starting to delete user..");
		ops.setMsg("删除用户");

		try {
			ops.setMsg("准备关闭和删除所有关联的vBox");

			SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
			VMMapper mapper = session.getMapper(VMMapper.class);
			VMExample exp = new VMExample();
			exp.createCriteria().andVmOwnerEqualTo(user.getUserName());
			List<VM> relatedVMList = mapper.selectByExample(exp);
			//

			for (VM eachVM : relatedVMList) {
				ops.setMsg("正在停止：" + eachVM.getVmName());
				logger.debug("Stopping " + eachVM.getVmId());
				HyperVVM vm = HyperVVMM.locateVM(eachVM.getVmId());
				vm.powerOff();
				boolean stateReached = vm.waitFor(VMState.PoweredOff);
				if (!stateReached)
					throw new VirtualServiceException("无法关闭" + eachVM.getVmId() + "，请联系我们");
				ops.setMsg("正在删除：" + eachVM.getVmName());
				logger.debug("Deleting " + eachVM.getVmId());
				vm.destroy();
				VMMapper mapper2 = session.getMapper(VMMapper.class);
				mapper2.deleteByPrimaryKey(eachVM.getVmId());
				session.commit();
			}
			ops.setMsg("所有关联的vBox均已删除");
			logger.debug("All vBox Deleted for " + user.getUserName());

			ops.setMsg("正在检查数据文件状态");
			String vhdFileName = user.getUserVhdName();
			wmServiceLocator = new WindowsManagementServiceLocator(
					HyperVVMM.hypervisors[user.getUserHypervisorId()].url);

			boolean fileExists = wmServiceLocator.fileExists(VBoxConfig.dataDrive, VBoxConfig.userDataDirectory,
					vhdFileName.substring(0, vhdFileName.lastIndexOf(".")), "vhd");

			if (!fileExists) {
				logger.debug("Personal VHD doesn't exist");
				throw new VirtualServiceException("数据文件不存在");
			}
			//
			ops.setMsg("正在删除用户数据");
			logger.debug("Deleting file " + vhdFileName);
			wmServiceLocator.deleteFile(VBoxConfig.dataDrive, VBoxConfig.userDataDirectory,
					vhdFileName.substring(0, vhdFileName.lastIndexOf(".")), "vhd");

			ops.setMsg("正在保存设置");
			UsersMapper mapper3 = session.getMapper(UsersMapper.class);
			mapper3.deleteByPrimaryKey(user.getUserName());
			session.commit();
			session.close();
			ops.setMsg("操作完成");
			logger.debug("Finished");
			ops.setRetval(0);
		} catch (Exception e) {
			ops.setMsg("操作失败:" + e.getMessage());
			ops.setRetval(1);
			logger.error("error while deleting user " + user.getUserName(), e);
		} finally {
			if (wmServiceLocator != null)
				wmServiceLocator.destroySession();
			AdminTaskManager.getInstance().getThreadlist().remove(sid);
		}
	}
}
