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
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.Spinner;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Window;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.UsersMapper;
import com.probridge.vbox.model.Users;
import com.probridge.vbox.model.UsersExample;
import com.probridge.vbox.servlet.OpStatus;
import com.probridge.vbox.servlet.SaveUserSettingsTask;
import com.probridge.vbox.utils.Utility;
import com.probridge.vbox.vmm.wmi.HyperVVMM;

public class UserEditorController extends SelectorComposer<Window> {

	private static final long serialVersionUID = 1223257687044249993L;

	@WireVariable
	Execution _execution;

	@Wire
	Bandbox bdUserGroup;

	@Wire
	Listbox lbGroupList;

	@Wire
	Textbox tbUserName, tbUserVhdName;

	@Wire
	Hlayout hlUserRole;

	@Wire
	Radiogroup rgUserPwdExpire;

	@Wire
	Radio rNormal, rNever, rNow;

	private Users user;

	private boolean storageDirty = false;

	public Users getUser() {
		return user;
	}

	private ListModelList<String> groupList;

	private UserManagementController parentController;

	private String opid;

	@Wire
	Row progressRow;

	@Wire
	Button btnSave, btnCancel;

	@Wire
	Label progressInfo;

	@Wire
	Timer timer;

	@Wire
	Progressmeter progressBar;

	@Wire
	Spinner userVhdQuota, userHypervisorId;

	public ListModelList<String> getGroupList() {
		if (groupList == null) {
			SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
			UsersMapper mapper = session.getMapper(UsersMapper.class);
			UsersExample exp = new UsersExample();
			groupList = new ListModelList<String>();
			for (Users eachUser : mapper.selectByExample(exp))
				if (eachUser.getUserGroup() != null && eachUser.getUserGroup().length() > 0
						&& !groupList.contains(eachUser.getUserGroup()))
					groupList.add(eachUser.getUserGroup());
			session.close();
		}
		return groupList;
	}

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		user = (Users) _execution.getArg().get("user");
		parentController = ((UserManagementController) _execution.getArg().get("parentController"));
		//
		if (Utility.isEmptyOrNull(user.getUserName())) {
			tbUserName.setReadonly(false);
			storageDirty = true;
			userHypervisorId.setReadonly(false);
			userHypervisorId.setDisabled(false);
			String constrain = "min 0 max " + (HyperVVMM.hypervisors.length - 1);
			userHypervisorId.setConstraint(constrain);
		} else if (user.getUserVhdQuota() != null) {
			String constrain = "min " + user.getUserVhdQuota();
			userVhdQuota.setConstraint(constrain);
		}

		if (user.getUserPwdExpire() == null)
			rgUserPwdExpire.setSelectedItem(rNever);
		else if (user.getUserPwdExpire().before(new Date()))
			rgUserPwdExpire.setSelectedItem(rNow);
		else
			rgUserPwdExpire.setSelectedItem(rNormal);
		//
	}

	@Listen("onChange = textbox#tbUserName")
	public void updateUserVHDFileName(InputEvent event) {
		user.setUserVhdName(Utility.generateUserVhdFileName(event.getValue()));
	}

	@Listen("onClick = button#btnCancel")
	public void closePanel(Event event) {
		parentController.reload();
		getSelf().getParent().removeChild(getSelf());
	}

	@Listen("onChange = #userVhdQuota")
	public void markDirty(Event e) {
		storageDirty = true;
	}

	@Listen("onChanging = bandbox#bdUserGroup")
	public void filterGroup(InputEvent event) {
		if (event.getValue().length() == 0)
			lbGroupList.setModel(getGroupList());
		else {
			ListModelList<String> filterGroupList = new ListModelList<String>();
			for (String eachGroup : getGroupList())
				if (eachGroup.toLowerCase().startsWith(event.getValue().toLowerCase()))
					filterGroupList.add(eachGroup);
			lbGroupList.setModel(filterGroupList);
		}
	}

	@Listen("onSelect = listbox#lbGroupList")
	public void selectGroup(Event e) {
		user.setUserGroup(lbGroupList.getSelectedItem().getLabel());
		AnnotateDataBinder binder = (AnnotateDataBinder) getSelf().getAttribute("binder");
		binder.loadComponent(bdUserGroup);
		bdUserGroup.close();
	}

	@Listen("onClick = button#btnSave")
	public void updateUser(Event event) {
		// save db, allocate user data file - dynamic expension with limit
		AnnotateDataBinder binder = (AnnotateDataBinder) getSelf().getAttribute("binder");
		binder.saveAll();
		//
		if (rNever.isChecked())
			user.setUserPwdExpire(null);
		if (rNow.isChecked())
			user.setUserPwdExpire(new Date());
		//
		if (storageDirty) {
			Messagebox.show("您修改了用户的空间配置，应用这些设置可能需要花费一些时间，请确认。", "Question", Messagebox.YES | Messagebox.NO,
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
		SaveUserSettingsTask t = new SaveUserSettingsTask(sid, opid, user, !tbUserName.isReadonly(), storageDirty);
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
