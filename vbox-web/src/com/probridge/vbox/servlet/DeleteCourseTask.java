package com.probridge.vbox.servlet;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.CourseMapper;
import com.probridge.vbox.model.Course;
import com.probridge.vbox.zk.AdminTaskManager;

public class DeleteCourseTask extends VMTask {
	private static final Logger logger = LoggerFactory.getLogger(DeleteCourseTask.class);
	//
	private Course course;

	public DeleteCourseTask(String sid, String opid, Course course) {
		super(sid, opid);
		this.course = course;
	}

	@Override
	public void run() {
		super.run();
		logger.debug("Starting to process course delete task..");
		ops.setMsg("开始删除课程");
		SqlSession session = null;
		try {
			session = VBoxConfig.sqlSessionFactory.openSession();
			CourseMapper mapper = session.getMapper(CourseMapper.class);
			mapper.deleteByPrimaryKey(course.getCourseId());
			session.commit();
			//
			ops.setMsg("删除成功");
			logger.debug("Finished");
			ops.setRetval(0);
		} catch (Exception e) {
			ops.setMsg("课程删除失败:" + e.getMessage());
			ops.setRetval(1);
			logger.error("error deleting course", e);
		} finally {
			if (session != null)
				session.close();
			AdminTaskManager.getInstance().getThreadlist().remove(sid);
		}
	}
}
