package com.probridge.vbox.vmm.wmi;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.JIString;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MsvmVirtualSwitchManagementService {
	private static final Logger logger = LoggerFactory.getLogger(MsvmVirtualSwitchManagementService.class);
	private IJIDispatch msvmVSMSDispatch = null;

	MsvmVirtualSwitchManagementService(IJIDispatch dispatch) {
		this.msvmVSMSDispatch = dispatch;
	}

	/**
	 * Tries to create a new Msvm_SwitchPort
	 * 
	 * @param virtualSwitchRef
	 * @param friendlyName
	 * @param name
	 * @param scopeOfResidence
	 * @return 0 if success or the error code
	 * @throws JIException
	 */
	public int createSwitchPort(String virtualSwitchRef, String friendlyName, String name, String scopeOfResidence)
			throws JIException {
		logger.debug("Creating switch port " + name + " on " + virtualSwitchRef);
		JIVariant[] tmp = msvmVSMSDispatch.callMethodA("CreateSwitchPort", new Object[] {
				new JIString(virtualSwitchRef), new JIString(name), new JIString(friendlyName),
				new JIString(scopeOfResidence) });
		int result = tmp[0].getObjectAsInt();
		return result;
	}

	/**
	 * Tries to create a new Msvm_SwitchPort
	 * 
	 * @param virtualSwitchRef
	 * @param friendlyName
	 * @param name
	 * @param scopeOfResidence
	 * @return 0 if success or the error code
	 * @throws JIException
	 */
	public int deleteSwitchPort(String virtualSwitchPortRef) throws JIException {
		logger.debug("deleting switch port " + virtualSwitchPortRef);
		JIVariant[] tmp = msvmVSMSDispatch.callMethodA("DeleteSwitchPort", new Object[] { new JIString(
				virtualSwitchPortRef) });
		int result = tmp[0].getObjectAsInt();
		return result;
	}
}
