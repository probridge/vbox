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
import com.probridge.vbox.vmm.wmi.VirtualMachine.HeartBeat;
import com.probridge.vbox.vmm.wmi.VirtualMachine.VMState;
import com.probridge.vbox.vmm.wmi.WindowsManagementServiceLocator;
import com.probridge.vbox.vmm.wmi.utils.VirtualServiceException;
import com.probridge.vbox.zk.AdminTaskManager;

public class SaveUserSettingsTask extends VMTask {
	private static final Logger logger = LoggerFactory.getLogger(SaveUserSettingsTask.class);
	//
	private Users user;
	private boolean updateStorage;
	private boolean newUser;
	private WindowsManagementServiceLocator wmServiceLocator;

	public SaveUserSettingsTask(String sid, String opid, Users userToSave, boolean newUser, boolean updateStorage) {
		super(sid, opid);
		this.user = userToSave;
		this.newUser = newUser;
		this.updateStorage = updateStorage;
	}

	@Override
	public void run() {
		super.run();
		logger.debug("Starting to save user setting..");
		ops.setMsg("保存用户设置");

		try {
			if (updateStorage) {
				HyperVVMM vmm = HyperVVMM.hypervisors[user.getUserHypervisorId()];
				ops.setMsg("正在检查数据文件状态");
				String vhdFileName = user.getUserVhdName();
				wmServiceLocator = new WindowsManagementServiceLocator(vmm.url);

				boolean fileExists = wmServiceLocator.fileExists(VBoxConfig.dataDrive, VBoxConfig.userDataDirectory,
						vhdFileName.substring(0, vhdFileName.lastIndexOf(".")), "vhd");
				if (!newUser) {
					if (!fileExists)
						throw new VirtualServiceException("数据文件不存在");
					ops.setMsg("正在关闭所有关联的vBox");

					SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
					VMMapper mapper = session.getMapper(VMMapper.class);
					VMExample exp = new VMExample();
					exp.createCriteria().andVmVhdUserFilenameEqualTo(user.getUserVhdName());
					List<VM> relatedVMList = mapper.selectByExample(exp);
					session.close();

					for (VM eachVM : relatedVMList) {
						HyperVVM vm = vmm.getVMInstance(eachVM.getVmId());
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
							throw new VirtualServiceException("无法关闭" + eachVM.getVmId() + "，请联系我们");
						ops.setMsg("vBox:" + eachVM.getVmTitle() + "已关闭");
					}
					ops.setMsg("所有关联的vBox均已关闭");
					logger.debug("Powered off now");
					//
					ops.setMsg("正在修改用户数据空间配置");
					vmm.getServiceLocator()
							.getImageManagementService()
							.expandVirtualHardDisk(
									VBoxConfig.dataDrive + VBoxConfig.userDataDirectory + user.getUserVhdName(),
									user.getUserVhdQuota() * 1073741824L);
				} else {
					if (fileExists)
						throw new VirtualServiceException("该数据文件已经存在");
					ops.setMsg("正在创建用户数据空间模板");
					//
					logger.debug("copying file " + VBoxConfig.vmUserVhdTemplateName + " to " + user.getUserVhdName());
					wmServiceLocator.copyFile(
							VBoxConfig.dataDrive,
							VBoxConfig.userDataDirectory,
							VBoxConfig.vmUserVhdTemplateName.substring(0,
									VBoxConfig.vmUserVhdTemplateName.lastIndexOf(".")), "vhd", user.getUserVhdName());
					logger.debug("copying finished!");
					//
					ops.setMsg("正在分配空间");
					vmm.getServiceLocator()
							.getImageManagementService()
							.expandVirtualHardDisk(
									VBoxConfig.dataDrive + VBoxConfig.userDataDirectory + user.getUserVhdName(),
									user.getUserVhdQuota() * 1073741824L);
				}
			}
			ops.setMsg("正在保存设置");
			SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
			UsersMapper mapper = session.getMapper(UsersMapper.class);
			if (newUser) {
				mapper.insert(user);
			} else {
				mapper.updateByPrimaryKey(user);
			}
			session.commit();
			session.close();
			ops.setMsg("操作完成");
			logger.debug("Finished");
			ops.setRetval(0);
		} catch (Exception e) {
			ops.setMsg("操作失败:" + e.getMessage());
			ops.setRetval(1);
			logger.error("error saving user " + user.getUserName(), e);
		} finally {
			if (wmServiceLocator != null)
				wmServiceLocator.destroySession();
			AdminTaskManager.getInstance().getThreadlist().remove(sid);
		}
	}
}
