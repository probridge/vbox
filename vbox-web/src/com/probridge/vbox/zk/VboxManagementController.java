package com.probridge.vbox.zk;

import java.text.SimpleDateFormat;
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
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.VMMapper;
import com.probridge.vbox.model.VM;
import com.probridge.vbox.model.VMExample;

public class VboxManagementController extends SelectorComposer<Window> {

	private static final long serialVersionUID = -7361135645876487457L;

	@Wire
	Listbox lbVMLists;

	ListModelList<VM> vmLists = new ListModelList<VM>();

	public ListModelList<VM> getVmLists() {
		return vmLists;
	}

	public void reload() {
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		try {
			VMMapper mapper = session.getMapper(VMMapper.class);
			VMExample exp = new VMExample();
			vmLists.clear();
			vmLists.addAll(mapper.selectByExample(exp));
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

	@Listen("onClick = #btnDeleteVM")
	public void deleteVM(Event e) {
		Listitem thisListItem = lbVMLists.getSelectedItem();
		if (thisListItem == null)
			return;
		VM thisVM = (VM) thisListItem.getValue();
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("vm", thisVM);
		arg.put("parentController", this);
		Window win = (Window) Executions.createComponents("/management/vm_delete.zul", getSelf(), arg);
		getSelf().appendChild(win);
	}

	@Listen("onClick = #btnEditVM")
	public void editVM(Event e) {
		Listitem thisListItem = lbVMLists.getSelectedItem();
		if (thisListItem == null)
			return;
		VM thisVM = (VM) thisListItem.getValue();
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("vm", thisVM);
		arg.put("parentController", this);
		Window win = (Window) Executions.createComponents("/management/vm_editor.zul", getSelf(), arg);
		getSelf().appendChild(win);
	}

	@Listen("onClick = #btnNewVM")
	public void createNewVM(Event event) {
		VM thisVM = new VM();
		thisVM.setVmId("--");
		thisVM.setVmType("0");
		thisVM.setVmDisabled("0");
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("vm", thisVM);
		arg.put("parentController", this);
		Window win = (Window) Executions.createComponents("/management/vm_editor.zul", getSelf(), arg);
		getSelf().appendChild(win);
	}

	@Listen("onClick = #btnFixVM")
	public void fixVM(Event event) {
		Listitem thisListItem = lbVMLists.getSelectedItem();
		if (thisListItem == null)
			return;
		VM thisVM = (VM) thisListItem.getValue();
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("vm", thisVM);
		arg.put("parentController", this);
		Window win = (Window) Executions.createComponents("/management/vm_fix.zul", getSelf(), arg);
		getSelf().appendChild(win);
	}

	@Listen("onClick = #btnShutdownVM")
	public void shutdownVM(Event event) {
		Listitem thisListItem = lbVMLists.getSelectedItem();
		if (thisListItem == null)
			return;
		VM thisVM = (VM) thisListItem.getValue();
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("vm", thisVM);
		arg.put("parentController", this);
		Window win = (Window) Executions.createComponents("/management/vm_shutdown.zul", getSelf(), arg);
		getSelf().appendChild(win);
	}

	@Listen("onClick = #btnShowPassword")
	public void showPassword(Event event) {
		Listitem thisListItem = lbVMLists.getSelectedItem();
		if (thisListItem == null)
			return;
		VM thisVM = (VM) thisListItem.getValue();
		Messagebox.show("该vBox当前的密码为：" + thisVM.getVmGuestPassword() + "\n上次的活动时间为："
				+ (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(thisVM.getVmLastUpdateTimestamp()));
	}

	@Listen("onClick = #btnRefresh")
	public void refresh(Event event) {
		reload();
	}
}
