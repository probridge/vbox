package com.probridge.vbox.servlet;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.GMImageMapper;
import com.probridge.vbox.dao.VMMapper;
import com.probridge.vbox.model.GMImage;
import com.probridge.vbox.model.VMExample;
import com.probridge.vbox.vmm.RepositoryManager;
import com.probridge.vbox.vmm.wmi.utils.VirtualServiceException;
import com.probridge.vbox.zk.AdminTaskManager;

public class DeleteImageTask extends VMTask {
	private static final Logger logger = LoggerFactory.getLogger(DeleteImageTask.class);
	//
	private GMImage image;

	public DeleteImageTask(String sid, String opid, GMImage image) {
		super(sid, opid);
		this.image = image;
	}

	@Override
	public void run() {
		super.run();
		logger.debug("Starting to delete image..");
		ops.setMsg("删除母盘");
		SqlSession session = null;
		try {
			ops.setMsg("准备检查是否有母盘相关的vBox");
			// check VM reference
			session = VBoxConfig.sqlSessionFactory.openSession();
			VMMapper mp = session.getMapper(VMMapper.class);
			VMExample exp = new VMExample();
			exp.createCriteria().andVmVhdGmImageEqualTo(image.getGmImageFilename()).andVmTypeNotEqualTo("2");
			int vmCount = mp.countByExample(exp);
			if (vmCount > 0) {
				logger.debug("Remaining " + vmCount + " vbox(es) of this image. Delete abort.");
				throw new VirtualServiceException("还有" + vmCount + "个vBox正在使用这个母盘，不能删除");
			}
			//
			ops.setMsg("正在删除母盘");
			RepositoryManager.deleteFile(image.getGmImageFilename());
			//
			GMImageMapper mapper = session.getMapper(GMImageMapper.class);
			mapper.deleteByPrimaryKey(image.getGmImageId());
			//
			session.commit();
			//
			ops.setMsg("操作完成");
			logger.debug("Finished");
			ops.setRetval(0);
		} catch (Exception e) {
			ops.setMsg("操作失败:" + e.getMessage());
			ops.setRetval(1);
			logger.error("error while deleting image " + image.getGmImageFilename(), e);
		} finally {
			AdminTaskManager.getInstance().getThreadlist().remove(sid);
			if (session != null)
				session.close();
		}
	}
}
