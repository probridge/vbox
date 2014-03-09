package com.probridge.vbox.zk.vmodel;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;

import com.probridge.vbox.model.VM;
import com.probridge.vbox.servlet.PowerOnVboxTask;

public class VboxPowerOn extends ProgressViewModel {
	private VM vm;

	@Init
	public void init() {
		vm = (VM) _execution.getArg().get("vm");
		globalCommandName = "reloadVmList";
	}

	@Command
	@NotifyChange({ "progress", "progressMsg", "started", "running", "closeBtnLabel" })
	public void poweron() {
		PowerOnVboxTask t = new PowerOnVboxTask(getSid(), getOpId(), vm.getVmId());
		submit(t);
		return;
	}

	public VM getVm() {
		return vm;
	}

	public void setVm(VM vm) {
		this.vm = vm;
	}
}
