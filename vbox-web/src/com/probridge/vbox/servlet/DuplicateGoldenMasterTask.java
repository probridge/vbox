package com.probridge.vbox.servlet;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.GMImageMapper;
import com.probridge.vbox.model.GMImage;
import com.probridge.vbox.vmm.RepositoryManager;
import com.probridge.vbox.vmm.wmi.HyperVVMM;
import com.probridge.vbox.vmm.wmi.utils.VirtualServiceException;
import com.probridge.vbox.zk.AdminTaskManager;

public class DuplicateGoldenMasterTask extends VMTask {
	private static final Logger logger = LoggerFactory.getLogger(DuplicateGoldenMasterTask.class);
	//
	private GMImage image;
	private String newFileName;
	private String desc;

	public DuplicateGoldenMasterTask(String sid, String opid, GMImage image, String newFilename, String desc) {
		super(sid, opid);
		this.image = image;
		this.newFileName = newFilename;
		this.desc = desc;
	}

	@Override
	public void run() {
		super.run();
		logger.debug("Start gm cloning");
		ops.setMsg("开始母盘克隆作业");
		SqlSession session = null;
		//
		try {
			//
			boolean sourceExists = RepositoryManager.fileExists(VBoxConfig.dataDrive,
					VBoxConfig.goldenMasterImageDirectory,
					image.getGmImageFilename().substring(0, image.getGmImageFilename().lastIndexOf(".")), "vhd");
			//
			if (!sourceExists) {
				logger.debug("Source GM VHD doesn't exist");
				throw new VirtualServiceException("母盘文件不存在");
			}
			boolean targetExists = RepositoryManager.fileExists(VBoxConfig.dataDrive,
					VBoxConfig.goldenMasterImageDirectory, newFileName.substring(0, newFileName.lastIndexOf(".")),
					"vhd");
			if (targetExists) {
				logger.debug("Target GM VHD already exists");
				throw new VirtualServiceException("目标母盘文件已经存在");
			}
			//
			ops.setMsg("正在克隆，请耐心等待");
			logger.debug("copying file " + image.getGmImageFilename() + " to " + newFileName);
			RepositoryManager.clone(image.getGmImageFilename(), newFileName);
			logger.debug("copying finished!");
			//
			ops.setMsg("克隆完成，正在保存");
			//
			image.setGmImageId(null);
			//
			if (HyperVVMM.hypervisors.length > 1)
				image.setGmImageLock("2");
			else
				image.setGmImageLock("0");
			//
			image.setGmImageDescription(desc);
			image.setGmImageFilename(newFileName);
			image.setGmImageCreationDate(null);

			session = VBoxConfig.sqlSessionFactory.openSession();
			GMImageMapper mapper = session.getMapper(GMImageMapper.class);
			mapper.insertSelective(image);
			session.commit();
			//
			ops.setMsg("操作完成");
			logger.debug("Finished");
			ops.setRetval(0);
		} catch (Exception e) {
			ops.setMsg("操作失败:" + e.getMessage());
			ops.setRetval(1);
			logger.error("error cloning images to " + newFileName, e);
		} finally {
			AdminTaskManager.getInstance().getThreadlist().remove(sid);
			if (session != null)
				session.close();
		}
	}
}
