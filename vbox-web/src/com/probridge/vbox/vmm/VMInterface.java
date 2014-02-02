package com.probridge.vbox.vmm;

import java.util.ArrayList;

public abstract class VMInterface {
	
	public abstract ArrayList<VirtualMachineInstance> retrieveVMList();

	public abstract boolean createVM(VirtualMachineInstance vm);

	public abstract boolean startVM(String vmid);

	public abstract boolean shutdownVM(String vmid);

	public abstract boolean forceStopVM(String vmid);

	public abstract boolean hardResetVM(String vmid);

	public abstract boolean suspendVM(String vmid);

	public abstract boolean resumeVM(String vmid);

	public static VMInterface newInstance() {
		VMInterface result = null;
		Class<?> clazz = null;
		try {
			clazz = Class
					.forName("com.probridge.vbox.vmm.wmi.utils.VMInterfaceImplHyperV");
			result = (VMInterface) clazz.newInstance();
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException e) {
			e.printStackTrace();
		}
		return result;
	}
}