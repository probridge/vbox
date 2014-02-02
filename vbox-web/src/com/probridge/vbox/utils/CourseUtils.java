package com.probridge.vbox.utils;

import java.util.Date;

import org.apache.ibatis.session.SqlSession;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.CourseMapper;
import com.probridge.vbox.dao.VMMapper;
import com.probridge.vbox.dao.VboxRequestMapper;
import com.probridge.vbox.model.Course;
import com.probridge.vbox.model.VMExample;
import com.probridge.vbox.model.VboxRequestExample;

public class CourseUtils {
	public static boolean isCourseValid(String courseID) {
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		CourseMapper mapper = session.getMapper(CourseMapper.class);
		Course thisCourse = mapper.selectByPrimaryKey(courseID);
		session.close();
		//
		if (thisCourse == null)
			return false;
		if (thisCourse.getCourseExpiration() == null)
			return true;
		else if (thisCourse.getCourseExpiration().before(new Date()))
			return false;
		return true;
	}

	public static boolean isCourseAlreadyApplied(String identity, String courseID) {
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		VboxRequestMapper mapper = session.getMapper(VboxRequestMapper.class);
		VboxRequestExample exp = new VboxRequestExample();
		exp.createCriteria().andVboxRequestOwnerEqualTo(identity).andVboxRequestCodeEqualTo(courseID);
		int previousRequests = mapper.countByExample(exp);
		//
		VMMapper VMmapper = session.getMapper(VMMapper.class);
		VMExample exp2 = new VMExample();
		exp2.createCriteria().andVmOwnerEqualTo(identity).andVmCourseCodeEqualTo(courseID);
		int vms = VMmapper.countByExample(exp2);
		session.close();
		if (previousRequests > 0 || vms > 0)
			return true;
		else
			return false;
	}
}
