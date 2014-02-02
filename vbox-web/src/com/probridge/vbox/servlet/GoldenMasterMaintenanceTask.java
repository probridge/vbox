package com.probridge.vbox.servlet;

import java.net.UnknownHostException;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.jinterop.dcom.common.JIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.GMImageMapper;
import com.probridge.vbox.dao.VMMapper;
import com.probridge.vbox.model.GMImage;
import com.probridge.vbox.model.VM;
import com.probridge.vbox.model.VMExample;
import com.probridge.vbox.vmm.RepositoryManager;
import com.probridge.vbox.vmm.wmi.HyperVVM;
import com.probridge.vbox.vmm.wmi.HyperVVMM;
import com.probridge.vbox.vmm.wmi.WindowsManagementServiceLocator;
import com.probridge.vbox.vmm.wmi.VirtualMachine.HeartBeat;
import com.probridge.vbox.vmm.wmi.VirtualMachine.VMState;
import com.probridge.vbox.vmm.wmi.utils.VirtualServiceException;
import com.probridge.vbox.zk.AdminTaskManager;

public class GoldenMasterMaintenanceTask extends VMTask {
	private static final Logger logger = LoggerFactory.getLogger(GoldenMasterMaintenanceTask.class);
	//
	private GMImage image;
	private int mode;

	public GoldenMasterMaintenanceTask(String sid, String opid, GMImage image, int mode) {
		super(sid, opid);
		this.image = image;
		this.mode = mode;
	}

	@Override
	public void run() {
		super.run();
		logger.debug("Start gm maintenance mode = " + mode);
		ops.setMsg("开始母盘维护作业");
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		try {
			//
			VMMapper vmapper = session.getMapper(VMMapper.class);
			VM maintainVM = getMaintenanceVM(vmapper);
			//
			if (maintainVM.getVmHypervisorId() != VBoxConfig.repositoryLocation)
				throw new VirtualServiceException("维护机Hypervisor位置错误，请检查");
			//
			HyperVVM maintainVm = HyperVVMM.locateVM(maintainVM.getVmId());
			//
			if (maintainVm.getState() != VMState.PoweredOff)
				throw new VirtualServiceException("操作前请确保维护机为关机状态，请检查！");

			// get affected VM list
			VMExample exp = new VMExample();
			exp.or().andVmVhdGmImageEqualTo(image.getGmImageFilename()).andVmVhdGmTypeNotEqualTo("2");
			List<VM> affectedVMList = vmapper.selectByExample(exp);
			//
			if (mode == 0) { // maint
				ops.setMsg("正在禁用相关的vBox");
				setVMDisableState(affectedVMList, "1", vmapper);
				session.commit();
				ops.setMsg("正在关闭相关的vBox");
				completeShutdown(affectedVMList);
				ops.setMsg("相关的vBox关闭完成");
				//
				maintainVM.setVmVhdGmImage(image.getGmImageFilename());
				maintainVM.setVmVhdGmType("2");
				maintainVM.setVmVhdGmFilename(image.getGmImageFilename());
				maintainVM.setVmDisabled("0");
				vmapper.updateByPrimaryKey(maintainVM);
				session.commit();
				// set image maintenance mode
				GMImageMapper mapper = session.getMapper(GMImageMapper.class);
				image.setGmImageLock("1");
				mapper.updateByPrimaryKey(image);
				session.commit();
				//
				ops.setMsg("正在挂接母盘到维护vBox");
				maintainVm.modifyStorage(image.getGmImageFilename(), 2, image.getGmImageFilename(),
						maintainVM.getVmVhdUserFilename());
				// Start maintenace VM
				maintainVm.powerOn();
				ops.setMsg("操作完成，维护vBox正在启动");
			} else if (mode == 1) { // ready
				maintainVM.setVmVhdGmImage(VBoxConfig.vmMaintLandingZone);
				maintainVM.setVmVhdGmType("2");
				maintainVM.setVmVhdGmFilename(VBoxConfig.vmMaintLandingZone);
				maintainVM.setVmDisabled("1");
				vmapper.updateByPrimaryKey(maintainVM);
				session.commit();
				//
				ops.setMsg("正在重置维护vBox");
				maintainVm.modifyStorage(VBoxConfig.vmMaintLandingZone, 2, VBoxConfig.vmMaintLandingZone,
						maintainVM.getVmVhdUserFilename());
				//
				ops.setMsg("正在关闭相关的vBox");
				completeShutdown(affectedVMList);
				ops.setMsg("相关的vBox关闭完成");
				//

				ops.setMsg("正在向所有节点同步母盘文件，可能需要较长时间...");
				RepositoryManager.syncFile(image.getGmImageFilename(), ops);
				ops.setMsg("文件同步完成");
				//
				for (VM eachVM : affectedVMList) {
					// Regenerate GM image file
					String existingFileName = eachVM.getVmVhdGmFilename();
					if ("0".equals(eachVM.getVmVhdGmType()))
						eachVM.setVmVhdGmFilename(VBoxConfig.gmVhdPrefix + eachVM.getVmName() + "_"
								+ System.currentTimeMillis() + ".vhd");
					else if ("1".equals(eachVM.getVmVhdGmType()))
						eachVM.setVmVhdGmFilename(VBoxConfig.gmVhdPrefix + eachVM.getVmName() + "_"
								+ System.currentTimeMillis() + "_Clone.vhd");
					//
					eachVM.setVmInitFlag("Y");
					//
					ops.setMsg("正在重置vBox[" + eachVM.getVmTitle() + "]的存储配置");
					HyperVVM vm = HyperVVMM.locateVM(eachVM.getVmId());
					vm.modifyStorage(eachVM.getVmVhdGmImage(), Integer.parseInt(eachVM.getVmVhdGmType()),
							eachVM.getVmVhdGmFilename(), eachVM.getVmVhdUserFilename());
					// Save
					vmapper.updateByPrimaryKey(eachVM);
					//
					WindowsManagementServiceLocator wmServiceLocator = new WindowsManagementServiceLocator(
							vm.parent.url);
					//
					ops.setMsg("正在删除旧数据");
					logger.debug("Deleting file " + existingFileName);
					wmServiceLocator.deleteFile(VBoxConfig.dataDrive, VBoxConfig.userImageDirectory,
							existingFileName.substring(0, existingFileName.lastIndexOf(".")), "vhd");
					wmServiceLocator.destroySession();
				}
				session.commit();
				ops.setMsg("正在启用相关的vBox");
				setVMDisableState(affectedVMList, "0", vmapper);
				session.commit();

				// set image maintenance mode
				GMImageMapper mapper = session.getMapper(GMImageMapper.class);
				image.setGmImageLock("0");
				mapper.updateByPrimaryKey(image);
				session.commit();
				//
				ops.setMsg("操作完成");
			}
			logger.debug("Finished");
			ops.setRetval(0);
		} catch (VirtualServiceException | JIException | InterruptedException | UnknownHostException e) {
			ops.setMsg("操作失败:" + e.getMessage());
			ops.setRetval(1);
			logger.error(
					"error maint/ready the golden master image " + image.getGmImageFilename() + " mode is " + mode, e);
		} finally {
			session.close();
			AdminTaskManager.getInstance().getThreadlist().remove(sid);
		}
	}

	private void setVMDisableState(List<VM> vmList, String disableStatus, VMMapper vmapper) {
		for (VM eachVM : vmList) {
			// set disable bit
			eachVM.setVmDisabled(disableStatus);
			vmapper.updateByPrimaryKey(eachVM);
		}
	}

	private void completeShutdown(List<VM> vmList) throws VirtualServiceException, InterruptedException {
		for (VM eachVM : vmList) {
			// power off
			HyperVVM vm = HyperVVMM.locateVM(eachVM.getVmId());
			ops.setMsg("正在关闭vBox:" + eachVM.getVmTitle());
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
			ops.setMsg("vBox:" + eachVM.getVmTitle() + "已关闭");
		}
		logger.debug("All affected VM powered off now");
	}

	private VM getMaintenanceVM(VMMapper vmapper) throws VirtualServiceException {
		VMExample expMaintenanceVM = new VMExample();
		expMaintenanceVM.createCriteria().andVmTypeEqualTo("2");
		List<VM> maintenanceVMList = vmapper.selectByExample(expMaintenanceVM);
		//
		if (maintenanceVMList.size() == 0)
			throw new VirtualServiceException("没有找到维护vBox，请检查");
		if (maintenanceVMList.size() > 1)
			throw new VirtualServiceException("重复的维护vBox，请检查");
		//
		return maintenanceVMList.get(0);
	}
}
