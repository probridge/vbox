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
import com.probridge.vbox.dao.UsersMapper;
import com.probridge.vbox.model.Users;
import com.probridge.vbox.model.UsersExample;

public class UserManagement {

	private Users selectedUser;
	private List<Users> userList = new ArrayList<Users>();
	private boolean autoRefresh = true;

	@WireVariable
	private Page _page;

	@Init
	public void init() {
		reloadUserList();
	}

	@GlobalCommand
	@NotifyChange({ "userList", "selectedUser" })
	public void reloadUserList() {
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		try {
			UsersMapper mapper = session.getMapper(UsersMapper.class);
			UsersExample exp = new UsersExample();
			userList.clear();
			userList.addAll(mapper.selectByExample(exp));
			for (Users eachUser : userList)
				if (selectedUser != null && eachUser.getUserName().equals(selectedUser.getUserName()))
					selectedUser = eachUser;
			//
		} finally {
			session.close();
		}
	}

	@Command
	public void deleteUser() {
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("user", selectedUser);
		Window win = (Window) Executions.createComponents("/management/user_delete_new.zul", null, arg);
		win.setPage(_page);
	}

	@Command
	public void editUser() {
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("user", selectedUser);
		Window win = (Window) Executions.createComponents("/management/user_editor_new.zul", null, arg);
		win.setPage(_page);
	}

	@Command
	public void newUser() {
		Users thisUser = new Users();
		thisUser.setUserEnabled("1");
		thisUser.setUserRole("ROLE_USER");
		thisUser.setUserType("0");
		thisUser.setUserVhdQuota(20);
		thisUser.setUserHypervisorId(VBoxConfig.repositoryLocation);

		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("user", thisUser);
		Window win = (Window) Executions.createComponents("/management/user_editor_new.zul", null, arg);
		win.setPage(_page);
	}

	@Command
	@NotifyChange({ "userList", "selectedUser" })
	public void refresh() {
		reloadUserList();
	}

	public boolean isAutoRefresh() {
		return autoRefresh;
	}

	public void setAutoRefresh(boolean autoRefresh) {
		this.autoRefresh = autoRefresh;
	}

	public Users getSelectedUser() {
		return selectedUser;
	}

	public void setSelectedUser(Users selectedUser) {
		this.selectedUser = selectedUser;
	}

	public void setUserList(List<Users> userList) {
		this.userList = userList;
	}

	public List<Users> getUserList() {
		return userList;
	}
}
