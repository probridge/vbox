package com.probridge.vbox.zk;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Window;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.UsersMapper;
import com.probridge.vbox.model.Users;
import com.probridge.vbox.model.UsersExample;

public class UserRequestApprovalController extends SelectorComposer<Div> {
	private static final long serialVersionUID = -4592498413991042683L;

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
			exp.or().andUserVhdNameIsNull().andUserVhdQuotaLessThan(0);
			exp.or().andUserVhdNameEqualTo("").andUserVhdQuotaLessThan(0);
			userList.clear();
			userList.addAll(mapper.selectByExample(exp));
		} finally {
			session.close();
		}
	}

	@Override
	public void doAfterCompose(Div comp) throws Exception {
		super.doAfterCompose(comp);
		reload();
	}

	@Listen("onClick = #btnProcess")
	public void process(Event e) {
		Listitem thisListItem = lbUserList.getSelectedItem();
		if (thisListItem == null)
			return;
		Users thisUser = (Users) thisListItem.getValue();
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("user", thisUser);
		arg.put("parentController", this);
		Window win = (Window) Executions.createComponents("/management/user_approve.zul", getSelf(), arg);
		getSelf().appendChild(win);
	}
}
