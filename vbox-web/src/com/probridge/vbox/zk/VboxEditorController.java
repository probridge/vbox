package com.probridge.vbox.zk;

import java.util.Date;

import org.apache.ibatis.session.SqlSession;
import org.apache.shiro.SecurityUtils;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.Spinner;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Window;

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
import com.probridge.vbox.servlet.OpStatus;
import com.probridge.vbox.servlet.SaveVMSettingsTask;
import com.probridge.vbox.utils.Utility;
import com.probridge.vbox.vmm.wmi.HyperVVMM;

public class VboxEditorController extends SelectorComposer<Window> {

	private static final long serialVersionUID = -6972782398962109854L;

	@WireVariable
	Execution _execution;

	@Wire
	Button btnCancel, btnSave;

	@Wire
	Row progressRow;

	@Wire
	Bandbox vmOwner, vmGoldenImage, vmCourseCode;

	@Wire
	Listbox lbUserList, lbImageList, lbCourseList;

	@Wire
	Textbox vmVHDUserFileName, vmName, vmTitle;

	@Wire
	Spinner vmCores, vmMemory, vmHypervisorId;

	@Wire
	Radiogroup vmNetworkType, vmVhdGmType;

	@Wire
	Timer timer;

	@Wire
	Progressmeter progressBar;

	@Wire
	Label progressInfo;

	private VboxManagementController parentController;

	private boolean vmConfigDirty = false;

	private boolean vmStorageConfigDirty = false;

	private String opid;

	private VM vm;

	public VM getVM() {
		return vm;
	}

	private ListModelList<Users> userList;

	public ListModelList<Users> getUserList() {
		if (userList == null) {
			SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
			UsersMapper mapper = session.getMapper(UsersMapper.class);
			UsersExample exp = new UsersExample();
			exp.createCriteria().andUserExpirationGreaterThanOrEqualTo(new Date());
			exp.or(exp.createCriteria().andUserExpirationIsNull());
			userList = new ListModelList<Users>(mapper.selectByExample(exp));
			session.close();
		}
		return userList;
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

	private ListModelList<Course> courseList;

	public ListModelList<Course> getCourseList() {
		if (courseList == null) {
			SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
			CourseMapper mapper = session.getMapper(CourseMapper.class);
			CourseExample exp = new CourseExample();
			exp.createCriteria().andCourseExpirationGreaterThanOrEqualTo(new Date());
			exp.or(exp.createCriteria().andCourseExpirationIsNull());
			courseList = new ListModelList<Course>(mapper.selectByExample(exp));
			session.close();
		}
		return courseList;
	}

	@Listen("onChange = #vmCores, #vmMemory, #vmName; onCheck = #vmNetworkType")
	public void markConfigDirty(Event e) {
		vmConfigDirty = true;
	}

	@Listen("onCheck = #vmVhdGmType; onSelect = #lbImageList, #lbUserList")
	public void markStorageDirty(Event e) {
		vmStorageConfigDirty = true;
	}

	@Listen("onSelect = listbox#lbImageList")
	public void selectImage(Event e) {
		vm.setVmVhdGmImage((((GMImage) lbImageList.getModel().getElementAt(lbImageList.getSelectedIndex()))
				.getGmImageFilename()));
		AnnotateDataBinder binder = (AnnotateDataBinder) getSelf().getAttribute("binder");
		binder.loadComponent(vmGoldenImage);
		vmGoldenImage.close();
	}

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		vm = (VM) _execution.getArg().get("vm");
		parentController = ((VboxManagementController) _execution.getArg().get("parentController"));
		if ("--".equals(vm.getVmId())) {
			vmName.setDisabled(false);
			String constrain = "min 0 max " + (HyperVVMM.hypervisors.length - 1);
			vmHypervisorId.setValue(0);
			vmHypervisorId.setConstraint(constrain);
		}
	}

	@Listen("onChanging = bandbox#vmOwner")
	public void filterUser(InputEvent event) {
		if (event.getValue().trim().length() == 0)
			lbUserList.setModel(getUserList());
		else {
			ListModelList<Users> filterUserList = new ListModelList<>();
			for (Users eachUser : getUserList())
				if (eachUser.getUserName().toLowerCase().startsWith(event.getValue().toLowerCase()))
					filterUserList.add(eachUser);
			lbUserList.setModel(filterUserList);
		}
	}

	@Listen("onSelect = listbox#lbUserList")
	public void selectUser(Event e) {
		Users selectedUser = ((Users) lbUserList.getModel().getElementAt(lbUserList.getSelectedIndex()));
		vm.setVmOwner(lbUserList.getSelectedItem().getLabel());
		vm.setVmVhdUserFilename(selectedUser.getUserVhdName());
		vm.setVmHypervisorId(selectedUser.getUserHypervisorId());
		if ("1".equals(vm.getVmType()))
			vm.setVmName(VBoxConfig.PersonalVMName + Utility.normalized(vm.getVmOwner()));
		else if ("0".equals(vm.getVmType()))
			vm.setVmName(VBoxConfig.CourseVMName + Utility.normalized(vm.getVmOwner()));
		else if ("2".equals(vm.getVmType()))
			vm.setVmName(VBoxConfig.MaintVMName + Utility.normalized(vm.getVmOwner()));
		//
		AnnotateDataBinder binder = (AnnotateDataBinder) getSelf().getAttribute("binder");
		binder.loadComponent(vmOwner);
		binder.loadComponent(vmVHDUserFileName);
		binder.loadComponent(vmName);
		binder.loadComponent(vmHypervisorId);
		vmOwner.close();
	}

	@Listen("onChanging = bandbox#vmCourseCode")
	public void filterCourse(InputEvent event) {
		if (event.getValue().trim().length() == 0)
			lbCourseList.setModel(getCourseList());
		else {
			ListModelList<Course> filterCourseList = new ListModelList<>();
			for (Course eachCourse : getCourseList())
				if (eachCourse.getCourseId().toLowerCase().startsWith(event.getValue().toLowerCase()))
					filterCourseList.add(eachCourse);
			lbCourseList.setModel(filterCourseList);
		}
	}

	@Listen("onSelect = listbox#lbCourseList")
	public void selectCourse(Event e) {
		Course selectedCourse = ((Course) lbCourseList.getModel().getElementAt(lbCourseList.getSelectedIndex()));
		vm.setVmCourseCode(selectedCourse.getCourseId());
		vm.setVmTitle(selectedCourse.getCourseName());
		AnnotateDataBinder binder = (AnnotateDataBinder) getSelf().getAttribute("binder");
		binder.loadComponent(vmCourseCode);
		binder.loadComponent(vmTitle);
		vmCourseCode.close();
	}

	@Listen("onClick = button#btnCancel")
	public void closePanel(Event event) {
		parentController.reload();
		getSelf().getParent().removeChild(getSelf());
	}

	@Listen("onClick = button#btnSave")
	public void updateVM(Event event) {
		AnnotateDataBinder binder = (AnnotateDataBinder) getSelf().getAttribute("binder");
		binder.saveAll();

		if (vmConfigDirty || vmStorageConfigDirty) {
			Messagebox.show("您修改了vBox的配置，应用这些设置可能需要花费一些时间，请确认。", "Question", Messagebox.YES | Messagebox.NO,
					Messagebox.QUESTION, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							if (Messagebox.ON_YES.equals(event.getName())) {
								saveSettings();
							}
						}
					});
		} else {
			saveSettings();
		}
	}

	private void saveSettings() {
		progressRow.setVisible(true);
		btnSave.setDisabled(true);
		btnCancel.setDisabled(true);
		String sid = SecurityUtils.getSubject().getSession().getId().toString();
		opid = String.valueOf(System.currentTimeMillis());
		SaveVMSettingsTask t = new SaveVMSettingsTask(sid, opid, vm, vmConfigDirty, vmStorageConfigDirty);
		if (AdminTaskManager.getInstance().submit(t) == null) {
			progressRow.setVisible(true);
			progressInfo.setValue("请等待之前操作完成");
		} else {
			timer.start();
		}
	}

	@Listen("onTimer = #timer")
	public void updateProgress(Event e) {
		OpStatus status = AdminTaskManager.getInstance().queryStatus(opid);
		if (status.getRetval() == 0 || status.getRetval() == 1) {
			timer.stop();
			progressBar.setValue(100);
			progressInfo.setValue(status.getMsg());
			btnCancel.setLabel("关闭");
			btnCancel.setDisabled(false);
			btnSave.setDisabled(true);
		} else {
			progressInfo.setValue(status.getMsg());
		}
	}
}
