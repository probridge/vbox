package com.probridge.vbox.zk;

import org.apache.ibatis.session.SqlSession;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zul.A;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Spinner;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.CourseMapper;
import com.probridge.vbox.dao.GMImageMapper;
import com.probridge.vbox.model.Course;
import com.probridge.vbox.model.GMImage;
import com.probridge.vbox.model.GMImageExample;
import com.probridge.vbox.utils.Utility;

public class CourseEditorController extends SelectorComposer<Window> {

	private static final long serialVersionUID = -6972782398962109854L;

	@WireVariable
	Execution _execution;

	@Wire
	Button btnCancel, btnSave;

	@Wire
	Bandbox vmGoldenImage;

	@Wire
	Listbox lbImageList;

	@Wire
	Textbox tbCourseId;

	@Wire
	Spinner vmCores, vmMemory;

	@Wire
	Radiogroup vmNetworkType;

	@Wire
	Radio vmDefaultNet;

	@Wire
	Checkbox defaultCores, defaultMemory;

	@Wire
	A generateCourseId;

	private CourseManagementController parentController;

	private Course course;

	public Course getCourse() {
		return course;
	}

	private ListModelList<GMImage> imageList;

	public ListModelList<GMImage> getImageList() {
		if (imageList == null) {
			SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
			try {
				GMImageMapper mapper = session.getMapper(GMImageMapper.class);
				GMImageExample exp = new GMImageExample();
				exp.createCriteria().andGmImageLockEqualTo("0");
				imageList = new ListModelList<GMImage>();
				imageList.addAll(mapper.selectByExample(exp));
				//
			} finally {
				session.close();
			}
		}
		return imageList;
	}

	@Listen("onSelect = listbox#lbImageList")
	public void selectImage(Event e) {
		course.setCourseVmGoldenMaster((((GMImage) lbImageList.getModel().getElementAt(lbImageList.getSelectedIndex()))
				.getGmImageFilename()));
		AnnotateDataBinder binder = (AnnotateDataBinder) getSelf().getAttribute("binder");
		binder.loadComponent(vmGoldenImage);
		vmGoldenImage.close();
	}

	@Listen("onClick = #generateCourseId")
	public void generateCourseId(Event e) {
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
		AnnotateDataBinder binder = (AnnotateDataBinder) getSelf().getAttribute("binder");
		binder.loadComponent(tbCourseId);
		vmGoldenImage.close();
	}

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		course = (Course) _execution.getArg().get("course");
		parentController = ((CourseManagementController) _execution.getArg().get("parentController"));
		if (Utility.isEmptyOrNull(course.getCourseId())) {
			tbCourseId.setReadonly(false);
			generateCourseId.setVisible(true);
		}
		if (course.getCourseVmCores() == null) {
			defaultCores.setChecked(true);
			course.setCourseVmCores(0);
			vmCores.setDisabled(true);
		}
		if (course.getCourseVmMemory() == null) {
			defaultMemory.setChecked(true);
			course.setCourseVmMemory(0);
			vmMemory.setDisabled(true);
		}
		if (Utility.isEmptyOrNull(course.getCourseVmNetwork()))
			course.setCourseVmNetwork("");
	}

	@Listen("onCheck = #defaultCores")
	public void resetCore(CheckEvent e) {
		vmCores.setDisabled(e.isChecked());
	}

	@Listen("onCheck = #defaultMemory")
	public void resetMemory(CheckEvent e) {
		vmMemory.setDisabled(e.isChecked());
	}

	@Listen("onClick = button#btnCancel")
	public void closePanel() {
		parentController.reload();
		getSelf().getParent().removeChild(getSelf());
	}

	@Listen("onClick = button#btnSave")
	public void updateCourse(Event event) {
		AnnotateDataBinder binder = (AnnotateDataBinder) getSelf().getAttribute("binder");
		binder.saveAll();
		if (vmCores.isDisabled())
			course.setCourseVmCores(null);
		if (vmMemory.isDisabled())
			course.setCourseVmMemory(null);
		if (vmDefaultNet.isChecked())
			course.setCourseVmNetwork(null);
		saveSettings();
		closePanel();
	}

	private void saveSettings() {
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		CourseMapper mapper = session.getMapper(CourseMapper.class);
		if (mapper.updateByPrimaryKey(course) == 0)
			mapper.insert(course);
		session.commit();
		session.close();
	}
}
