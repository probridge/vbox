package com.probridge.vbox.zk.vmodel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.ibatis.session.SqlSession;
import org.zkoss.bind.Form;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.UsersMapper;
import com.probridge.vbox.model.Users;
import com.probridge.vbox.model.UsersExample;
import com.probridge.vbox.servlet.SaveUserSettingsTask;
import com.probridge.vbox.utils.Utility;
import com.probridge.vbox.vmm.wmi.HyperVVMM;

public class UserEditor extends ProgressViewModel {
	private Users user;
	private ArrayList<String> groupList;

	private String userPwdExpire;
	private boolean newUser = false;

	private String selectedGroup = null;
	private VBoxForm form = new VBoxForm();

	private String constrainHypervisor = "min 0 max " + (HyperVVMM.hypervisors.length - 1);
	private String constrainQuota = "min 5";
	private String initUserPwdExpire;

	@Init
	public void init() {
		user = (Users) _execution.getArg().get("user");
		globalCommandName = "reloadUserList";
		newUser = (user.getUserName() == null);
		//
		if (!newUser && user.getUserVhdQuota()!=null)
			constrainQuota = "min " + user.getUserVhdQuota();
		//
		SqlSession session = null;
		try {
			session = VBoxConfig.sqlSessionFactory.openSession();
			UsersMapper mapper = session.getMapper(UsersMapper.class);
			UsersExample exp = new UsersExample();
			groupList = new ArrayList<String>();
			for (Users eachUser : mapper.selectByExample(exp))
				if (eachUser.getUserGroup() != null && eachUser.getUserGroup().length() > 0
						&& !groupList.contains(eachUser.getUserGroup()))
					groupList.add(eachUser.getUserGroup());
		} catch (Exception e) {

		} finally {
			if (session != null)
				session.close();
		}
		//
		for (String eachUser : groupList)
			if (eachUser.equals(user.getUserGroup()))
				selectedGroup = eachUser;
		//
		if (user.getUserPwdExpire() == null)
			userPwdExpire = "1";
		else if (user.getUserPwdExpire().before(new Date()))
			userPwdExpire = "2";
		else
			userPwdExpire = "0";
		//
		initUserPwdExpire = userPwdExpire;
	}

	@Command
	@NotifyChange("form")
	public void reload() {
		//
		if (form.getField("userName") != null)
			form.setField("userVhdName", Utility.generateUserVhdFileName(String.valueOf(form.getField("userName"))));
		form.addSaveFieldName("userVhdName");
		//
		if (selectedGroup != null)
			form.setField("userGroup", selectedGroup);
		form.addSaveFieldName("userGroup");
		//
		if (initUserPwdExpire.equals(userPwdExpire))
			form.setField("userPwdExpire", user.getUserPwdExpire());
		else {
			if ("0".equals(userPwdExpire)) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				cal.add(Calendar.MONTH, 3);
				form.setField("userPwdExpire", cal.getTime());
			}
			if ("1".equals(userPwdExpire)) { // never
				form.setField("userPwdExpire", null);
			}
			if ("2".equals(userPwdExpire)) { // now
				form.setField("userPwdExpire", new Date());
			}
		}
		form.addSaveFieldName("userPwdExpire");
	}

	@Command
	@NotifyChange({ "progress", "progressMsg", "started", "running", "closeBtnLabel" })
	public void save() {
		//
		boolean modifyStorage = form.isFieldDirty("userVhdQuota") || newUser;
		//
		SaveUserSettingsTask t = new SaveUserSettingsTask(getSid(), getOpId(), user, newUser, modifyStorage);
		submit(t);
		return;
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

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
	}

	public ArrayList<String> getGroupList() {
		return groupList;
	}

	public void setGroupList(ArrayList<String> groupList) {
		this.groupList = groupList;
	}

	public String getSelectedGroup() {
		return selectedGroup;
	}

	public void setSelectedGroup(String selectedGroup) {
		this.selectedGroup = selectedGroup;
	}

	public boolean isNewUser() {
		return newUser;
	}

	public void setNewUser(boolean newUser) {
		this.newUser = newUser;
	}

	public String getConstrainQuota() {
		return constrainQuota;
	}

	public void setConstrainQuota(String constrainQuota) {
		this.constrainQuota = constrainQuota;
	}

	public String getUserPwdExpire() {
		return userPwdExpire;
	}

	public void setUserPwdExpire(String userPwdExpire) {
		this.userPwdExpire = userPwdExpire;
	}
}
