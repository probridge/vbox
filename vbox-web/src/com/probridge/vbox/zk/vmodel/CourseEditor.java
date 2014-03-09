package com.probridge.vbox.zk.vmodel;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;
import org.zkoss.bind.Form;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.CourseMapper;
import com.probridge.vbox.dao.GMImageMapper;
import com.probridge.vbox.model.Course;
import com.probridge.vbox.model.GMImage;
import com.probridge.vbox.model.GMImageExample;
import com.probridge.vbox.servlet.SaveCourseTask;
import com.probridge.vbox.utils.Utility;

public class CourseEditor extends ProgressViewModel {
	private Course course;

	private ArrayList<GMImage> imageList;
	private GMImage selectedImage = null;
	private boolean defaultCores = false;
	private boolean defaultMemory = false;
	private VBoxForm form = new VBoxForm();

	@Init
	public void init() {
		course = (Course) _execution.getArg().get("course");
		globalCommandName = "reloadCourseList";
		//
		defaultCores = (course.getCourseVmCores() == null);
		defaultMemory = (course.getCourseVmMemory() == null);
		//
		if (Utility.isEmptyOrNull(course.getCourseVmNetwork()))
			course.setCourseVmNetwork("");
		//
		SqlSession session = null;
		try {
			session = VBoxConfig.sqlSessionFactory.openSession();
			GMImageMapper iMapper = session.getMapper(GMImageMapper.class);
			GMImageExample iExp = new GMImageExample();
			iExp.createCriteria().andGmImageLockEqualTo("0");
			imageList = new ArrayList<GMImage>(iMapper.selectByExample(iExp));
		} catch (Exception e) {
		} finally {
			if (session != null)
				session.close();
		}
		//
		for (GMImage eachImage : imageList)
			if (eachImage.getGmImageFilename().equals(course.getCourseVmGoldenMaster()))
				selectedImage = eachImage;
	}

	@NotifyChange("course")
	@Command
	public void generateCourseId() {
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		String courseId = null;
		while (true) {
			String seed = String.valueOf(System.currentTimeMillis());
			courseId = seed.substring(seed.length() - 6);
			CourseMapper mapper = session.getMapper(CourseMapper.class);
			if (mapper.selectByPrimaryKey(courseId) == null)
				break;
		}
		session.close();
		course.setCourseId(courseId);
	}

	@Command
	@NotifyChange({ "defaultCores", "defaultMemory", "form" })
	public void reload() {
		form.setField("courseVmCores", defaultCores ? null : VBoxConfig.defaultCPUCores);
		form.setField("courseVmMemory", defaultMemory ? null : VBoxConfig.defaultMemory);
	}

	@Command
	@NotifyChange("form")
	public void updategm() {
		form.setField("courseVmGoldenMaster", selectedImage.getGmImageFilename());
		form.addSaveFieldName("courseVmGoldenMaster");
	}

	@Command
	@NotifyChange({ "progress", "progressMsg", "started", "running", "closeBtnLabel" })
	public void save() {
		if ("".equals(course.getCourseVmNetwork()))
			course.setCourseVmNetwork(null);
		SaveCourseTask t = new SaveCourseTask(getSid(), getOpId(), course);
		submit(t);
		return;
	}

	public ArrayList<GMImage> getImageList() {
		return imageList;
	}

	public void setImageList(ArrayList<GMImage> imageList) {
		this.imageList = imageList;
	}

	public Form getForm() {
		return form;
	}

	public GMImage getSelectedImage() {
		return selectedImage;
	}

	public void setSelectedImage(GMImage selectedImage) {
		this.selectedImage = selectedImage;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public boolean isDefaultCores() {
		return defaultCores;
	}

	public void setDefaultCores(boolean defaultCores) {
		this.defaultCores = defaultCores;
	}

	public boolean isDefaultMemory() {
		return defaultMemory;
	}

	public void setDefaultMemory(boolean defaultMemory) {
		this.defaultMemory = defaultMemory;
	}
}
