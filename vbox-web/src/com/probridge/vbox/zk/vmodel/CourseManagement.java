package com.probridge.vbox.zk.vmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.CourseMapper;
import com.probridge.vbox.model.Course;
import com.probridge.vbox.model.CourseExample;

public class CourseManagement {

	private Course selectedCourse;

	public Course getSelectedCourse() {
		return selectedCourse;
	}

	public void setSelectedCourse(Course selectedCourse) {
		this.selectedCourse = selectedCourse;
	}

	public List<Course> getCourseList() {
		return courseList;
	}

	public void setCourseList(List<Course> courseList) {
		this.courseList = courseList;
	}

	private List<Course> courseList = new ArrayList<Course>();
	private boolean autoRefresh = true;

	@WireVariable
	private Page _page;

	@Init
	public void init() {
		reloadCourseList();
	}

	@GlobalCommand
	@NotifyChange({ "courseList", "selectedCourse" })
	public void reloadCourseList() {
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		try {
			CourseMapper mapper = session.getMapper(CourseMapper.class);
			CourseExample exp = new CourseExample();
			courseList.clear();
			courseList.addAll(mapper.selectByExample(exp));
			for (Course eachCourse : courseList)
				if (selectedCourse != null && eachCourse.getCourseId().equals(selectedCourse.getCourseId()))
					selectedCourse = eachCourse;
			//
		} finally {
			session.close();
		}
	}

	@Command
	public void editPreApprovalList() {
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("course", selectedCourse);
		Window win = (Window) Executions.createComponents("/management/course_approval_list_editor_new.zul", null, arg);
		win.setPage(_page);
	}

	@Command
	@NotifyChange("selectedCourse")
	public void deleteCourse() {
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("course", selectedCourse);
		Window win = (Window) Executions.createComponents("/management/course_delete_new.zul", null, arg);
		win.setPage(_page);
		selectedCourse = null;
	}

	@Command
	public void editCourse() {
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("course", selectedCourse);
		Window win = (Window) Executions.createComponents("/management/course_editor_new.zul", null, arg);
		win.setPage(_page);
	}

	@Command
	public void createCourse() {
		Course thisCourse = new Course();
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("course", thisCourse);
		Window win = (Window) Executions.createComponents("/management/course_editor_new.zul", null, arg);
		win.setPage(_page);
	}

	@Command
	@NotifyChange({ "courseList", "selectedCourse" })
	public void refresh() {
		reloadCourseList();
	}

	public boolean isAutoRefresh() {
		return autoRefresh;
	}

	public void setAutoRefresh(boolean autoRefresh) {
		this.autoRefresh = autoRefresh;
	}
}
