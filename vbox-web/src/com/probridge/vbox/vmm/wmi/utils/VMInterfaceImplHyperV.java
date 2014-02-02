package com.probridge.vbox.vmm.wmi.utils;

import java.io.IOException;
import java.util.ArrayList;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.impls.automation.IJIEnumVariant;

import com.probridge.vbox.vmm.VMInterface;
import com.probridge.vbox.vmm.VirtualMachineInstance;

public class VMInterfaceImplHyperV extends VMInterface {
	private static final int HYPERV_POWER_ON = 2;
	private static final int HYPERV_POWER_OFF = 3;
	private static final int HYPERV_HARD_RESET = 10;
	private static final int HYPERV_SUSPEND = 32769;

	public ArrayList<VirtualMachineInstance> retrieveVMList() {
		WMIUtils wmistub = null;
		ArrayList<VirtualMachineInstance> vmlist = new ArrayList<VirtualMachineInstance>();
		try {
			wmistub = new WMIUtils("root\\virtualization");
			wmistub.init();
			JIVariant[] vms = wmistub
					.ExecQuery("SELECT * FROM Msvm_ComputerSystem where ProcessID != NULL");

			int cc = WMIUtils.getObjectSetCount(vms);

			IJIEnumVariant eVarc = WMIUtils.getObjectSetEnum(vms);
			//
			for (int i = 0; i < cc; i++) {
				JIVariant eachVM = WMIUtils.getEnumNext(eVarc);

				VirtualMachineInstance thisVM = new VirtualMachineInstance();
				thisVM.id = WMIUtils.getPropertyAsString(eachVM, "Name");
				thisVM.name = WMIUtils.getPropertyAsString(eachVM,
						"ElementName");
				thisVM.state = WMIUtils
						.getPropertyAsInt(eachVM, "EnabledState");
				// thisVM.uptime = WMIUtils.getPropertyAsLong(eachVM,
				// "OnTimeInMilliseconds");

				vmlist.add(thisVM);
			}
		} catch (SecurityException | IOException | JIException e) {
			e.printStackTrace();
		} finally {
			if (wmistub != null)
				wmistub.free();
		}
		return vmlist;
	}

	@Override
	public boolean createVM(VirtualMachineInstance vm) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean startVM(String vmid) {
		return requestStateChange(vmid, HYPERV_POWER_ON);
	}

	@Override
	public boolean shutdownVM(String vmid) {
		return false;
	}

	@Override
	public boolean forceStopVM(String vmid) {
		return requestStateChange(vmid, HYPERV_POWER_OFF);
	}

	@Override
	public boolean hardResetVM(String vmid) {
		return requestStateChange(vmid, HYPERV_HARD_RESET);
	}

	@Override
	public boolean suspendVM(String vmid) {
		return requestStateChange(vmid, HYPERV_SUSPEND);
	}

	@Override
	public boolean resumeVM(String vmid) {
		return requestStateChange(vmid, HYPERV_POWER_ON);
	}

	private boolean requestStateChange(String vmid, int newState) {
		WMIUtils wmistub = null;
		boolean retval = false;
		try {
			wmistub = new WMIUtils("root\\virtualization");
			wmistub.init();
			JIVariant[] vms = wmistub
					.ExecQuery("SELECT * FROM Msvm_ComputerSystem where Name=\""
							+ vmid + "\"");

			int cc = WMIUtils.getObjectSetCount(vms);

			IJIEnumVariant eVarc = WMIUtils.getObjectSetEnum(vms);
			//
			for (int i = 0; i < cc; i++) {
				JIVariant eachVM = WMIUtils.getEnumNext(eVarc);
				Object[] params = new Object[] { new Integer(newState),
						JIVariant.EMPTY_BYREF(), null };
				retval = WMIUtils.invokeMethod(eachVM, "RequestStateChange",
						params) == 0;
			}
		} catch (SecurityException | IOException | JIException e) {
			e.printStackTrace();
		} finally {
			if (wmistub != null)
				wmistub.free();
		}
		return retval;
	}
}
