package com.probridge.vbox.vmm.wmi;

import static com.probridge.vbox.vmm.wmi.utils.Utils.enumToJIVariantArray;
import static org.jinterop.dcom.impls.JIObjectFactory.narrowObject;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.JIArray;
import org.jinterop.dcom.core.JIString;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.vmm.wmi.utils.MyIJIDispatch;
import com.probridge.vbox.vmm.wmi.utils.NotifierListener;
import com.probridge.vbox.vmm.wmi.utils.UpdateServiceEvent;
import com.probridge.vbox.vmm.wmi.utils.Utils;
import com.probridge.vbox.vmm.wmi.utils.VirtualServiceException;

/**
 * Implementation of {@link VirtualMachine} for Microsoft Hyper-V Hypervisor.
 * This module uses J-Interop (see http://j-interop.org/ ) framework for DCOM
 * communication.
 */
/**
 * @author PennyGe
 * 
 */
public class HyperVVM implements VirtualMachine,
		NotifierListener<HyperVVMM, UpdateServiceEvent> {

	/** Class' logger */
	private static final Logger logger = LoggerFactory
			.getLogger(HyperVVM.class);

	/**
	 * The WBemServiceLocator used to manage the hyper-v environment. This is an
	 * instance of WbemScripting.SWbemLocator regarding Microsoft MSDN
	 * Documentation
	 */
	private VirtualizationServiceLocator service;

	/** The dispatch associated with this virtual machine. */
	private MyIJIDispatch vmDispatch;

	/**
	 * The name of the virtual machine, and pieces of inforation used to connect
	 * to remote Microsoft Server 2008 with Hyper-V Role enabled.
	 */
	private final String id, name;
	/**
	 * The {@link HyperVVMM} that instantiated this virtual machine. This
	 * reference is kept to be able to update network settings in case of
	 * network troubles.
	 */
	public final HyperVVMM parent;
	/**
	 * When cloning a virtual machine, one must also create associated switch
	 * port on the virtual switch the template vm is connected. These strings
	 * are the name and friendly name of that switch port, composed of the
	 * virtual machine's name.
	 */
	private final String switchPortName, switchPortFriendlyName;

	/**
	 * Package private constructor to be able to control virtual machine
	 * instantiation.
	 * 
	 * @param service
	 *            The {@link VirtualizationServiceLocator} used by the virtual
	 *            machine to communicate with remote server.
	 * @param vmDispatch
	 *            the {@link MyIJIDispatch} associated with this virtual
	 *            machine.
	 * @param vvm
	 *            the parent {@link HyperVVMM} that created this virtual
	 *            machine.
	 */
	HyperVVM(VirtualizationServiceLocator service, IJIDispatch vmDispatch,
			HyperVVMM vmm) throws VirtualServiceException {
		this.service = service;
		this.vmDispatch = new MyIJIDispatch(vmDispatch);
		this.parent = vmm;
		try {
			this.id = this.vmDispatch.getString("Name");
			this.name = this.vmDispatch.getString("ElementName");
			logger.debug("VM initialized with a dispatch element: "
					+ this.vmDispatch.getDispatch("Path_").getString("Path"));
			this.switchPortFriendlyName = this.name
					+ " Switch Port by Probridge vBox";
			this.switchPortName = this.id + "_MsvmSwitchPort_vBox";
		} catch (JIException e) {
			throw new VirtualServiceException(e,
					"Cannot instantiate virtual machine");
		}
	}

	public String getName() {
		return this.name;
	}

	public String getID() {
		return this.id;
	}

	/**
	 * Reload dispatch object from hypervisor to reflect latest status of the
	 * VM.
	 * 
	 * @throws VirtualServiceException
	 * @throws JIException
	 */
	public void reloadStatus() throws VirtualServiceException {
		testService();
		try {
			JIVariant[] tmp = this.service
					.execQuery("Select * From Msvm_ComputerSystem Where Name='"
							+ this.id + "'");
			JIVariant[][] tmpSet = enumToJIVariantArray(tmp);
			if (tmpSet.length == 0)
				throw new VirtualServiceException("VM[" + name
						+ "] doesn't exist...");
			vmDispatch = new MyIJIDispatch(tmpSet[0][0]);
			return;
		} catch (JIException e) {
			throw new VirtualServiceException(e,
					"error reloading dispatch object");
		}
	}

	/**
	 * Wait VM's status, default timeout 60 seconds
	 * 
	 * @param expectedState
	 * @return
	 */
	public boolean waitFor(VMState expectedState) {
		return waitFor(expectedState, VBoxConfig.vBoxStatusChangeTimeout);
	}

	/**
	 * Wait for VM's state, return when state reached or timeout (roughly)
	 * 
	 * @param expectedState
	 * @param timeout
	 *            in seconds
	 * @return
	 */
	public boolean waitFor(VMState expectedState, int timeout) {
		boolean stateReached = false;
		try {
			int waitTimer = 0;
			while (waitTimer < timeout) {
				if (getState() == expectedState) {
					stateReached = true;
					break;
				}
				logger.debug("Waiting for VM[" + name + "] status: "
						+ expectedState.getName());
				Thread.sleep(1000);
				waitTimer++;
			}
		} catch (VirtualServiceException | InterruptedException e) {
			logger.error(
					"error while waiting for status " + expectedState.getName()
							+ "...", e);
		}
		return stateReached;
	}

	/**
	 * {@inheritDoc}
	 */
	public VMState getState() throws VirtualServiceException {
		try {
			int state = vmDispatch.getInt("EnabledState");
			return VMState.getItem(state);
		} catch (JIException e) {
			throw new VirtualServiceException(e, "Cannot retrieve " + name
					+ "'s state.");
		}
	}

	/**
	 * To get this computer system's heart beat see <a
	 * href="http://msdn.microsoft.com/en-us/library/cc136898(VS.85).aspx">MSDN
	 * Documentation</a>
	 * 
	 * @return 2 = "OK", 6 = "Error", 12 = "No Contact" or 13 =
	 *         "Lost Communication"
	 * @throws VirtualServiceException
	 * @throws JIException
	 * @throws Exception
	 */
	public HeartBeat getHeartBeat() throws VirtualServiceException {
		testService();
		try {
			MyIJIDispatch tmp = service.getVirtualSystemManagementService()
					.getSummaryInformation(this.getVirtualSystemSettingData(),
							new Integer[] { 104 });
			return HeartBeat.getItem(tmp.getInt("HeartBeat"));
		} catch (JIException e) {
			throw new VirtualServiceException(e, "Error getting heartbeat.");
		}
	}

	public VMGuestStatus getVMGuestStatus() throws VirtualServiceException {
		String credential = null;
		String ips = null;
		HeartBeat hb = this.getHeartBeat();
		if (hb == null)
			hb = HeartBeat.NoContact;
		//
		if (hb == HeartBeat.OK) {
			ips = this.getGuestIntrinsicExchangeItem("NetworkAddressIPv4");
			credential = this.getGuestExchangeItem("vBoxGuestOSPassword");
		}
		return new VMGuestStatus(hb, ips, credential);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean powerOn() throws VirtualServiceException {
		reloadStatus();
		if (getState().equals(VMState.Running)) {
			logger.warn("Cannot start " + name + " as it is already running.");
			return false;
		}
		try {
			Object[] params = new Object[] { new Integer(2),
					JIVariant.EMPTY_BYREF(), null };
			JIVariant[] res = vmDispatch.getBase().callMethodA(
					"RequestStateChange", params);
			int result = res[0].getObjectAsInt();
			if (result == 0) {
				logger.info(this.name + " powered on.");
				return true;
			} else {
				if (result == 4096) {
					logger.debug("powering " + this.name + " on...");
					try {
						String jobPath = res[1].getObjectAsVariant()
								.getObjectAsString2();
						Utils.monitorJobState(jobPath, service);
						logger.debug(this.name + " powered on.");
						return true;
					} catch (JIException e) {
						logger.warn("An exception occured while monitoring "
								+ this.name + " powering on", e);
						return false;
					}
				} else {
					logger.warn("Failed at powering " + this.name
							+ " on. Error code: " + result);
					return false;
				}
			}
		} catch (JIException e) {
			throw new VirtualServiceException(e, "Cannot power on " + name
					+ ".");
		}
	}

	public boolean suspend() throws VirtualServiceException {
		reloadStatus();
		if (!getState().equals(VMState.Running)) {
			logger.warn("Cannot suspend " + name + " as it is not running.");
			return false;
		}
		try {
			Object[] params = new Object[] { new Integer(32769),
					JIVariant.EMPTY_BYREF(), null };
			JIVariant[] res = vmDispatch.getBase().callMethodA(
					"RequestStateChange", params);
			int result = res[0].getObjectAsInt();
			if (result == 0) {
				logger.info(this.name + " suspended.");
				return true;
			} else {
				if (result == 4096) {
					logger.debug("suspending " + this.name + " ...");
					try {
						String jobPath = res[1].getObjectAsVariant()
								.getObjectAsString2();
						Utils.monitorJobState(jobPath, service);
						logger.debug(this.name + " suspended.");
						return true;
					} catch (JIException e) {
						logger.warn("An exception occured while monitoring "
								+ this.name + " suspending", e);
						return false;
					}
				} else {
					logger.warn("Failed at suspending " + this.name
							+ " . Error code: " + result);
					return false;
				}
			}
		} catch (JIException e) {
			throw new VirtualServiceException(e, "Cannot suspend " + name + ".");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean powerOff() throws VirtualServiceException {
		reloadStatus();
		if (getState().equals(VMState.PoweredOff)) {
			logger.info("Cannot power off " + name + " as it is already off.");
			return false;
		}
		try {
			Object[] params = new Object[] { new Integer(3),
					JIVariant.EMPTY_BYREF(), null };
			JIVariant[] res = vmDispatch.getBase().callMethodA(
					"RequestStateChange", params);
			int result = res[0].getObjectAsInt();
			if (result == 0) {
				logger.info(name + " powered off.");
				return true;
			} else {
				if (result == 4096) {
					logger.debug("powering " + this.name + " off...");
					try {
						String jobPath = res[1].getObjectAsVariant()
								.getObjectAsString2();
						Utils.monitorJobState(jobPath, service);
						logger.debug(this.name + " powered off.");
					} catch (JIException e) {
						logger.warn("An exception occured while monitoring "
								+ this.name + " powering off", e);
					}
					return true;
				} else {
					logger.warn("Failed at powering " + this.name
							+ " off. Error Code: " + result);
					return false;
				}
			}
		} catch (JIException e) {
			throw new VirtualServiceException(e, "Cannot power off " + name
					+ ".");
		}
	}

	/**
	 * Initiate soft shutdown
	 * 
	 * @return
	 * @throws VirtualServiceException
	 */
	public boolean shutdown() throws VirtualServiceException {
		reloadStatus();
		if (getState().equals(VMState.PoweredOff)) {
			logger.info("Cannot shutdown " + name + " as it is already off.");
			return false;
		}
		try {
			JIVariant[] tmp = service
					.execQuery("SELECT * FROM Msvm_ShutdownComponent WHERE SystemName='"
							+ id + "'");
			JIVariant[][] tmpSet = enumToJIVariantArray(tmp);
			if (tmpSet.length == 0) {
				logger.warn("Could not shutdown "
						+ name
						+ " as shutdown component doesn't exist, try hard power off.");
				return false;
			}
			IJIDispatch machineDispatch = (IJIDispatch) narrowObject(tmpSet[0][0]
					.getObjectAsComObject());
			Object[] params = new Object[] { new JIVariant(true),
					new JIString("vBox Shutdown") };
			JIVariant[] res = machineDispatch.callMethodA("InitiateShutdown",
					params);
			int result = res[0].getObjectAsInt();
			return result == 0;
		} catch (JIException e) {
			throw new VirtualServiceException(e, "Cannot shutdown");
		}
	}

	/**
	 * This method create an identical VM like the template on the current
	 * hypervisor, then add it to current managedVms list for threads to pick
	 * up. Code still need to call modify* method to customize the configuration
	 * 
	 * 
	 * * This method tries to retrieve hardware settings of the parameter
	 * virtual machine and to apply them to this virtual machine. Only removable
	 * devices are not taken in account as they represent potential sources of
	 * conflict for running virtual machines ( there is no way to share these
	 * devices between several virtual machine ). Backing vhd file are not
	 * duplicated, new differencing virtual hard disk are created for each vhd
	 * attached to the template virtual machine. This method first retrieve
	 * 
	 * 
	 * @param newName
	 * @return HyperVVM instance
	 * @throws VirtualServiceException
	 */
	public HyperVVM clone(String newName) throws VirtualServiceException {
		try {
			testService();

			// only allow cloning of the template
			if (!name.equals(VBoxConfig.vmTemplateName))
				throw new VirtualServiceException(
						"Operation not permitted, only allow cloning of the template");
			// check if already exists
			JIVariant[] tmp = service
					.execQuery("Select * From Msvm_VirtualSystemGlobalSettingData Where ElementName='"
							+ newName + "'");
			JIVariant[][] tmpSet = enumToJIVariantArray(tmp);
			if (tmpSet.length > 0)
				throw new VirtualServiceException(
						"A vitual machine with the name " + newName
								+ " already exists.");
			//
			tmp = service
					.execQuery("Select * From Msvm_VirtualSystemGlobalSettingData Where SystemName='"
							+ id + "'");
			tmpSet = enumToJIVariantArray(tmp);
			if (tmpSet.length == 0)
				throw new VirtualServiceException(
						"VM Msvm_VirtualSystemGlobalSettingData not found");
			//
			MyIJIDispatch vsgsdClass = new MyIJIDispatch(tmpSet[0][0]);
			MyIJIDispatch vsgsdInstance = vsgsdClass.clone_();
			vsgsdInstance.put("ElementName", new JIVariant(
					new JIString(newName)));
			// Get ManagementService instance and define new VirtualSystem
			MsvmVirtualSystemManagementService vsmService = service
					.getVirtualSystemManagementService();
			vsmService.defineVirtualSystem(vsgsdInstance);

			HyperVVM newVM = parent.createVMInstanceByName(newName);

			ArrayList<MyIJIDispatch> newResource = newVM
					.copyVolatileResources(this);
			newVM.modifyVirtualSystemResources(newResource);

			// defining non default hardware
			newResource = new ArrayList<MyIJIDispatch>();
			newResource.addAll(newVM.cloneVHDAndControllers(this));
			newResource.addAll(newVM.cloneEthernetDevices(this,
					"Msvm_SyntheticEthernetPortSettingData"));

			for (MyIJIDispatch tmpDispatch : newResource) {
				newVM.addVirtualSystemRessources(tmpDispatch);
			}
			//
			parent.registerVM(newVM);
			return newVM;
		} catch (JIException e) {
			throw new VirtualServiceException(e, "Cannot create " + newName
					+ ".");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void destroy() throws VirtualServiceException {
		testService();
		powerOff();
		//
		parent.deregisterVM(this);
		MsvmVirtualSystemManagementService vsms;
		try {
			vsms = service.getVirtualSystemManagementService();
			vsms.destroyVirtualSystem(this.vmDispatch);
			logger.info(this.name + " destroyed, deleting VHDs");
			try {
				destroyVHDs();
			} catch (UnknownHostException e) {
				throw new VirtualServiceException(e,
						"Cannot delete vhd. Be sure to do it manually in critical space environments");
			}
			//
			logger.info(this.name + " VHD deleted.");
		} catch (JIException e) {
			throw new VirtualServiceException(e, "Cannot destroy VM " + name
					+ ". You may have to destroy it manually.");
		}
	}

	/**
	 * Destroys the vhd files associated with a clone virtual machine. This
	 * method will skip every vhd that haven't been created by cloning an
	 * existing virtual machine with this api.
	 * 
	 * @throws JIException
	 * @throws UnknownHostException
	 *             To remove a file on the remote computer, one uses winmgnt
	 *             over wmi. This exception is raised if the remote server is
	 *             not reachable
	 * @throws VirtualServiceException
	 */
	private void destroyVHDs() throws JIException, UnknownHostException,
			VirtualServiceException {
		WindowsManagementServiceLocator wmServiceLocator = new WindowsManagementServiceLocator(
				parent.url);
		ArrayList<MyIJIDispatch> rasds = getAssociatedToSystemSettingData("Msvm_ResourceAllocationSettingData");
		for (MyIJIDispatch rasd : rasds) {
			int val = rasd.getInt("ResourceType");
			if (val == 21) {
				try {
					String vhdPath = rasd.getStringArray("Connection")[0];

					if (vhdPath.toLowerCase().indexOf(".iso") > 0)
						continue;
					//
					if (vhdPath.toLowerCase().indexOf(
							VBoxConfig.userDataDirectory.toLowerCase()) >= 0)
						continue; // skip user data

					if (vhdPath.toLowerCase()
							.indexOf(
									VBoxConfig.goldenMasterImageDirectory
											.toLowerCase()) >= 0)
						continue; // skip master copy - proceed with master
									// copy's clone
					//
					int result = wmServiceLocator
							.deleteFile(VBoxConfig.dataDrive,
									vhdPath.substring(2,
											vhdPath.lastIndexOf("\\") + 1),
									vhdPath.substring(
											vhdPath.lastIndexOf("\\") + 1,
											vhdPath.lastIndexOf(".")), "vhd");
					if (result == 0) {
						logger.debug(vhdPath + " deleted successfuly");
					} else {
						logger.warn("Unable to delete " + vhdPath
								+ ". Error code " + result);
					}
				} catch (JIException e) {
					logger.warn(
							"An error occured while destroying " + this.name
									+ " vhd files. Check the good deletion", e);
				}
			}
		}
		if (wmServiceLocator != null) {
			wmServiceLocator.destroySession();
		}
	}

	public boolean modifyConfiguration(int numOfProcessor, int memoryReserved,
			int memoryFixed) throws VirtualServiceException, JIException {
		if (getState() != (VMState.PoweredOff))
			throw new VirtualServiceException("VM must be stopped.");
		//
		MyIJIDispatch thisMemSet = getAssociatedToSystemSettingData(
				"Msvm_MemorySettingData").get(0);
		thisMemSet.put("VirtualQuantity",
				new JIVariant(new JIString(String.valueOf(memoryReserved))));
		thisMemSet.put("Limit",
				new JIVariant(new JIString(String.valueOf(memoryFixed))));
		thisMemSet.put("Reservation",
				new JIVariant(new JIString(String.valueOf(memoryReserved))));
		thisMemSet.put("DynamicMemoryEnabled", new JIVariant(
				memoryFixed != memoryReserved));
		//
		MyIJIDispatch thisCPUSet = getAssociatedToSystemSettingData(
				"Msvm_ProcessorSettingData").get(0);
		thisCPUSet.put("VirtualQuantity",
				new JIVariant(new JIString(String.valueOf(numOfProcessor))));
		//
		ArrayList<MyIJIDispatch> toModify = new ArrayList<MyIJIDispatch>();
		toModify.add(thisCPUSet);
		toModify.add(thisMemSet);
		//
		modifyVirtualSystemResources(toModify);
		return true;
	}

	public boolean modifyStorage(String gmVhdImage, int gmVhdType,
			String gmVhdFileName, String userVhdFileName)
			throws VirtualServiceException, JIException {
		if (getState() != (VMState.PoweredOff))
			throw new VirtualServiceException("VM must be stopped.");
		//
		MsvmImageManagementService imageManagementService = service
				.getImageManagementService();
		ArrayList<MyIJIDispatch> curSetting = getAssociatedToSystemSettingData("Msvm_ResourceAllocationSettingData");
		//
		for (MyIJIDispatch rasdDispatch : curSetting) {
			int val = rasdDispatch.getInt("ResourceType");
			if (val == 21) { /* only cares image resource type */
				String vhdPath = rasdDispatch.getStringArray("Connection")[0];
				if (vhdPath.toLowerCase().indexOf(".iso") > 0) // skip DVD ISO
					continue;
				//
				if (vhdPath.indexOf(VBoxConfig.goldenMasterImageDirectory) >= 0
						|| vhdPath.indexOf(VBoxConfig.userImageDirectory) >= 0) {
					// GM VHD - diffvhd(0) OR full clone(1) OR original link(2)
					String absolutePath = null;
					switch (gmVhdType) {
					case 0:
						absolutePath = VBoxConfig.dataDrive
								+ VBoxConfig.userImageDirectory + gmVhdFileName;
						imageManagementService
								.createDifferencingVirtualHardDisk(
										absolutePath,
										VBoxConfig.dataDrive
												+ VBoxConfig.goldenMasterImageDirectory
												+ gmVhdImage);
						break;
					case 1:
						absolutePath = VBoxConfig.dataDrive
								+ VBoxConfig.userImageDirectory + gmVhdFileName;
						imageManagementService.convertVirtualHardDisk(
								VBoxConfig.dataDrive
										+ VBoxConfig.goldenMasterImageDirectory
										+ gmVhdImage, absolutePath, 2);
						break;
					case 2:
						absolutePath = VBoxConfig.dataDrive
								+ VBoxConfig.goldenMasterImageDirectory
								+ gmVhdImage;
						break;
					}
					rasdDispatch.put("Connection", new JIVariant(new JIArray(
							new JIString[] { new JIString(absolutePath) })));
				} else if (vhdPath.indexOf(VBoxConfig.userDataDirectory) >= 0) {
					// User VHD image. direct link
					String absolutePath = VBoxConfig.dataDrive
							+ VBoxConfig.userDataDirectory + userVhdFileName;
					rasdDispatch.put("Connection", new JIVariant(new JIArray(
							new JIString[] { new JIString(absolutePath) })));
				}
			}
		}
		//
		modifyVirtualSystemResources(curSetting);
		//
		return true;
	}

	public boolean modifyNetwork(int networkType)
			throws VirtualServiceException, JIException {
		if (getState() != (VMState.PoweredOff))
			throw new VirtualServiceException("VM must be stopped.");
		//
		String type = "Msvm_SyntheticEthernetPortSettingData";
		//
		ArrayList<MyIJIDispatch> curNetworkSetting = getAssociatedToSystemSettingData(type);
		int i = 0;
		for (MyIJIDispatch eepsdDispatch : curNetworkSetting) {
			try {
				i++;
				JIVariant[] tmp = service
						.execQuery("Select * From Msvm_SwitchPort Where Name='"
								+ this.switchPortName + "_" + type + "_" + i
								+ "'");
				JIVariant[][] tmpSet = enumToJIVariantArray(tmp);
				if (tmpSet.length > 0) {
					// delete existing switchports
					for (JIVariant[] eachElement : tmpSet) {
						MyIJIDispatch thisSwitchPortDispatch = new MyIJIDispatch(
								eachElement[0]);
						service.getVirtualSwitchManagementService()
								.deleteSwitchPort(
										thisSwitchPortDispatch.getDispatch(
												"Path_").getString("Path"));
					}
				}
				// create the Msvm_SwitchPort
				String networkSwitchName = (networkType == 0) ? VBoxConfig.vmInternalNetworkSwitchName
						: VBoxConfig.vmExternalNetworkSwitchName;
				tmp = service
						.execQuery("Select * From Msvm_VirtualSwitch Where ElementName='"
								+ networkSwitchName + "'");
				//
				MyIJIDispatch virtualSwitch = new MyIJIDispatch(
						enumToJIVariantArray(tmp)[0][0]);

				int error = service.getVirtualSwitchManagementService()
						.createSwitchPort(
								virtualSwitch.getDispatch("Path_").getString(
										"Path"),
								this.switchPortFriendlyName + " #" + i,
								this.switchPortName + "_" + type + "_" + i,
								null);
				if (error != 0) {
					throw new JIException(error,
							"Cannot create Msvm_SwitchPort associated with "
									+ type);
				}
				// get the existing switch port
				tmp = service
						.execQuery("Select * From Msvm_SwitchPort Where Name='"
								+ this.switchPortName + "_" + type + "_" + i
								+ "'");
				tmpSet = enumToJIVariantArray(tmp);
				MyIJIDispatch newSwitchPortDispatch = new MyIJIDispatch(
						tmpSet[0][0]);
				eepsdDispatch.put("Connection", new JIVariant(new JIArray(
						new JIString[] { new JIString(newSwitchPortDispatch
								.getDispatch("Path_").getString("Path")) })));
				eepsdDispatch.put("Address", null);
			} catch (Exception e) {
				logger.warn("An exception occured while cloning " + type, e);
			}
		}
		//
		modifyVirtualSystemResources(curNetworkSetting);
		return true;

	}

	/**
	 * This method is not intended to be called by end users. It is used to
	 * update connection information after a network failure
	 */
	public void update(HyperVVMM notifier, UpdateServiceEvent event) {
		this.service = notifier.getServiceLocator();
		try {
			reloadStatus();
		} catch (VirtualServiceException e) {
			logger.error(
					"error while updating switching to the new dispatch object.",
					e);
		}
	}

	/*----------------------------------
	 * Getters, Setters and Helpers
	 *---------------------------------*/

	/**
	 * The purpose of this method is to test the remote service availability,
	 * and, in case of troubles, notify the parent {@link HyperVVMM} to
	 * negotiate a new network connection.
	 */
	private void testService() throws VirtualServiceException {
		final int tries = 2;
		for (int i = 0; i <= tries; i++) {
			try {
				service.execQuery("Select EnabledState From Msvm_ComputerSystem Where ElementName='"
						+ name + "'");
				return;
			} catch (Exception e) {
				if (i >= tries) {
					throw new VirtualServiceException(e,
							"After numerous tries, the service is still unavailable.");
				}
				logger.debug("Update service requested.");
				parent.updateServiceRequest();
			}
		}
	}

	private ArrayList<MyIJIDispatch> copyVolatileResources(HyperVVM cs) {
		ArrayList<MyIJIDispatch> res = new ArrayList<MyIJIDispatch>();
		try {
			ArrayList<MyIJIDispatch> templateMemSet = cs
					.getAssociatedToSystemSettingData("Msvm_MemorySettingData");
			// we need to get this setting data to have the good instance id &
			// path properties.
			ArrayList<MyIJIDispatch> thisMemSet = this
					.getAssociatedToSystemSettingData("Msvm_MemorySettingData");
			if (templateMemSet != null && templateMemSet.size() > 0
					&& thisMemSet != null && thisMemSet.size() > 0) {
				MyIJIDispatch templateMem = templateMemSet.get(0), thisMem = thisMemSet
						.get(0);
				thisMem.put("AllocationUnits", new JIVariant(new JIString(
						templateMem.getString("AllocationUnits"))));
				thisMem.put("VirtualQuantity", new JIVariant(new JIString(
						templateMem.getString("VirtualQuantity"))));
				thisMem.put(
						"Limit",
						new JIVariant(new JIString(templateMem
								.getString("Limit"))));
				thisMem.put("Reservation", new JIVariant(new JIString(
						templateMem.getString("Reservation"))));
				thisMem.put(
						"DynamicMemoryEnabled",
						new JIVariant(new JIVariant(templateMem
								.getBoolean("DynamicMemoryEnabled"))));
				res.add(thisMem);
			} else {
				logger.warn("Memory settings for template(" + cs.getName()
						+ ") and clone(" + this.getName()
						+ ") are not compatible.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			ArrayList<MyIJIDispatch> templateProcSet = cs
					.getAssociatedToSystemSettingData("Msvm_ProcessorSettingData");
			// we need to get this setting data to have the good instance id &
			// path properties.
			ArrayList<MyIJIDispatch> thisProcSet = this
					.getAssociatedToSystemSettingData("Msvm_ProcessorSettingData");
			if (templateProcSet != null && templateProcSet.size() > 0
					&& thisProcSet != null && thisProcSet.size() > 0) {
				MyIJIDispatch templateProc = templateProcSet.get(0), thisProc = thisProcSet
						.get(0);
				thisProc.put(
						"ProcessorsPerSocket",
						new JIVariant(new JIVariant(templateProc
								.getInt("ProcessorsPerSocket"))));
				thisProc.put("Reservation", new JIVariant(new JIString(
						templateProc.getString("Reservation"))));
				thisProc.put("SocketCount", new JIVariant(new JIVariant(
						templateProc.getInt("SocketCount"))));
				thisProc.put("ThreadsEnabled", new JIVariant(new JIVariant(
						templateProc.getInt("ThreadsEnabled"))));
				thisProc.put("VirtualQuantity", new JIVariant(new JIString(
						templateProc.getString("VirtualQuantity"))));
				thisProc.put("LimitCPUID", new JIVariant(new JIVariant(
						templateProc.getBoolean("LimitCPUID"))));
				thisProc.put(
						"Limit",
						new JIVariant(new JIString(templateProc
								.getString("Limit"))));
				thisProc.put(
						"Weight",
						new JIVariant(new JIVariant(templateProc
								.getInt("Weight"))));
				res.add(thisProc);
			} else {
				logger.warn("Processor settings for template(" + cs.getName()
						+ ") and clone(" + this.getName()
						+ ") are not compatible.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * Clone the ethernet devices represented by the String parameter ( see
	 * http://msdn.microsoft.com/en-us/library/cc433215(VS.85).aspx to get the
	 * different types of supported resources, for example
	 * Msvm_SyntheticEthernetPortSettingData,
	 * Msvm_EmulatedEthernetPortSettingData ).
	 * 
	 * @param hyperVVM
	 *            the virtual machine used as template.
	 * @param type
	 *            the type of ethernet resource ( emulated or synthetic for
	 *            example ).
	 * @return a collection of cloned ethernet devices.
	 * @throws JIException
	 * @throws VirtualServiceException
	 */
	private ArrayList<MyIJIDispatch> cloneEthernetDevices(HyperVVM hyperVVM,
			String type) throws JIException, VirtualServiceException {
		testService();
		//
		ArrayList<MyIJIDispatch> res = new ArrayList<MyIJIDispatch>();
		// get hyperVVM associated SyntheticEthernetPortSettingData
		logger.debug("Cloning " + type + " devices");
		ArrayList<MyIJIDispatch> eepsdFromTemplate = hyperVVM
				.getAssociatedToSystemSettingData(type);
		int i = 0;
		for (MyIJIDispatch eepsdDispatch : eepsdFromTemplate) {
			try {
				i++;
				JIVariant[] tmp = service
						.execQuery("Select * From Msvm_SwitchPort Where Name='"
								+ this.switchPortName + "_" + type + "_" + i
								+ "'");
				JIVariant[][] tmpSet = enumToJIVariantArray(tmp);
				// create the Msvm_SwitchPort
				if (tmpSet.length == 0) {
					String switchPortPath = eepsdDispatch
							.getStringArray("Connection")[0];
					MyIJIDispatch switchPortDispatch = new MyIJIDispatch(
							service.get(switchPortPath));
					String virtualSwitchName = switchPortDispatch
							.getString("SystemName");

					tmp = service
							.execQuery("Select * From Msvm_VirtualSwitch Where Name='"
									+ virtualSwitchName + "'");

					MyIJIDispatch virtualSwitch = new MyIJIDispatch(
							enumToJIVariantArray(tmp)[0][0]);

					int error = service.getVirtualSwitchManagementService()
							.createSwitchPort(
									virtualSwitch.getDispatch("Path_")
											.getString("Path"),
									this.switchPortFriendlyName + " #" + i,
									this.switchPortName + "_" + type + "_" + i,
									null);
					if (error != 0) {
						throw new JIException(error,
								"Cannot create Msvm_SwitchPort associated with "
										+ type);
					}
				} else {
					logger.debug("Msvm_SwitchPort " + this.switchPortName + "_"
							+ type + "_" + i
							+ " already exists, trying to use it.");
				}
				// get the existing switch port
				tmp = service
						.execQuery("Select * From Msvm_SwitchPort Where Name='"
								+ this.switchPortName + "_" + type + "_" + i
								+ "'");
				tmpSet = enumToJIVariantArray(tmp);
				MyIJIDispatch newSwitchPortDispatch = new MyIJIDispatch(
						tmpSet[0][0]);
				MyIJIDispatch newEepsdDispatch = eepsdDispatch.clone_();
				newEepsdDispatch.put("Connection", new JIVariant(new JIArray(
						new JIString[] { new JIString(newSwitchPortDispatch
								.getDispatch("Path_").getString("Path")) })));
				newEepsdDispatch.put("Address", null);
				res.add(newEepsdDispatch);
			} catch (Exception e) {
				logger.warn("An exception occured while cloning " + type, e);
			}
		}
		return res;
	}

	/**
	 * Clones the vhd backing file and controllers of the parameter.
	 * 
	 * @param hyperVVM
	 *            the virtual machine from which one the hardware is cloned.
	 * @return A list containing the cloned hardware
	 * @throws JIException
	 * @throws UnknownHostException
	 * @throws VirtualServiceException
	 */
	private ArrayList<MyIJIDispatch> cloneVHDAndControllers(HyperVVM hyperVVM)
			throws JIException, VirtualServiceException {

		ArrayList<MyIJIDispatch> res = new ArrayList<MyIJIDispatch>();
		// get hyperVVM associated ResourceAllocationSettingData
		ArrayList<MyIJIDispatch> rasdFromTemplate = hyperVVM
				.getAssociatedToSystemSettingData("Msvm_ResourceAllocationSettingData");
		// for all Msvm_ResourceAllocationSettingData of the template virtual
		// machine
		for (MyIJIDispatch rasdDispatch : rasdFromTemplate) {
			// String en = rasdDispatch.getString("ElementName");
			int val = rasdDispatch.getInt("ResourceType");
			/*
			 * @see
			 * http://msdn.microsoft.com/en-us/library/cc136877(v=vs.85).aspx
			 */
			if (val == 6 || val == 22) {
				res.add(rasdDispatch.clone_());
			}

			if (val == 21) { /* en.contains("Hard Disk Image") */
				String vhdPath = rasdDispatch.getStringArray("Connection")[0];
				if (vhdPath.toLowerCase().indexOf(".iso") > 0) // skip DVD ISO
					continue;
				//
				MyIJIDispatch tmp = rasdDispatch.clone_();
				tmp.put("Connection", new JIVariant(new JIArray(
						new JIString[] { new JIString(vhdPath) })));
				res.add(tmp);
			}
		}
		return res;
	}

	/**
	 * Get the collection associated with the Msvm_VirtualSystemSettingData of
	 * this virtual machine with the resultClass parameter type.
	 * 
	 * @param resultClass
	 *            the type of the element associated with
	 *            Msvm_VirtualSystemSettingData of this virtual machine ( see
	 *            http://msdn.microsoft.com/en-us/library/cc136990(VS.85).aspx
	 *            for available result classes )
	 * @return a list containing the wanted elements.
	 * @throws JIException
	 * @throws VirtualServiceException
	 */
	private ArrayList<MyIJIDispatch> getAssociatedToSystemSettingData(
			String resultClass) throws JIException, VirtualServiceException {
		testService();
		MyIJIDispatch vssdDispatch = getVirtualSystemSettingData();
		ArrayList<MyIJIDispatch> res = new ArrayList<MyIJIDispatch>();
		JIVariant[] tmp = service
				.execQuery("Associators of {Msvm_VirtualSystemSettingData.InstanceID='"
						+ vssdDispatch.getString("InstanceID")
						+ "'} Where ResultClass=" + resultClass);
		JIVariant[][] rasdSet = enumToJIVariantArray(tmp);
		for (int i = 0; i < rasdSet.length; i++) {
			try {
				res.add(new MyIJIDispatch(rasdSet[i][0]));
			} catch (IndexOutOfBoundsException e) {
				logger.warn("An error occured while determining the virtual system setting data of "
						+ this.name);
			}
		}
		return res;
	}

	/**
	 * To get the VirtualSystemSettingData associated to this virtual machine
	 * 
	 * @return the {@link MyIJIDispatch} of this vm's
	 *         Msvm_VirtualSystemSettingData
	 * @throws JIException
	 * @throws VirtualServiceException
	 */
	private MyIJIDispatch getVirtualSystemSettingData() throws JIException,
			VirtualServiceException {
		testService();
		String vmPath = this.vmDispatch.getDispatch("Path_").getString("Path");
		JIVariant[] tmp = service
				.execQuery("Associators of {"
						+ vmPath
						+ "} Where AssocClass=Msvm_SettingsDefineState ResultClass=Msvm_VirtualSystemSettingData");
		JIVariant[][] vssdVariantArray = enumToJIVariantArray(tmp);
		return new MyIJIDispatch(
				(IJIDispatch) JIObjectFactory
						.narrowObject(vssdVariantArray[0][0]
								.getObjectAsComObject().queryInterface(
										IJIDispatch.IID)));
	}

	private void addVirtualSystemRessources(
			MyIJIDispatch newResourceAllocationDispatch) throws JIException,
			VirtualServiceException {
		service.getVirtualSystemManagementService().addVirtualSystemRessources(
				this.vmDispatch, newResourceAllocationDispatch);
	}

	private void modifyVirtualSystemResources(ArrayList<MyIJIDispatch> toModify)
			throws JIException {
		service.getVirtualSystemManagementService()
				.modifyVirtualSystemResources(this.vmDispatch, toModify);
	}

	public String[] getMacAddress() throws VirtualServiceException {
		testService();
		try {
			String path = this.vmDispatch.getDispatch("Path_")
					.getString("Path");
			JIVariant[] tmp = service.execQuery("Associators of {" + path
					+ "} Where ResultClass=CIM_EthernetPort");
			JIVariant[][] tmpSet = Utils.enumToJIVariantArray(tmp);
			String[] res = new String[tmpSet.length];
			for (int i = 0; i < tmpSet.length; i++) {
				MyIJIDispatch nic = new MyIJIDispatch(tmpSet[i][0]);
				String tmpMac = nic.getString("PermanentAddress");
				tmpMac = tmpMac.trim();
				String mac = tmpMac.substring(0, 2) + ":"
						+ tmpMac.substring(2, 4) + ":" + tmpMac.substring(4, 6)
						+ ":" + tmpMac.substring(6, 8) + ":"
						+ tmpMac.substring(8, 10) + ":"
						+ tmpMac.substring(10, 12);
				res[i] = mac;
			}
			return res;
		} catch (Exception e) {
			throw new VirtualServiceException(e, "Cannot get mac address.");
		}
	}

	/**
	 * Calls {@link #getKvpExchangeData(String)} {@inheritDoc}
	 */
	public String getData(String dataKey) throws VirtualServiceException {
		return getKvpExchangeData(dataKey);
	}

	/**
	 * Calls {@link #removeKvpExchangeData(String)} if a similar key/value pair
	 * is already registered and then calls
	 * {@link #pushKvpExchangeData(String, String)} {@inheritDoc}
	 */
	public boolean pushData(String dataKey, String value)
			throws VirtualServiceException {
		return pushKvpExchangeData(dataKey, value);
	}

	/**
	 * Updates the virtual machine's environment. The pair key/value can be
	 * retrieved either in the virtual machine's registry ( in case of Windows
	 * guest ) in HKLM/SOFTWARE/Microsoft/Virtual Machine/External or calling
	 * {@link #getKvpExchangeData(String)}
	 * 
	 * @param dataKey
	 *            the key
	 * @param value
	 *            the associated value
	 * @return the associated value of null if no such key has been pushed.
	 * @throws VirtualServiceException
	 */
	public boolean pushKvpExchangeData(String dataKey, String value)
			throws VirtualServiceException {
		testService();
		try {
			// K/V parameter
			MyIJIDispatch kvpDataItemsClass = new MyIJIDispatch(
					service.get("Msvm_KvpExchangeDataItem"));
			MyIJIDispatch kvpDataItem = kvpDataItemsClass.spawnInstance_();
			if (value != null)
				kvpDataItem.put("Data", new JIVariant(new JIString(value)));
			else
				kvpDataItem.put("Data", JIVariant.EMPTY());
			kvpDataItem.put("Name", new JIVariant(new JIString(dataKey)));
			kvpDataItem.put("Source", new JIVariant(new Integer(0)));
			MyIJIDispatch[] paramArray = new MyIJIDispatch[] { kvpDataItem };
			MsvmVirtualSystemManagementService vsms = service
					.getVirtualSystemManagementService();
			int result = vsms.addKvpItems(this.vmDispatch, paramArray);
			if (result == 0) {
				return true;
			} else {
				if (result == 4096) {
					logger.debug("Failed at pushing kvp data items, try to update values.");
					if (vsms.modifyKvmItems(this.vmDispatch,
							new MyIJIDispatch[] { kvpDataItem }) == 0) {
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			}
		} catch (Exception e) {
			throw new VirtualServiceException(e, "Cannot update " + this.name
					+ " environment with " + dataKey);
		}
	}

	/**
	 * Used to update virtual machine's environment with values contained in the
	 * Hashtable
	 * 
	 * @param values
	 * @return true if the environment was successfully updated false otherwise
	 * @throws VirtualServiceException
	 */
	public boolean pushKvpExchangeData(HashMap<String, String> values)
			throws VirtualServiceException {
		testService();
		try {
			MyIJIDispatch[] paramArray = new MyIJIDispatch[values.size()];
			MyIJIDispatch kvpDataItemsClass = new MyIJIDispatch(
					service.get("Msvm_KvpExchangeDataItem"));
			Set<String> keys = values.keySet();
			int i = 0;
			for (String key : keys) {
				MyIJIDispatch kvpDataItem = kvpDataItemsClass.spawnInstance_();
				kvpDataItem.put("Data",
						new JIVariant(new JIString(values.get(key))));
				kvpDataItem.put("Name", new JIVariant(new JIString(key)));
				kvpDataItem.put("Source", new JIVariant(new Integer(0)));
				paramArray[i] = kvpDataItem;
				i++;
			}
			MsvmVirtualSystemManagementService vsms = service
					.getVirtualSystemManagementService();
			int result = vsms.addKvpItems(this.vmDispatch, paramArray);
			if (result == 0) {
				return true;
			} else {
				if (result == 4096) {
					logger.debug("Failed at pushing kvp data items, try to update values.");
					if (vsms.modifyKvmItems(this.vmDispatch, paramArray) == 0) {
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			}
		} catch (Exception e) {
			throw new VirtualServiceException(e, "Cannot update " + this.name
					+ " environment.");
		}
	}

	/**
	 * To remove a previously set K/V pair
	 * 
	 * @param dataKey
	 *            the key
	 * @return true in case of success false otherwise
	 * @throws VirtualServiceException
	 */
	public boolean removeKvpExchangeData(String dataKey)
			throws VirtualServiceException {
		testService();
		try {
			MyIJIDispatch kvpDataItemsClass = new MyIJIDispatch(
					service.get("Msvm_KvpExchangeDataItem"));
			MyIJIDispatch kvpDataItem = kvpDataItemsClass.spawnInstance_();
			kvpDataItem.put("Data", new JIVariant(new JIString("")));
			kvpDataItem.put("Name", new JIVariant(new JIString(dataKey)));
			kvpDataItem.put("Source", new JIVariant(new Integer(0)));
			MsvmVirtualSystemManagementService vsms = service
					.getVirtualSystemManagementService();
			int result = vsms.removeKvpItems(this.vmDispatch,
					new MyIJIDispatch[] { kvpDataItem });
			if (result == 0) {
				return true;
			} else {
				if (result == 4096) {
					logger.warn("Cannot ensure that the removeKvpExchangeData exited normally...");
					return true;
				} else {
					return false;
				}
			}
		} catch (Exception e) {
			throw new VirtualServiceException(e, "Cannot update " + this.name
					+ " environment with " + dataKey);
		}
	}

	/**
	 * Used to get a previously set K/V pair.
	 * 
	 * @param dataKey
	 *            the key to use.
	 * @return the associated value if it exists, null otherwise
	 * @throws VirtualServiceException
	 */
	public String getKvpExchangeData(String dataKey)
			throws VirtualServiceException {
		testService();
		try {
			MyIJIDispatch vssd = getVirtualSystemSettingData();
			String vssdPath = vssd.getDispatch("Path_").getString("Path");
			JIVariant[] tmp = service
					.execQuery("Associators of {"
							+ vssdPath
							+ "} Where AssocClass=Msvm_VirtualSystemSettingDataComponent ResultClass=Msvm_KvpExchangeComponentSettingData");
			JIVariant[][] tmpSet = enumToJIVariantArray(tmp);
			if (tmpSet.length == 0) {
				return null;
			}
			for (int j = 0; j < tmpSet.length; j++) {
				MyIJIDispatch kecsd = new MyIJIDispatch(tmpSet[j][0]);
				String[] values = kecsd.getStringArray("HostExchangeItems");
				for (int i = 0; i < values.length; i++) {
					String tmpDataName = Utils.getStringProperty(values[i],
							"Name");
					if (tmpDataName != null && tmpDataName.equals(dataKey)) {
						return Utils.getStringProperty(values[i], "Data");
					}
				}
			}
			return null;
		} catch (Exception e) {
			throw new VirtualServiceException(e, "Cannot get kvp with key "
					+ dataKey);
		}
	}

	/**
	 * To retrieve a key set by the guest windows operating system. To be
	 * usable, guest additions must be installed, and you must set a K/V pair in
	 * Windows guest registry: with reg.exe reg add
	 * "HKLM\SOFTWARE\Microsoft\Virtual Machine\Guest" /v key /t REG_SZ /d value
	 * or with powershell New-ItemProperty -path
	 * "HKLM\SOFTWARE\Microsoft\Virtual Machine\Guest" -name key -value value
	 * 
	 * @param dataKey
	 *            the key from which one the associated value is retrieved
	 * @return the associated value if such a key exists, null otherwise
	 * @throws VirtualServiceException
	 */
	public String getGuestExchangeItem(String dataKey)
			throws VirtualServiceException {
		testService();
		String vmPath = null;
		try {
			vmPath = this.vmDispatch.getDispatch("Path_").getString("Path");
			JIVariant[] tmp = service
					.execQuery("Associators of {"
							+ vmPath
							+ "} Where AssocClass=Msvm_SystemDevice ResultClass=Msvm_KvpExchangeComponent");
			JIVariant[][] tmpSet = enumToJIVariantArray(tmp);
			if (tmpSet.length == 0) {
				return null;
			}
			for (int j = 0; j < tmpSet.length; j++) {
				MyIJIDispatch kec = new MyIJIDispatch(tmpSet[j][0]);
				String[] kvp = kec.getStringArray("GuestExchangeItems");
				for (int i = 0; i < kvp.length; i++) {
					String tmpDataName = Utils
							.getStringProperty(kvp[i], "Name");
					if (tmpDataName != null && tmpDataName.equals(dataKey)) {
						return Utils.getStringProperty(kvp[i], "Data");
					}
				}
			}
			return null;
		} catch (Exception e) {
			throw new VirtualServiceException(e, "Cannot get kvp with key "
					+ dataKey);
		}
	}

	/**
	 * To retrieve a key set by the guest windows operating system.ct. you can
	 * check <a
	 * href="http://msdn.microsoft.com/en-us/library/cc136850(VS.85).aspx">The
	 * MSDN Documentation</a> to see what pieces of information are availab
	 * 
	 * @param dataKey
	 *            the key from which one the associated value is retrieved
	 * @return the associated value if such a key exists, null otherwise
	 * @throws VirtualServiceException
	 */
	public String getGuestIntrinsicExchangeItem(String dataKey)
			throws VirtualServiceException {
		testService();
		String vmPath = null;
		try {
			vmPath = this.vmDispatch.getDispatch("Path_").getString("Path");
			JIVariant[] tmp = service
					.execQuery("Associators of {"
							+ vmPath
							+ "} Where AssocClass=Msvm_SystemDevice ResultClass=Msvm_KvpExchangeComponent");
			JIVariant[][] tmpSet = enumToJIVariantArray(tmp);
			if (tmpSet.length == 0) {
				return null;
			}
			for (int j = 0; j < tmpSet.length; j++) {
				MyIJIDispatch kec = new MyIJIDispatch(tmpSet[j][0]);
				String[] kvp = kec
						.getStringArray("GuestIntrinsicExchangeItems");
				for (int i = 0; i < kvp.length; i++) {
					String tmpDataName = Utils
							.getStringProperty(kvp[i], "Name");
					if (tmpDataName != null && tmpDataName.equals(dataKey)) {
						return Utils.getStringProperty(kvp[i], "Data");
					}
				}
			}
			return null;
		} catch (Exception e) {
			throw new VirtualServiceException(e, "Cannot get kvp with key "
					+ dataKey);
		}
	}

}
