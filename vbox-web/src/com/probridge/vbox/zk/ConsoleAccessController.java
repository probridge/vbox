package com.probridge.vbox.zk;

import org.apache.ibatis.session.SqlSession;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Selectbox;
import org.zkoss.zul.Window;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.VMMapper;
import com.probridge.vbox.model.VM;
import com.probridge.vbox.model.VMExample;
import com.probridge.vbox.vmm.wmi.VirtualMachine.VMState;

public class ConsoleAccessController extends SelectorComposer<Window> {

	private static final long serialVersionUID = 1L;

	@WireVariable
	Desktop desktop;

	@Wire
	Button btnConnect;

	@Wire
	Selectbox selectVbox;

	@Wire
	Checkbox cbUseAdmin, cbFullScreen;

	ListModelList<VM> vmLists = new ListModelList<VM>();

	public ListModelList<VM> getVmLists() {
		return vmLists;
	}

	public void reload() {
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		try {
			VMMapper mapper = session.getMapper(VMMapper.class);
			VMExample exp = new VMExample();
			exp.or().andVmStatusEqualTo(VMState.Running.getValue());
			vmLists.clear();
			VM hypervisor = new VM();
			hypervisor.setVmName("Hypervisor");
			hypervisor.setVmIpAddress(VBoxConfig.hypervisorConsole);
			hypervisor.setVmGuestPassword(VBoxConfig.hypervisorPwd);
			vmLists.add(hypervisor);
			vmLists.addAll(mapper.selectByExample(exp));
			//
		} finally {
			session.close();
		}
	}

	@Override
	public void doAfterCompose(Window win) throws Exception {
		super.doAfterCompose(win);
		reload();
		//
		if (!"ie".equalsIgnoreCase(Executions.getCurrent().getBrowser())) {
			Messagebox.show("抱歉，目前控制台访问只支持Internet Explorer浏览器，其他浏览器会在今后提供支持", "浏览器", Messagebox.OK,
					Messagebox.INFORMATION);
		}
	}

	@Listen("onClick = #btnConnect")
	public void doConnect() {
		String ip = vmLists.get(selectVbox.getSelectedIndex()).getVmIpAddress();
		String userName = cbUseAdmin.isChecked() ? VBoxConfig.osManagementAccount : VBoxConfig.guestOSUserName;
		String pwd = cbUseAdmin.isChecked() ? VBoxConfig.osManagementPassword : vmLists.get(
				selectVbox.getSelectedIndex()).getVmGuestPassword();
		String fullscreen = cbFullScreen.isChecked() ? "true" : "false";
		//
		if (selectVbox.getSelectedIndex() == 0) {
			userName = VBoxConfig.hypervisorUser;
			pwd = VBoxConfig.hypervisorPwd;
		}
		Clients.evalJavaScript("connect(\"" + ip + "\",\"" + userName + "\",\"" + pwd + "\"," + fullscreen + ");");
	}
}