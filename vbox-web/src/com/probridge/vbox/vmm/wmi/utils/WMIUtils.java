package com.probridge.vbox.vmm.wmi.utils;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Level;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.common.JISystem;
import org.jinterop.dcom.core.IJIComObject;
import org.jinterop.dcom.core.JIArray;
import org.jinterop.dcom.core.JIClsid;
import org.jinterop.dcom.core.JIComServer;
import org.jinterop.dcom.core.JISession;
import org.jinterop.dcom.core.JIString;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.jinterop.dcom.impls.automation.IJIEnumVariant;

public class WMIUtils {

	private static final String SWBEM_LOCATOR = "76A64158-CB41-11d1-8B02-00600806D9B6";
	private static final String SWBEM_LOCATOR_INTERFACE = "76A6415B-CB41-11d1-8B02-00600806D9B6";
	private JIComServer comStub = null;
	private IJIComObject comObject = null;
	private IJIDispatch dispatch = null;
	private JISession session = null;
	private IJIDispatch wbemServices_dispatch = null;
	//
	private String win32_namespace = "ROOT\\CIMV2";
	//
	private static String domainName = "localhost";
	private static String userName = "PennyGe";
	private static String password = "820928";
	private static String hostIP = "192.168.1.3";

	public WMIUtils(String namespace) throws SecurityException, IOException {
		JISystem.setInBuiltLogHandler(true);
		JISystem.getLogger().setLevel(Level.WARNING);
		JISystem.setAutoRegisteration(true);
		win32_namespace = namespace;
	}

	public void init() throws JIException, UnknownHostException, IOException {
		//
		session = JISession.createSession(domainName, userName, password);
		// session.useSessionSecurity(false);
		// session.setGlobalSocketTimeout(5000);
		//
		comStub = new JIComServer(JIClsid.valueOf(SWBEM_LOCATOR), hostIP,
				session);
		// comStub = new JIComServer(valueOf("WbemScripting.SWbemLocator"),
		// hostIP, dcomSession);
		//
		IJIComObject unknown = comStub.createInstance();
		comObject = (IJIComObject) unknown
				.queryInterface(SWBEM_LOCATOR_INTERFACE);// ISWbemLocator
		// This will obtain the dispatch interface
		dispatch = (IJIDispatch) JIObjectFactory.narrowObject(comObject
				.queryInterface(IJIDispatch.IID));
		// Connect Server
		JIVariant results[] = dispatch.callMethodA(
				"ConnectServer",
				new Object[] {
						JIVariant.OPTIONAL_PARAM(), // strNamespace
						new JIString(win32_namespace),
						JIVariant.OPTIONAL_PARAM(), // strUser
						JIVariant.OPTIONAL_PARAM(),// strPassword
						JIVariant.OPTIONAL_PARAM(), // strLocale
						JIVariant.OPTIONAL_PARAM(),// strAuthority
						new Integer(0), // iSecurityFlags
						JIVariant.OPTIONAL_PARAM() // objwbemNamedValueSet
				});
		wbemServices_dispatch = (IJIDispatch) JIObjectFactory
				.narrowObject((results[0]).getObjectAsComObject());
	}

	public JIVariant[] ExecQuery(String query) throws JIException,
			UnknownHostException, IOException {
		JIVariant[] results = wbemServices_dispatch
				.callMethodA("ExecQuery", new Object[] { new JIString(query),
						JIVariant.OPTIONAL_PARAM(), JIVariant.OPTIONAL_PARAM(),
						JIVariant.OPTIONAL_PARAM() });
		return results;
	}

	public void free() {
		try {
			JISession.destroySession(session);
		} catch (JIException e) {
			e.printStackTrace();
		}
	}

	public static int getObjectSetCount(JIVariant[] objSets) throws JIException {
		IJIDispatch wbemObjectSet_dispatch = (IJIDispatch) JIObjectFactory
				.narrowObject((objSets[0]).getObjectAsComObject());
		JIVariant Count = wbemObjectSet_dispatch.get("Count");
		return Count.getObjectAsInt();
	}

	public static IJIEnumVariant getObjectSetEnum(JIVariant[] objSets)
			throws JIException {
		IJIDispatch wbemObjectSet_dispatch = (IJIDispatch) JIObjectFactory
				.narrowObject((objSets[0]).getObjectAsComObject());
		IJIComObject obj = wbemObjectSet_dispatch.get("_NewEnum")
				.getObjectAsComObject();
		IJIEnumVariant enumVARIANT = (IJIEnumVariant) JIObjectFactory
				.narrowObject(obj.queryInterface(IJIEnumVariant.IID));
		return enumVARIANT;
	}

	public static String getObjectText(JIVariant obj) throws JIException {
		IJIDispatch wbemObject_dispatch = (IJIDispatch) JIObjectFactory
				.narrowObject(((JIVariant) obj).getObjectAsComObject());
		JIVariant variant2 = (JIVariant) (wbemObject_dispatch.callMethodA(
				"GetObjectText_", new Object[] { new Integer(1) }))[0];
		return variant2.getObjectAsString().getString();
	}
	
	public static String getPropertyAsString(JIVariant obj, String property) throws JIException {
		IJIDispatch wbemObject_dispatch = (IJIDispatch) JIObjectFactory
				.narrowObject(((JIVariant) obj).getObjectAsComObject());
		return wbemObject_dispatch.get(property).getObjectAsString2();
	}

	public static int getPropertyAsInt(JIVariant obj, String property) throws JIException {
		IJIDispatch wbemObject_dispatch = (IJIDispatch) JIObjectFactory
				.narrowObject(((JIVariant) obj).getObjectAsComObject());
		return wbemObject_dispatch.get(property).getObjectAsInt();
	}
	
	public static long getPropertyAsLong(JIVariant obj, String property) throws JIException {
		IJIDispatch wbemObject_dispatch = (IJIDispatch) JIObjectFactory
				.narrowObject(((JIVariant) obj).getObjectAsComObject());
		return wbemObject_dispatch.get(property).getObjectAsLong();
	}
	
	public static int invokeMethod(JIVariant obj, String method)
			throws JIException {
		IJIDispatch wbemObjectDispatch = (IJIDispatch) JIObjectFactory
				.narrowObject(((JIVariant) obj).getObjectAsComObject());
		JIVariant returnStatus = wbemObjectDispatch.callMethodA(method);
		return returnStatus.getObjectAsInt();
	}
	
	public static int invokeMethod(JIVariant obj, String method, Object[] params)
			throws JIException {
		IJIDispatch wbemObjectDispatch = (IJIDispatch) JIObjectFactory
				.narrowObject(((JIVariant) obj).getObjectAsComObject());
		JIVariant[] returnStatus = wbemObjectDispatch.callMethodA(method,params);
		return returnStatus[0].getObjectAsInt();
	}

	public static JIVariant getEnumNext(IJIEnumVariant evar) throws JIException {
		return ((JIVariant[]) ((JIArray) evar.next(1)[0]).getArrayInstance())[0];
	}

	public static void main(String[] args) throws UnknownHostException,
			JIException, IOException, InterruptedException {
		WMIUtils test = new WMIUtils("root\\virtualization");
		test.init();
		/*
		 * JIVariant[] services = test
		 * .ExecQuery("SELECT * FROM Win32_Service where Name='AdobeARMservice'"
		 * ); int c = getObjectSetCount(services); IJIEnumVariant eVar =
		 * getObjectSetEnum(services); // for (int i = 0; i < c; i++) {
		 * JIVariant[] arrayObj = getEnumNext(eVar);
		 * System.out.println(getObjectText((JIVariant) arrayObj[0])); for (int
		 * j = 0; j < 10; j++) { System.out.println(invokeMethod(arrayObj[0],
		 * "StartService")); Thread.sleep(3000);
		 * System.out.println(invokeMethod(arrayObj[0], "StopService"));
		 * Thread.sleep(3000); } }
		 */
		// JIVariant[] osinfo = test
		// .ExecQuery("SELECT * FROM Msvm_VirtualSystemManagementService");
		JIVariant[] osinfo = test
				.ExecQuery("Select * From Msvm_SwitchPort");
		// JIVariant[] osinfo = test
		// .ExecQuery("SELECT * FROM Msvm_HostedDependency");

		// JIVariant[] osinfo =
		// test.ExecQuery("SELECT * FROM Msvm_ResourceAllocationSettingData");
		// JIVariant[] osinfo =
		// test.ExecQuery("SELECT * FROM Msvm_VirtualSystemGlobalSettingData");

		int cc = getObjectSetCount(osinfo);
		System.out.println("Count=" + cc);
		IJIEnumVariant eVarc = getObjectSetEnum(osinfo);
		//
		for (int i = 0; i < cc; i++) {
			JIVariant arrayObj = getEnumNext(eVarc);
			System.out.println(getObjectText(arrayObj));
		}
		//
		test.free();
	}
}
