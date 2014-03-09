package com.probridge.vbox.zk.vmodel;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;

import com.probridge.vbox.model.Course;
import com.probridge.vbox.servlet.DeleteCourseTask;

public class CourseDelete extends ProgressViewModel {
	private Course course;

	@Init
	public void init() {
		course = (Course) _execution.getArg().get("course");
		globalCommandName = "reloadCourseList";
	}

	@Command
	@NotifyChange({ "progress", "progressMsg", "started", "running", "closeBtnLabel" })
	public void delete() {
		DeleteCourseTask t = new DeleteCourseTask(getSid(), getOpId(), course);
		submit(t);
		return;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}
}
