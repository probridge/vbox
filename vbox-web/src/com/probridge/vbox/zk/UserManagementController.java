package com.probridge.vbox.zk;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Window;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.UsersMapper;
import com.probridge.vbox.model.Users;
import com.probridge.vbox.model.UsersExample;

public class UserManagementController extends SelectorComposer<Window> {

	private static final long serialVersionUID = -7361135645876487457L;

	@Wire
	Listbox lbUserList;

	ListModelList<Users> userList = new ListModelList<Users>();

	public ListModelList<Users> getUserList() {
		return userList;
	}

	public void reload() {
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		try {
			UsersMapper mapper = session.getMapper(UsersMapper.class);
			UsersExample exp = new UsersExample();
			exp.createCriteria().andUserVhdNameIsNotNull().andUserVhdNameNotEqualTo("").andUserVhdQuotaGreaterThan(0);
			userList.clear();
			userList.addAll(mapper.selectByExample(exp));
			//
		} finally {
			session.close();
		}
	}

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		reload();
	}

	@Listen("onClick = #btnDeleteUser")
	public void deleteUser(Event event) {
		Listitem thisListItem = lbUserList.getSelectedItem();
		if (thisListItem == null)
			return;
		Users thisUser = (Users) thisListItem.getValue();
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("user", thisUser);
		arg.put("parentController", this);
		Window win = (Window) Executions.createComponents("/management/user_delete.zul", getSelf(), arg);
		getSelf().appendChild(win);
	}

	@Listen("onClick = #btnEditUser")
	public void editUser(Event e) {
		Listitem thisListItem = lbUserList.getSelectedItem();
		if (thisListItem == null)
			return;
		Users thisUser = (Users) thisListItem.getValue();
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("user", thisUser);
		arg.put("parentController", this);
		Window win = (Window) Executions.createComponents("/management/user_editor.zul", getSelf(), arg);
		getSelf().appendChild(win);
	}

	@Listen("onClick = #btnNewUser")
	public void createNewUser(Event event) {
		Users thisUser = new Users();
		thisUser.setUserEnabled("1");
		thisUser.setUserRole("ROLE_USER");
		thisUser.setUserType("0");
		thisUser.setUserVhdQuota(20);
		thisUser.setUserHypervisorId(VBoxConfig.repositoryLocation);
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("user", thisUser);
		arg.put("parentController", this);
		Window win = (Window) Executions.createComponents("/management/user_editor.zul", getSelf(), arg);
		getSelf().appendChild(win);
	}
}
