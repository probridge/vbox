package com.probridge.vbox.vmm.wmi;

import static com.probridge.vbox.vmm.wmi.utils.Utils.enumToJIVariantArray;
import static org.jinterop.dcom.impls.JIObjectFactory.narrowObject;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.VMMapper;
import com.probridge.vbox.model.VM;
import com.probridge.vbox.model.VMExample;
import com.probridge.vbox.vmm.wmi.utils.MyIJIDispatch;
import com.probridge.vbox.vmm.wmi.utils.NotifierAdapter;
import com.probridge.vbox.vmm.wmi.utils.UpdatableService;
import com.probridge.vbox.vmm.wmi.utils.UpdateServiceEvent;
import com.probridge.vbox.vmm.wmi.utils.VirtualServiceException;

/**
 * Implementation of {@link VirtualMachineManager} for Microsoft Hyper-V.
 * 
 */
public class HyperVVMM extends NotifierAdapter<HyperVVMM, UpdateServiceEvent> implements VirtualMachineManager,
		UpdatableService {

	/** The class' logger */
	private static final Logger logger = LoggerFactory.getLogger(HyperVVMM.class);

	public static HyperVVMM[] hypervisors;

	public static void initialize() {
		try {
			logger.info("Initializing Hypervisor: " + VBoxConfig.hypervisorUrl);
			String[] hypervisorURLList = VBoxConfig.hypervisorUrl.split(",");
			String[] hypervisorConsoleList = VBoxConfig.hypervisorConsole.split(",");
			int numOfHypervisor = hypervisorURLList.length;
			//
			hypervisors = new HyperVVMM[numOfHypervisor];
			//
			for (int i = 0; i < numOfHypervisor; i++) {
				hypervisors[i] = new HyperVVMM(i, hypervisorURLList[i], hypervisorConsoleList[i]);
			}
			logger.info("All hypervisors initialized");
		} catch (VirtualServiceException e) {
			logger.error("Hypervisor Initialization Failed.", e);
		}
	}

	public static void disconnect() {
		logger.info("Disconnecting Hypervisor..." + VBoxConfig.hypervisorUrl);
		//
		for (int i = 0; i < hypervisors.length; i++) {
			hypervisors[i].service.destroySession();
			logger.info("Hypervisor[" + i + "] url=" + hypervisors[i].url + "] disconnected.");
		}
		logger.info("All hypervisors disconnected");
	}

	public static HyperVVMM getHyperVVMM(int index) {
		return hypervisors[index];
	}

	public static HyperVVM locateVM(String vmid) throws VirtualServiceException {
		for (int i = 0; i < hypervisors.length; i++) {
			Iterator<HyperVVM> iter = hypervisors[i].managedVms.values().iterator();
			while (iter.hasNext()) {
				HyperVVM thisVM = iter.next();
				if (thisVM.getID().equals(vmid))
					return thisVM;
			}
		}
		throw new VirtualServiceException("VM not found in all hypervisors");
	}

	// Object Members
	/**
	 * The {@link VirtualizationServiceLocator} used to manage the remote server
	 */
	private VirtualizationServiceLocator service;

	public String consoleUrl;

	public String url;

	public int vmmId;

	public boolean connected = false;

	public String hypervisorName = null;

	private HyperVVM templateVM = null;

	//
	private ArrayListMultimap<VirtualizationServiceLocator, HyperVVM> managedVms = ArrayListMultimap
			.<VirtualizationServiceLocator, HyperVVM> create();

	/**
	 * Public constructor
	 * 
	 * @throws VirtualServiceException
	 */
	private HyperVVMM(int vmmId, String url, String consoleUrl) throws VirtualServiceException {
		try {
			this.vmmId = vmmId;
			this.consoleUrl = consoleUrl;
			this.url = url;
			try {
				service = new VirtualizationServiceLocator(this.url);
			} catch (Exception e) {
				logger.error("Hypervisor[" + vmmId + "] not connected.", e);
				return;
			}
			//
			connected = true;
			//
			JIVariant[] vmEnum = service.execQuery("Select * From Msvm_ComputerSystem Where ProcessID=NULL");
			JIVariant[][] vmSet = enumToJIVariantArray(vmEnum);
			IJIDispatch hypervisorObjectDispatch = (IJIDispatch) narrowObject(vmSet[0][0].getObjectAsComObject()
					.queryInterface(IJIDispatch.IID));
			MyIJIDispatch hypervisorDispatch = new MyIJIDispatch(hypervisorObjectDispatch);
			hypervisorName = hypervisorDispatch.getString("ElementName");
			//
			templateVM = createVMInstanceByName(VBoxConfig.vmTemplateName);
			//
			logger.info("Hypervisor[" + vmmId + ":" + hypervisorName + "], url=" + url + "] initialized.");
			// Get belonging VM lists
			SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
			VMMapper mapper = session.getMapper(VMMapper.class);
			VMExample exp = new VMExample();
			exp.createCriteria().andVmHypervisorIdEqualTo(this.vmmId);
			List<VM> managedVmList = mapper.selectByExample(exp);
			session.close();
			//
			for (VM eachVM : managedVmList) {
				HyperVVM thisVm = createVMInstance(eachVM.getVmId());
				managedVms.put(service, thisVm);
			}
			logger.info("Hypervisor[" + vmmId + ":" + hypervisorName + "], " + managedVmList.size() + " VM registered");
		} catch (JIException e) {
			throw new VirtualServiceException(e, "Initializing VMM failed.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public HyperVVM createVMInstance(String vmid) throws VirtualServiceException {
		try {
			JIVariant[] vmEnum = service.execQuery("Select * From Msvm_ComputerSystem Where Name='" + vmid + "'");
			JIVariant[][] vmSet = enumToJIVariantArray(vmEnum);
			if (vmSet == null || vmSet.length < 1 || vmSet[0] == null || vmSet[0].length < 1) {
				throw new VirtualServiceException("Virtual machine " + vmid + " not found.");
			}
			if (vmSet.length > 1) {
				throw new VirtualServiceException("Serveral machine with name " + vmid + " found.");
			}
			IJIDispatch wbemObjectDispatch = (IJIDispatch) narrowObject(vmSet[0][0].getObjectAsComObject()
					.queryInterface(IJIDispatch.IID));
			HyperVVM res = new HyperVVM(service, wbemObjectDispatch, this);
			addNotifierListener(res);
			return res;
		} catch (JIException e) {
			throw new VirtualServiceException(e, "Unable to create virtual machine id=" + vmid + ".");
		}
	}

	public HyperVVM createVMInstanceByName(String vmName) throws VirtualServiceException {
		synchronized (service) {
			try {
				JIVariant[] vmEnum = service.execQuery("Select * From Msvm_ComputerSystem Where ElementName='" + vmName
						+ "'");
				JIVariant[][] vmSet = enumToJIVariantArray(vmEnum);
				if (vmSet == null || vmSet.length < 1 || vmSet[0] == null || vmSet[0].length < 1) {
					throw new VirtualServiceException("Virtual machine " + vmName + " not found.");
				}
				if (vmSet.length > 1) {
					throw new VirtualServiceException("Serveral machine with name " + vmName + " found.");
				}
				IJIDispatch wbemObjectDispatch = (IJIDispatch) narrowObject(vmSet[0][0].getObjectAsComObject()
						.queryInterface(IJIDispatch.IID));
				HyperVVM res = new HyperVVM(service, wbemObjectDispatch, this);
				addNotifierListener(res);
				return res;
			} catch (JIException e) {
				throw new VirtualServiceException(e, "Unable to retrieve virtual machine " + vmName + ".");
			}
		}
	}

	public HyperVVM getVMTemplate() {
		return templateVM;
	}

	public Collection<HyperVVM> getVirtualMachines() throws VirtualServiceException {
		return Collections.unmodifiableCollection(managedVms.values());
	}

	public void registerVM(HyperVVM newVM) {
		managedVms.put(service, newVM);
	}

	public void deregisterVM(HyperVVM deletedVM) {
		managedVms.remove(service, deletedVM);
	}

	public HyperVVM getVMInstance(String vmid) {
		return null;
	}

	/**
	 * This method is used to test network connection and remote service
	 * availability. In case of trouble, it will negotiate a new connection and
	 * ask every dependent entities to update potential network settings
	 * according to the new connection.
	 * 
	 * @throws VirtualServiceException
	 */
	private void testService() throws VirtualServiceException {
		try {
			service.execQuery("SELECT * FROM Msvm_ComputerSystem WHERE ProcessID = NULL");
			return;
		} catch (Exception e) {
			logger.debug("An error occured while testing service availability.");
		}
		final int tries = 2;
		for (int i = 0; i <= tries; i++) {
			try {
				this.service = new VirtualizationServiceLocator(this.url);
				service.execQuery("SELECT * FROM Msvm_ComputerSystem WHERE ProcessID = NULL");
				fire(new UpdateServiceEvent());
				return;
			} catch (Exception e) {
				if (i >= tries) {
					throw new VirtualServiceException(e, "Cannot negociate the WMI Session again.");
				}
				logger.debug("An attempt to negociate a new session failed.");
			}
		}
	}

	/*------------------------------------
	 * Getters, Setters and Helpers
	 *-----------------------------------*/

	/**
	 * {@link VirtualizationServiceLocator} getter
	 */
	public VirtualizationServiceLocator getServiceLocator() {
		return this.service;
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateServiceRequest() throws VirtualServiceException {
		testService();
	}
}
