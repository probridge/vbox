package com.probridge.vbox.servlet;

import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;
import org.jinterop.dcom.common.JIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.UsersMapper;
import com.probridge.vbox.dao.VMMapper;
import com.probridge.vbox.model.Users;
import com.probridge.vbox.model.VM;
import com.probridge.vbox.utils.Utility;
import com.probridge.vbox.vmm.wmi.HyperVVM;
import com.probridge.vbox.vmm.wmi.HyperVVMM;
import com.probridge.vbox.vmm.wmi.WindowsManagementServiceLocator;
import com.probridge.vbox.vmm.wmi.utils.VirtualServiceException;
import com.probridge.vbox.zk.AdminTaskManager;

public class PreAppoveTask extends VMTask {
	private static final Logger logger = LoggerFactory.getLogger(PreAppoveTask.class);
	//
	private HashMap<String, Object> cmdMap;
	private WindowsManagementServiceLocator wmServiceLocator;

	public PreAppoveTask(String sid, String opid, HashMap<String, Object> cmdMap) {
		super(sid, opid);
		this.cmdMap = cmdMap;
	}

	@Override
	public void run() {
		super.run();
		logger.debug("Starting to process Preapprove");
		ops.setMsg("开始创建资源");
		SqlSession session = null;
		
		String identity = null;
		if (cmdMap.get("identity") != null)
			identity = (String) cmdMap.get("identity");

		int hypervisorId = 0;
		if (cmdMap.get("hypervisor") != null)
			hypervisorId = (int) cmdMap.get("hypervisor");

		int personalVhd = 0;
		if (cmdMap.get("personalVhd") != null)
			personalVhd = (int) cmdMap.get("personalVhd");

		VM userVBox = null;
		if (cmdMap.get("userVBox") != null)
			userVBox = (VM) cmdMap.get("userVBox");

		VM courseVBox = null;
		if (cmdMap.get("courseVBox") != null)
			courseVBox = (VM) cmdMap.get("courseVBox");

		try {
			HyperVVMM vmm = HyperVVMM.hypervisors[hypervisorId];
			String userVhdFileName = Utility.generateUserVhdFileName(identity);
			if (personalVhd > 0) {
				ops.setMsg("正在创建用户存储空间模板");
				wmServiceLocator = new WindowsManagementServiceLocator(vmm.url);
				//
				logger.debug("copying file " + VBoxConfig.vmUserVhdTemplateName + " to " + userVhdFileName);
				wmServiceLocator
						.copyFile(
								VBoxConfig.dataDrive,
								VBoxConfig.userDataDirectory,
								VBoxConfig.vmUserVhdTemplateName.substring(0,
										VBoxConfig.vmUserVhdTemplateName.lastIndexOf(".")), "vhd", userVhdFileName);
				logger.debug("copying finished!");
				//
				ops.setMsg("正在分配空间");
				vmm.getServiceLocator()
						.getImageManagementService()
						.expandVirtualHardDisk(VBoxConfig.dataDrive + VBoxConfig.userDataDirectory + userVhdFileName,
								personalVhd * 1073741824L);
				// save to DB
				Users newUser = new Users();
				newUser.setUserName(identity);
				newUser.setUserVhdQuota(personalVhd);
				newUser.setUserVhdName(userVhdFileName);
				newUser.setUserHypervisorId(hypervisorId);
				//
				session = VBoxConfig.sqlSessionFactory.openSession();
				UsersMapper umapper = session.getMapper(UsersMapper.class);
				umapper.updateByPrimaryKeySelective(newUser);
				session.commit();
				ops.setMsg("分配用户存储空间完成");
			}

			if (userVBox != null) {
				ops.setMsg("正在为您创建个人vBox");
				userVBox.setVmVhdUserFilename(userVhdFileName);
				setupVM(vmm, userVBox, "您的个人vBox");
				ops.setMsg("您的个人vBox创建成功");
			}

			if (courseVBox != null) {
				ops.setMsg("正在为您创建课程vBox");
				courseVBox.setVmVhdUserFilename(userVhdFileName);
				setupVM(vmm, courseVBox, "您的课程vBox");
				ops.setMsg("您的课程vBox创建成功");
			}
			ops.setMsg("所有操作成功");
			logger.debug("Finished");
			ops.setRetval(0);
		} catch (Exception e) {
			ops.setMsg("操作失败:" + e.getMessage());
			ops.setRetval(1);
			logger.error("error preapproving task" + e);
		} finally {
			AdminTaskManager.getInstance().getThreadlist().remove(sid);
			if (session != null)
				session.close();
			if (wmServiceLocator != null)
				wmServiceLocator.destroySession();
		}
	}

	private void setupVM(HyperVVMM vmm, VM configSpec, String prefix) throws VirtualServiceException, JIException {
		ops.setMsg(prefix + "：初始化vBox");
		HyperVVM thisVM = null;
		if ("--".equals(configSpec.getVmId())) {
			HyperVVM vmTemplate = vmm.getVMTemplate();
			thisVM = vmTemplate.clone(configSpec.getVmName());
			configSpec.setVmId(thisVM.getID());
			configSpec.setVmStatus(0);
		}
		//
		if ("0".equals(configSpec.getVmVhdGmType()))
			configSpec.setVmVhdGmFilename(VBoxConfig.gmVhdPrefix + configSpec.getVmName() + "_"
					+ System.currentTimeMillis() + ".vhd");
		else if ("1".equals(configSpec.getVmVhdGmType()))
			configSpec.setVmVhdGmFilename(VBoxConfig.gmVhdPrefix + configSpec.getVmName() + "_"
					+ System.currentTimeMillis() + "_Clone.vhd");
		else if ("2".equals(configSpec.getVmVhdGmType()))
			configSpec.setVmVhdGmFilename(configSpec.getVmVhdGmImage());
		//
		ops.setMsg(prefix + "：分配处理资源");
		thisVM.modifyConfiguration(configSpec.getVmCores(), configSpec.getVmMemory(), configSpec.getVmMemory());
		ops.setMsg(prefix + "：分配存储");
		thisVM.modifyStorage(configSpec.getVmVhdGmImage(), Integer.parseInt(configSpec.getVmVhdGmType()),
				configSpec.getVmVhdGmFilename(), configSpec.getVmVhdUserFilename());
		ops.setMsg(prefix + "：配置网络连接");
		thisVM.modifyNetwork(Integer.parseInt(configSpec.getVmNetworkType()));
		//
		ops.setMsg(prefix + "：保存设置");
		//
		configSpec.setVmInitFlag("Y");
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		VMMapper mapper = session.getMapper(VMMapper.class);
		mapper.insertSelective(configSpec);
		//
		session.commit();
		session.close();
		ops.setMsg(prefix + "：保存完成");
	}
}
