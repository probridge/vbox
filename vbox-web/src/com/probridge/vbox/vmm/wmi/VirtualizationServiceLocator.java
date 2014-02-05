package com.probridge.vbox.vmm.wmi;

import static com.probridge.vbox.vmm.wmi.utils.Utils.enumToJIVariantArray;

import java.net.UnknownHostException;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.IJIComObject;
import org.jinterop.dcom.core.JIClsid;
import org.jinterop.dcom.core.JIComServer;
import org.jinterop.dcom.core.JIString;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.jinterop.dcom.impls.automation.IJIDispatch;

import com.probridge.vbox.vmm.wmi.utils.Utils;

/**
 * Representation of Virtualization role of WMI Script engine ( see
 * http://msdn.microsoft.com/en-us/library/aa155217.aspx )
 */
public class VirtualizationServiceLocator extends AbstractServiceLocator {

	private static final String namespace = "root\\virtualization";

	private static final String METHOD_CONNECT_SERVER = "connectServer";
	private static final String METHOD_INSTANCES_OF = "InstancesOf";
	private static final String METHOD_EXEC_QUERY = "ExecQuery";

	private static final int RETURN_IMMEDIATE = 0x10;
	private static final int FORWARD_ONLY = 0x20;

	private MsvmVirtualSystemManagementService virtualSystemManagementService = null;
	private MsvmImageManagementService imageManagementService = null;
	private MsvmVirtualSwitchManagementService virtualSwitchManagementService = null;

	/**
	 * Instantiate a new WBemService Locator based on \root\virtualization
	 * namespace
	 * 
	 * @throws UnknownHostException
	 * @throws JIException
	 */
	public VirtualizationServiceLocator(String url)
			throws UnknownHostException, JIException {
		super(url);
	}

	/**
	 * Enumerate all instances of the String parameter. Must be followed by a
	 * call to {@link Utils#enumToJIVariantArray(JIVariant[])} if you want to
	 * get an array, otherwise, this method returns an enum object
	 * 
	 * @param type
	 *            the wanted type of instance
	 * @return an enum representing the reulst
	 * @throws JIException
	 */
	public JIVariant[] instancesOf(String type) throws JIException {
		IJIDispatch servicesDispatch = (IJIDispatch) JIObjectFactory
				.narrowObject(service.queryInterface(IJIDispatch.IID));
		return servicesDispatch.callMethodA(METHOD_INSTANCES_OF,
				new Object[] { new JIVariant(new JIString(type)) });
	}

	/**
	 * Returns the class definition representing the String parameter
	 * 
	 * @param instance
	 *            the type of class.
	 * @return the associated class definition on remote host
	 * @throws JIException
	 */
	public IJIDispatch get(String instance) throws JIException {
		return (IJIDispatch) JIObjectFactory.narrowObject(service.callMethodA(
				"Get", new Object[] { new JIString(instance) })[0]
				.getObjectAsComObject().queryInterface(IJIDispatch.IID));
	}

	/**
	 * Execute a wql on the remote host with "return immediate" and
	 * "forward only"
	 * 
	 * @param wql
	 * @return the wql result
	 * @throws JIException
	 */
	public JIVariant[] execQuery(String wql) throws JIException {
		Object[] params = new Object[] { new JIString(wql),
				JIVariant.OPTIONAL_PARAM(),
				new JIVariant(new Integer(RETURN_IMMEDIATE + FORWARD_ONLY)) };
		return service.callMethodA(METHOD_EXEC_QUERY, params);
	}

	/**
	 * To get the Msvm_VirtualSystemManagementService singleton
	 * 
	 * @return the wanted virtual system management service
	 * @throws JIException
	 */
	public MsvmVirtualSystemManagementService getVirtualSystemManagementService()
			throws JIException {
		if (virtualSystemManagementService == null) {
			JIVariant[] tmp = instancesOf("Msvm_VirtualSystemManagementService");
			JIVariant[][] instances = enumToJIVariantArray(tmp);
			IJIDispatch virtualSystemManagementServiceDispatch = (IJIDispatch) JIObjectFactory
					.narrowObject(instances[0][0].getObjectAsComObject()
							.queryInterface(IJIDispatch.IID));
			virtualSystemManagementService = new MsvmVirtualSystemManagementService(
					virtualSystemManagementServiceDispatch, this);
		}
		return virtualSystemManagementService;
	}

	/**
	 * To get the Msvm_ImageManagementService singleton
	 * 
	 * @return the wanted image management service
	 * @throws JIException
	 */
	public MsvmImageManagementService getImageManagementService()
			throws JIException {
		if (imageManagementService == null) {
			JIVariant[] tmp = instancesOf("Msvm_ImageManagementService");
			JIVariant[][] imageManagementServiceSet = enumToJIVariantArray(tmp);
			IJIDispatch imageManagementServiceDispatch = (IJIDispatch) JIObjectFactory
					.narrowObject(imageManagementServiceSet[0][0]
							.getObjectAsComObject().queryInterface(
									IJIDispatch.IID));
			imageManagementService = new MsvmImageManagementService(
					imageManagementServiceDispatch, this);
		}
		return imageManagementService;
	}

	/**
	 * To get the Msvm_VirtualSwitchManagementService singleton
	 * 
	 * @return the wanted virtual switch management service
	 * @throws JIException
	 */
	public MsvmVirtualSwitchManagementService getVirtualSwitchManagementService()
			throws JIException {
		if (virtualSwitchManagementService == null) {
			JIVariant[] tmp = instancesOf("Msvm_VirtualSwitchManagementService");
			JIVariant[][] virtualSwitchManagementServiceSet = enumToJIVariantArray(tmp);
			IJIDispatch virtualSwitchManagementServiceDispatch = (IJIDispatch) JIObjectFactory
					.narrowObject(virtualSwitchManagementServiceSet[0][0]
							.getObjectAsComObject().queryInterface(
									IJIDispatch.IID));
			virtualSwitchManagementService = new MsvmVirtualSwitchManagementService(
					virtualSwitchManagementServiceDispatch);
		}
		return virtualSwitchManagementService;
	}

	/**
	 * Implement connection to \root\virtualization namespace {@inheritDoc}
	 */
	@Override
	protected void connectServerImpl() throws UnknownHostException, JIException {
		JIComServer comServer = new JIComServer(JIClsid.valueOf(wbemLocator),
				this.url, session);
		IJIComObject wimObject = comServer.createInstance();
		IJIDispatch locatorDispatch = (IJIDispatch) JIObjectFactory
				.narrowObject((IJIComObject) wimObject
						.queryInterface(IJIDispatch.IID));
		Object[] params = new Object[] { new JIString(this.url),
				new JIString(namespace), JIVariant.OPTIONAL_PARAM(),
				JIVariant.OPTIONAL_PARAM(), JIVariant.OPTIONAL_PARAM(),
				JIVariant.OPTIONAL_PARAM(), new Integer(0),
				JIVariant.OPTIONAL_PARAM() };
		JIVariant[] res = locatorDispatch.callMethodA(METHOD_CONNECT_SERVER,
				params);
		service = (IJIDispatch) JIObjectFactory.narrowObject(res[0]
				.getObjectAsComObject());
	}
}
