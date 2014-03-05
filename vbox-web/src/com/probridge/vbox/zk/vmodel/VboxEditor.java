package com.probridge.vbox.zk.vmodel;

import java.util.ArrayList;
import java.util.Date;

import org.apache.ibatis.session.SqlSession;
import org.zkoss.bind.Form;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.CourseMapper;
import com.probridge.vbox.dao.GMImageMapper;
import com.probridge.vbox.dao.UsersMapper;
import com.probridge.vbox.model.Course;
import com.probridge.vbox.model.CourseExample;
import com.probridge.vbox.model.GMImage;
import com.probridge.vbox.model.GMImageExample;
import com.probridge.vbox.model.Users;
import com.probridge.vbox.model.UsersExample;
import com.probridge.vbox.model.VM;
import com.probridge.vbox.servlet.SaveVMSettingsTask;
import com.probridge.vbox.utils.Utility;
import com.probridge.vbox.vmm.wmi.HyperVVMM;

public class VboxEditor extends ProgressViewModel {
	private VM vm;

	private ArrayList<Users> userList;
	private ArrayList<GMImage> imageList;
	private ArrayList<Course> courseList;

	private Users selectedUser = null;
	private GMImage selectedImage = null;
	private Course selectedCourse = null;

	private VBoxForm form = new VBoxForm();

	private String constrainHypervisor = "min 0 max " + (HyperVVMM.hypervisors.length - 1);

	@Init
	public void init() {
		vm = (VM) _execution.getArg().get("vm");
		globalCommandName = "reloadVmList";
		//
		SqlSession session = null;
		try {
			session = VBoxConfig.sqlSessionFactory.openSession();

			UsersMapper uMapper = session.getMapper(UsersMapper.class);
			UsersExample uExp = new UsersExample();
			uExp.createCriteria().andUserExpirationGreaterThanOrEqualTo(new Date());
			uExp.or(uExp.createCriteria().andUserExpirationIsNull());
			userList = new ArrayList<Users>(uMapper.selectByExample(uExp));

			GMImageMapper iMapper = session.getMapper(GMImageMapper.class);
			GMImageExample iExp = new GMImageExample();
			iExp.createCriteria().andGmImageLockEqualTo("0");
			imageList = new ArrayList<GMImage>(iMapper.selectByExample(iExp));

			CourseMapper cMapper = session.getMapper(CourseMapper.class);
			CourseExample cExp = new CourseExample();
			cExp.createCriteria().andCourseExpirationGreaterThanOrEqualTo(new Date());
			cExp.or(cExp.createCriteria().andCourseExpirationIsNull());
			courseList = new ArrayList<Course>(cMapper.selectByExample(cExp));
		} catch (Exception e) {

		} finally {
			if (session != null)
				session.close();
		}
		//
		for (Users eachUser : userList)
			if (eachUser.getUserName().equals(vm.getVmOwner()))
				selectedUser = eachUser;
		//
		for (Course eachCourse : courseList)
			if (eachCourse.getCourseId().equals(vm.getVmCourseCode()))
				selectedCourse = eachCourse;
		//
		for (GMImage eachImage : imageList)
			if (eachImage.getGmImageFilename().equals(vm.getVmVhdGmImage()))
				selectedImage = eachImage;
	}

	@Command
	@NotifyChange("form")
	public void reload() {
		if (selectedCourse != null) {
			form.setField("vmCourseCode", selectedCourse.getCourseId());
			form.setField("vmVhdGmImage", selectedCourse.getCourseVmGoldenMaster());
		}
		//
		if (selectedUser != null) {
			form.setField("vmOwner", selectedUser.getUserName());
			form.setField("vmVhdUserFilename", selectedUser.getUserVhdName());
			form.setField("vmHypervisorId", selectedUser.getUserHypervisorId());
		}
		//
		if ("0".equals(form.getField("vmType")))
			form.setField(
					"vmName",
					VBoxConfig.CourseVMName
							+ Utility.normalized(form.getField("vmCourseCode") + "_" + form.getField("vmOwner")));
		else if ("1".equals(form.getField("vmType")))
			form.setField("vmName",
					VBoxConfig.PersonalVMName + Utility.normalized(String.valueOf(form.getField("vmOwner"))));
		else if ("2".equals(form.getField("vmType")))
			form.setField("vmName",
					VBoxConfig.MaintVMName + Utility.normalized(String.valueOf(form.getField("vmOwner"))));
		//
		form.addSaveFieldName("vmName");
		form.addSaveFieldName("vmVhdUserFilename");
		form.addSaveFieldName("vmHypervisorId");
	}

	@Command
	@NotifyChange("form")
	public void updategm() {
		form.setField("vmVhdGmImage", selectedImage.getGmImageFilename());
		//
		form.addSaveFieldName("vmVhdGmImage");
	}

	@Command
	@NotifyChange({ "progress", "progressMsg", "started", "running" })
	public void save() {
		boolean vmConfigDirty = form.isFieldDirty("vmCores") || form.isFieldDirty("vmMemory")
				|| form.isFieldDirty("vmName") || form.isFieldDirty("vmNetworkType");
		boolean vmStorageConfigDirty = form.isFieldDirty("vmType") || form.isFieldDirty("vmVhdGmType")
				|| form.isFieldDirty("vmOwner") || form.isFieldDirty("vmVhdGmImage");
		//
		SaveVMSettingsTask t = new SaveVMSettingsTask(getSid(), getOpId(), vm, vmConfigDirty, vmStorageConfigDirty);
		submit(t);
		return;
	}

	public VM getVm() {
		return vm;
	}

	public void setVm(VM vm) {
		this.vm = vm;
	}

	public ArrayList<Users> getUserList() {
		return userList;
	}

	public void setUserList(ArrayList<Users> userList) {
		this.userList = userList;
	}

	public ArrayList<GMImage> getImageList() {
		return imageList;
	}

	public void setImageList(ArrayList<GMImage> imageList) {
		this.imageList = imageList;
	}

	public ArrayList<Course> getCourseList() {
		return courseList;
	}

	public void setCourseList(ArrayList<Course> courseList) {
		this.courseList = courseList;
	}

	public String getConstrainHypervisor() {
		return constrainHypervisor;
	}

	public void setConstrainHypervisor(String constrainHypervisor) {
		this.constrainHypervisor = constrainHypervisor;
	}

	public Form getForm() {
		return form;
	}

	public Users getSelectedUser() {
		return selectedUser;
	}

	public void setSelectedUser(Users selectedUser) {
		this.selectedUser = selectedUser;
	}

	public GMImage getSelectedImage() {
		return selectedImage;
	}

	public void setSelectedImage(GMImage selectedImage) {
		this.selectedImage = selectedImage;
	}

	public Course getSelectedCourse() {
		return selectedCourse;
	}

	public void setSelectedCourse(Course selectedCourse) {
		this.selectedCourse = selectedCourse;
	}
}
