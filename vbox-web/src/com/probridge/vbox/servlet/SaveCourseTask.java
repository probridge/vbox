package com.probridge.vbox.servlet;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.CourseMapper;
import com.probridge.vbox.model.Course;
import com.probridge.vbox.zk.AdminTaskManager;

public class SaveCourseTask extends VMTask {
	private static final Logger logger = LoggerFactory.getLogger(SaveCourseTask.class);
	//
	private Course course;

	public SaveCourseTask(String sid, String opid, Course course) {
		super(sid, opid);
		this.course = course;
	}

	@Override
	public void run() {
		super.run();
		logger.debug("Starting to process course save task..");
		ops.setMsg("开始保存课程");
		SqlSession session = null;
		try {
			session = VBoxConfig.sqlSessionFactory.openSession();
			CourseMapper mapper = session.getMapper(CourseMapper.class);
			if (mapper.updateByPrimaryKey(course) == 0)
				mapper.insert(course);
			session.commit();
			//
			ops.setMsg("保存成功");
			logger.debug("Finished");
			ops.setRetval(0);
		} catch (Exception e) {
			ops.setMsg("课程保存失败:" + e.getMessage());
			ops.setRetval(1);
			logger.error("error saving course", e);
		} finally {
			AdminTaskManager.getInstance().getThreadlist().remove(sid);
			if (session!=null)
				session.close();
		}
	}
}
