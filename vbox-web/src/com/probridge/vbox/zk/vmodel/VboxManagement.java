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
import com.probridge.vbox.dao.VMMapper;
import com.probridge.vbox.model.VM;
import com.probridge.vbox.model.VMExample;

public class VboxManagement {

	private VM selectedVM;
	private List<VM> vmLists = new ArrayList<VM>();
	private boolean autoRefresh = true;

	@WireVariable
	private Page _page;

	@Init
	public void init() {
		reloadVmList();
	}

	public List<VM> getVmLists() {
		return vmLists;
	}

	@GlobalCommand
	@NotifyChange({ "vmLists", "selectedVM" })
	public void reloadVmList() {
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		try {
			VMMapper mapper = session.getMapper(VMMapper.class);
			VMExample exp = new VMExample();
			vmLists.clear();
			vmLists.addAll(mapper.selectByExample(exp));
			for (VM eachVM : vmLists)
				if (selectedVM != null && eachVM.getVmId().equals(selectedVM.getVmId()))
					selectedVM = eachVM;
			//
		} finally {
			session.close();
		}
	}

	@Command
	public void deleteVM() {
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("vm", selectedVM);
		Window win = (Window) Executions.createComponents("/management/vm_delete_new.zul", null, arg);
		win.setPage(_page);
	}

	@Command
	public void fixVM() {
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("vm", selectedVM);
		Window win = (Window) Executions.createComponents("/management/vm_fix_new.zul", null, arg);
		win.setPage(_page);
	}

	@Command
	public void shutdownVM() {
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("vm", selectedVM);
		Window win = (Window) Executions.createComponents("/management/vm_shutdown_new.zul", null, arg);
		win.setPage(_page);
	}
	
	@Command
	public void poweronVM() {
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("vm", selectedVM);
		Window win = (Window) Executions.createComponents("/management/vm_poweron_new.zul", null, arg);
		win.setPage(_page);
	}
	
	@Command
	@NotifyChange({ "vmLists", "selectedVM" })
	public void refresh() {
		reloadVmList();
	}

	@Command
	public void editVM() {
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("vm", selectedVM);
		Window win = (Window) Executions.createComponents("/management/vm_editor_new.zul", null, arg);
		win.setPage(_page);
	}

	@Command
	public void newVM() {
		VM thisVM = new VM();
		thisVM.setVmId("--");
		thisVM.setVmName("新建vBox");
		thisVM.setVmType("0");
		thisVM.setVmDisabled("0");
		thisVM.setVmCores(VBoxConfig.defaultCPUCores);
		thisVM.setVmMemory(VBoxConfig.defaultMemory);
		thisVM.setVmNetworkType(VBoxConfig.defaultNetwork);
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("vm", thisVM);
		Window win = (Window) Executions.createComponents("/management/vm_editor_new.zul", null, arg);
		win.setPage(_page);
	}

	public VM getSelectedVM() {
		return selectedVM;
	}

	public void setSelectedVM(VM selectedVM) {
		this.selectedVM = selectedVM;
	}

	public boolean isAutoRefresh() {
		return autoRefresh;
	}

	public void setAutoRefresh(boolean autoRefresh) {
		this.autoRefresh = autoRefresh;
	}
}
