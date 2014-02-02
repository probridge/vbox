package com.probridge.vbox.vmm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VirtualMachineManager {
	private ConcurrentHashMap<String, VirtualMachineInstance> vms;

	private static VirtualMachineManager instance = null;

	public static VirtualMachineManager getInstance() {
		if (instance == null)
			instance = new VirtualMachineManager();
		//
		return instance;
	}

	private VirtualMachineManager() {
		vms = new ConcurrentHashMap<String, VirtualMachineInstance>();
		reload();
	}

	private void reload() {
		VMInterface vmi = VMInterface.newInstance();
		ArrayList<VirtualMachineInstance> vmlist = vmi.retrieveVMList();
		for (VirtualMachineInstance eachVM : vmlist)
			vms.put(eachVM.id, eachVM);
		return;
	}

	public Map<String, VirtualMachineInstance> getVMList() {
		return Collections.unmodifiableMap(vms);
	}

	public VirtualMachineInstance getVMByName(String vmid) {
		return null;
	}

	public String getHostName() {
		return null;
	}

	public boolean startVM(VirtualMachineInstance VM) {
		VMInterface vmi = VMInterface.newInstance();
		return vmi.startVM(VM.id);
	}

	public boolean shutdownVM(VirtualMachineInstance VM) {
		return true;
	}

	public boolean poweroffVM(VirtualMachineInstance VM) {
		VMInterface vmi = VMInterface.newInstance();
		return vmi.forceStopVM(VM.id);
	}
	
	public boolean suspendVM(VirtualMachineInstance VM) {
		VMInterface vmi = VMInterface.newInstance();
		return vmi.suspendVM(VM.id);
	}

	public boolean hardResetVM(VirtualMachineInstance VM) {
		VMInterface vmi = VMInterface.newInstance();
		return vmi.hardResetVM(VM.id);
	}

	public int getState(VirtualMachineInstance VM) {
		return 0;
	}
}
