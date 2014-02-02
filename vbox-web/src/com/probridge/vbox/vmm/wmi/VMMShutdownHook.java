package com.probridge.vbox.vmm.wmi;

import java.util.ArrayList;

public class VMMShutdownHook extends Thread {
	
	ArrayList<WeakHyperVVMM> toRelease = null;

	VMMShutdownHook(ArrayList<WeakHyperVVMM> toRelease) {
		this.toRelease = toRelease;
	}

	@Override
	public void run() {
		Thread.currentThread().setName("vBox Shutdown Hook Thread");
		for (WeakHyperVVMM wvmm : toRelease) {
			if (wvmm.get() != null) {
				wvmm.disconnect();
			}
		}
	}
}
