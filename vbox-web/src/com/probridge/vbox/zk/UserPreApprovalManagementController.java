package com.probridge.vbox.zk;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.PreApprovedUserMapper;
import com.probridge.vbox.model.PreApprovedUser;
import com.probridge.vbox.model.PreApprovedUserExample;

public class UserPreApprovalManagementController extends SelectorComposer<Div> {
	private static final long serialVersionUID = -4592498413991042683L;

	@Wire
	Listbox lbUserPreApprovalList;

	ListModelList<PreApprovedUser> userPreApprovalList = new ListModelList<PreApprovedUser>();

	public ListModelList<PreApprovedUser> getUserPreApprovalList() {
		return userPreApprovalList;
	}

	public void reload() {
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		try {
			PreApprovedUserMapper mapper = session.getMapper(PreApprovedUserMapper.class);
			PreApprovedUserExample exp = new PreApprovedUserExample();
			exp.createCriteria();
			userPreApprovalList.clear();
			userPreApprovalList.addAll(mapper.selectByExample(exp));
			//
		} finally {
			session.close();
		}
	}

	@Override
	public void doAfterCompose(Div comp) throws Exception {
		super.doAfterCompose(comp);
		reload();
	}

	@Listen("onClick = #btnDeletePreApproval")
	public void deleteCourse(Event e) {
		if (lbUserPreApprovalList.getSelectedItem() == null)
			return;
		//
		Messagebox.show("是否确认要删除这个预授权？", "确认", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION,
				new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						if (!Messagebox.ON_YES.equals(event.getName()))
							return;
						//
						PreApprovedUser thisPreApprovedUser = (PreApprovedUser) lbUserPreApprovalList.getSelectedItem()
								.getValue();
						SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
						PreApprovedUserMapper mapper = session.getMapper(PreApprovedUserMapper.class);
						mapper.deleteByPrimaryKey(thisPreApprovedUser.getPreapproveUserName());
						//
						session.commit();
						session.close();
						//
						reload();
					}
				});
	}

	@Listen("onClick = #btnEditPreApproval")
	public void editUser(Event e) {
		Listitem thisListItem = lbUserPreApprovalList.getSelectedItem();
		if (thisListItem == null)
			return;
		PreApprovedUser thisPreApprovedUser = (PreApprovedUser) thisListItem.getValue();
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("preapproval", thisPreApprovedUser);
		arg.put("parentController", this);
		Window win = (Window) Executions.createComponents("/management/user_preapproval_editor.zul", getSelf(), arg);
		getSelf().appendChild(win);
	}

	@Listen("onClick = #btnAddPreApproval")
	public void createCourse(Event event) {
		PreApprovedUser approvalInfo = new PreApprovedUser();
		approvalInfo.setPreapproveVhdQuota(VBoxConfig.defaultVHDQuota);
		approvalInfo.setPreapproveVmCores(VBoxConfig.defaultCPUCores);
		approvalInfo.setPreapproveVmGoldenMaster(VBoxConfig.defaultGoldenImage);
		approvalInfo.setPreapproveVmMemory(VBoxConfig.defaultMemory);
		approvalInfo.setPreapproveVmNetwork(VBoxConfig.defaultNetwork);
		//
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("preapproval", approvalInfo);
		arg.put("parentController", this);
		Window win = (Window) Executions.createComponents("/management/user_preapproval_editor.zul", getSelf(), arg);
		getSelf().appendChild(win);
	}
}
