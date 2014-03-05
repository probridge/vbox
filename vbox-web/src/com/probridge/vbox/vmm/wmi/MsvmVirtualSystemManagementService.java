package com.probridge.vbox.vmm.wmi;

import java.util.ArrayList;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.JIArray;
import org.jinterop.dcom.core.JIPointer;
import org.jinterop.dcom.core.JIString;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.vmm.wmi.utils.MyIJIDispatch;
import com.probridge.vbox.vmm.wmi.utils.Utils;
import com.probridge.vbox.vmm.wmi.utils.VirtualServiceException;

/**
 * Representation of
 * http://msdn.microsoft.com/en-us/library/cc136940(VS.85).aspx
 * 
 */
public class MsvmVirtualSystemManagementService {

	private static final Logger logger = LoggerFactory.getLogger(MsvmVirtualSystemManagementService.class);
	private IJIDispatch msvmVSMSDispatch = null;
	private VirtualizationServiceLocator serviceLocator = null;

	MsvmVirtualSystemManagementService(IJIDispatch dispatch, VirtualizationServiceLocator service) {
		this.msvmVSMSDispatch = dispatch;
		this.serviceLocator = service;
	}

	public MyIJIDispatch getSummaryInformation(MyIJIDispatch vssd, Integer[] what) throws JIException,
			VirtualServiceException {
		String vssdPath = vssd.getDispatch("Path_").getString("Path");
		JIString[] param = new JIString[what.length];
		for (int i = 0; i < param.length; i++) {
			param[i] = new JIString(new Integer(what[i]).toString());
		}
		JIVariant[] tmp = msvmVSMSDispatch.callMethodA("GetSummaryInformation", new Object[] {
				new JIArray(new JIString[] { new JIString(vssdPath) }), new JIArray(what), JIVariant.EMPTY_BYREF() });
		int result = tmp[0].getObjectAsInt();
		if (result == 0) {
			try {
				return new MyIJIDispatch(((JIVariant[]) tmp[1].getObjectAsVariant().getObjectAsArray()
						.getArrayInstance())[0]);
			} catch (Exception e) {
				throw new VirtualServiceException(e, "Cannot convert get summary information result.");
			}
		}
		throw new VirtualServiceException("Cannot get summary information, returnvalue: " + result
				+ " (!= 0 indicates an error)." + " See http://msdn.microsoft.com/en-us/library/cc160706(VS.85).aspx");
	}

	/**
	 * Adds a new hardware resource to the virtual machine represented by the
	 * first {@link MyIJIDispatch} parameter
	 * 
	 * @param vmDispatch
	 *            the {@link MyIJIDispatch} representing the virtual machine
	 * @param newResourceAllocationDispatch
	 *            the {@link MyIJIDispatch} representing the resource to add to
	 *            the virtual machine
	 * @throws JIException
	 * @throws VirtualServiceException
	 */
	public void addVirtualSystemRessources(MyIJIDispatch vmDispatch, MyIJIDispatch newResourceAllocationDispatch)
			throws JIException, VirtualServiceException {
		String vmPath = vmDispatch.getDispatch("Path_").getString("Path");
		JIVariant[] tmp = msvmVSMSDispatch.callMethodA("AddVirtualSystemResources",
				new Object[] { new JIString(vmPath),
						new JIArray(new JIString[] { new JIString(newResourceAllocationDispatch.getText_()) }),
						JIVariant.EMPTY_BYREF(), JIVariant.EMPTY_BYREF() });
		int result = tmp[0].getObjectAsInt();
		String name = newResourceAllocationDispatch.getString("ElementName");
		if (result == 0) {
			logger.debug(name + " added to " + vmPath);
		} else {
			if (result == 4096) {
				logger.debug("Addind resources...");
				String jobPath = tmp[1].getObjectAsVariant().getObjectAsString2();
				Utils.monitorJobState(jobPath, serviceLocator);
			} else {
				logger.error(name + " addition to " + vmPath + " failed with error code " + result);
				throw new IllegalStateException("Cannot add resource " + name + " to "
						+ vmDispatch.getString("ElementName"));
			}
		}
	}

	public void modifyVirtualSystem(MyIJIDispatch vmDispatch, MyIJIDispatch vsgsdDefString) throws JIException,
			VirtualServiceException {
		String vmPath = vmDispatch.getDispatch("Path_").getString("Path");

		JIVariant[] tmp = msvmVSMSDispatch.callMethodA("ModifyVirtualSystem", new Object[] { new JIString(vmPath),
				new JIString(vsgsdDefString.getText_()), JIVariant.OPTIONAL_PARAM(), JIVariant.OPTIONAL_PARAM() });
		//
		int defRes = tmp[0].getObjectAsInt();
		if (defRes == 0) {
			logger.debug("System modified successfully..");
		} else {
			if (defRes == 4096) {
				logger.debug("Modifying system...");
				String jobPath = tmp[1].getObjectAsVariant().getObjectAsString2();
				Utils.monitorJobState(jobPath, serviceLocator);
			} else {
				throw new VirtualServiceException("Cannot modify " + vsgsdDefString.getString("ElementName")
						+ ". Error code " + defRes);
			}
		}
	}

	public void modifyVirtualSystemResources(MyIJIDispatch vmDispatch, ArrayList<MyIJIDispatch> toModify)
			throws JIException {
		String vmPath = vmDispatch.getDispatch("Path_").getString("Path");
		JIString[] resourcesDef = new JIString[toModify.size()];
		for (int i = 0; i < toModify.size(); i++) {
			resourcesDef[i] = new JIString(toModify.get(i).getText_());
		}
		JIVariant[] tmp = msvmVSMSDispatch.callMethodA("ModifyVirtualSystemResources", new Object[] {
				new JIString(vmPath), new JIArray(resourcesDef), JIVariant.EMPTY_BYREF() });
		int result = tmp[0].getObjectAsInt();
		if (result == 0) {
			logger.debug("Resources modified for computer system " + vmPath);
		} else {
			if (result == 4096) {
				logger.debug("Modifying resources...");
				String jobPath = tmp[1].getObjectAsVariant().getObjectAsString2();
				Utils.monitorJobState(jobPath, serviceLocator);
			} else {
				logger.error("Cannot modify virtual system resources of " + vmPath + " Failed with error code: "
						+ result);
				throw new IllegalStateException("Cannot modify resources of " + vmPath);
			}
		}
	}

	/**
	 * To create a new virtual system ( a new virtual machine in DMTF's OVF
	 * vocabulary ) based on a Msvm_VirtualSystemGlobalSettingData ( see
	 * http://msdn.microsoft.com/en-us/library/cc136990(VS.85).aspx )
	 * 
	 * @param vsgsdDefString
	 *            the Msvm_VirtualSystemGlobalSettingData representing the
	 *            virtual machine to be created
	 * @return String ID of newly created VM
	 * @throws VirtualServiceException
	 * @throws JIException
	 */
	public int defineVirtualSystem(MyIJIDispatch vsgsdDefString) throws VirtualServiceException, JIException {
		JIVariant[] tmp = msvmVSMSDispatch.callMethodA("DefineVirtualSystem", new Object[] {
				new JIString(vsgsdDefString.getText_()), JIVariant.OPTIONAL_PARAM(), JIVariant.OPTIONAL_PARAM(),
				JIVariant.OUTPARAMforType(JIPointer.class, false) });
		//
		int defRes = tmp[0].getObjectAsInt();
		if (defRes == 0) {
			logger.debug("System defined successfully..");
		} else {
			if (defRes == 4096) {
				logger.debug("Defining system...");
				String jobPath = tmp[1].getObjectAsVariant().getObjectAsString2();
				Utils.monitorJobState(jobPath, serviceLocator);
			} else {
				throw new VirtualServiceException("Cannot create " + vsgsdDefString.getString("ElementName")
						+ ". Error code " + defRes);
			}
		}
		return defRes;
	}

	/**
	 * Tries to destroy an existing Msvm_ComputerSystem on Hyper-V
	 * 
	 * @param vm
	 *            the Msvm_ComputerSystem associated to a HyperVVM
	 * @return the error code of the native call
	 * @throws JIException
	 */
	public int destroyVirtualSystem(MyIJIDispatch vm) throws JIException {
		String vsPath = vm.getDispatch("Path_").getString("Path");
		logger.debug("Destroying Virtual System " + vsPath);
		JIVariant[] res = msvmVSMSDispatch.callMethodA("DestroyVirtualSystem", new Object[] { new JIString(vsPath),
				JIVariant.EMPTY_BYREF() });
		int error = res[0].getObjectAsInt();
		if (res.length > 1) {
			if (error != 0) {
				if (error == 4096) {
					logger.debug("destroying virtual system...");
					String jobPath = res[1].getObjectAsVariant().getObjectAsString2();
					Utils.monitorJobState(jobPath, serviceLocator);
					logger.debug("Virtual System destroyed.");
				} else {
					logger.error("Failed at destroying Virtual System " + vsPath);
					throw new JIException(error, "Cannot destroy Virtual system " + vsPath);
				}
			}
		}
		return error;
	}

	/**
	 * Add a key/value pair for the vm parameter
	 * 
	 * @param vm
	 *            the vm whose environment will be updated
	 * @param kvpItems
	 *            the array containing key/value pairs
	 * @return see http://msdn.microsoft.com/en-us/library/cc160704(VS.85).aspx
	 * @throws JIException
	 */
	public int addKvpItems(MyIJIDispatch vm, MyIJIDispatch[] kvpItems) throws JIException {
		String vmPath = vm.getDispatch("Path_").getString("Path");
		JIString[] kvpItemsParam = new JIString[kvpItems.length];
		for (int i = 0; i < kvpItems.length; i++) {
			kvpItemsParam[i] = new JIString(kvpItems[i].getText_());
		}

		JIVariant[] result = msvmVSMSDispatch.callMethodA("AddKvpItems", new Object[] { new JIString(vmPath),
				new JIArray(kvpItemsParam), JIVariant.EMPTY_BYREF() });
		int res = result[0].getObjectAsInt();
		if (res == 0) {
			return 0;
		} else {
			if (res == 4096) {
				try {
					logger.debug("Waiting for AddKvpItems to return...");
					String jobPath = result[1].getObjectAsVariant().getObjectAsString2();
					Utils.monitorJobState(jobPath, serviceLocator);
					logger.debug("AddKvpItems succeed");
					return 0;
				} catch (Exception e) {
					logger.warn("An exception occured while monitoring AddKvpItems execution, check the result.", e);
				}
				return 4096;
			} else {
				logger.error("AddKvpItems failed for " + vmPath + " with error code: " + res);
				throw new JIException(res, "Cannot AddKvpItems to " + vmPath);
			}
		}
	}

	public int removeKvpItems(MyIJIDispatch vm, MyIJIDispatch[] kvpItems) throws JIException {
		String vmPath = vm.getDispatch("Path_").getString("Path");
		JIString[] kvpItemsParam = new JIString[kvpItems.length];
		for (int i = 0; i < kvpItems.length; i++) {
			kvpItemsParam[i] = new JIString(kvpItems[i].getText_());
		}

		JIVariant[] result = msvmVSMSDispatch.callMethodA("RemoveKvpItems", new Object[] { new JIString(vmPath),
				new JIArray(kvpItemsParam), JIVariant.EMPTY_BYREF() });
		int res = result[0].getObjectAsInt();
		if (res == 0) {
			return 0;
		} else {
			if (res == 4096) {
				try {
					logger.debug("Waiting for RemoveKvpItems to return...");
					String jobPath = result[1].getObjectAsVariant().getObjectAsString2();
					Utils.monitorJobState(jobPath, serviceLocator);
					logger.debug("RemoveKvpItems succeed");
					return 0;
				} catch (Exception e) {
					logger.warn("An exception occured while monitoring RemoveKvpItems execution, check the result.", e);
				}
				return 4096;
			} else {
				logger.error("RemoveKvpItems failed for " + vmPath + " with error code: " + res);
				throw new JIException(res, "Cannot RemoveKvpItems to " + vmPath);
			}
		}
	}

	public int modifyKvmItems(MyIJIDispatch vm, MyIJIDispatch[] kvpItems) throws JIException {
		String vmPath = vm.getDispatch("Path_").getString("Path");
		JIString[] kvpItemsParam = new JIString[kvpItems.length];
		for (int i = 0; i < kvpItems.length; i++) {
			kvpItemsParam[i] = new JIString(kvpItems[i].getText_());
		}

		JIVariant[] result = msvmVSMSDispatch.callMethodA("ModifyKvpItems", new Object[] { new JIString(vmPath),
				new JIArray(kvpItemsParam), JIVariant.EMPTY_BYREF() });
		int res = result[0].getObjectAsInt();
		if (res == 0) {
			return 0;
		} else {
			if (res == 4096) {
				try {
					logger.debug("Waiting for ModifyKvpItems to return...");
					String jobPath = result[1].getObjectAsVariant().getObjectAsString2();
					Utils.monitorJobState(jobPath, serviceLocator);
					logger.debug("ModifyKvpItems succeed");
					return 0;
				} catch (Exception e) {
					logger.warn("An exception occured while monitoring ModifyKvpItems execution, check the result.", e);
				}
				return 4096;
			} else {
				logger.error("ModifyKvpItems failed for " + vmPath + " with error code: " + res);
				throw new JIException(res, "Cannot ModifyKvpItems to " + vmPath);
			}
		}
	}
}
