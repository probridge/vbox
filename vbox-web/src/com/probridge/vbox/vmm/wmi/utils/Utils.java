package com.probridge.vbox.vmm.wmi.utils;

import static org.jinterop.dcom.impls.JIObjectFactory.narrowObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.IJIComObject;
import org.jinterop.dcom.core.JIArray;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.jinterop.dcom.impls.automation.IJIEnumVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.probridge.vbox.vmm.wmi.VirtualizationServiceLocator;

/**
 * Utility class
 */
public class Utils {

	private static final Logger logger = LoggerFactory.getLogger(Utils.class);

	/**
	 * To convert the result of a "Select" or "Associators Of" request from a
	 * IJIComObject to an enum.
	 * 
	 * @param toConvert
	 * @return
	 * @throws JIException
	 */
	public static JIVariant[][] enumToComObjectArray(IJIComObject toConvert) throws JIException {
		IJIDispatch toConvertDispatch = (IJIDispatch) narrowObject(toConvert.queryInterface(IJIDispatch.IID));
		JIVariant variant = toConvertDispatch.get("_NewEnum");
		IJIComObject object2 = variant.getObjectAsComObject();
		IJIEnumVariant enumVariant = (IJIEnumVariant) narrowObject(object2.queryInterface(IJIEnumVariant.IID));
		JIVariant Count = toConvertDispatch.get("Count");
		int count = Count.getObjectAsInt();
		JIVariant[][] res = new JIVariant[count][];
		for (int i = 0; i < count; i++) {
			Object values[] = enumVariant.next(1);
			JIArray array = (JIArray) values[0];
			res[i] = (JIVariant[]) array.getArrayInstance();
		}
		return res;
	}

	/**
	 * To convert the result of a "Select" or "Associators Of" to an enum
	 * 
	 * @param set
	 * @return
	 * @throws JIException
	 */
	public static JIVariant[][] enumToJIVariantArray(JIVariant[] set) throws JIException {
		IJIDispatch toConvertDispatch = (IJIDispatch) narrowObject(set[0].getObjectAsComObject().queryInterface(
				IJIDispatch.IID));
		JIVariant toConvertVariant = toConvertDispatch.get("_NewEnum");
		IJIComObject toConvertComObject = toConvertVariant.getObjectAsComObject();
		IJIEnumVariant toConvertEnumVariant = (IJIEnumVariant) narrowObject(toConvertComObject
				.queryInterface(IJIEnumVariant.IID));
		ArrayList<JIVariant[]> res = new ArrayList<JIVariant[]>();
		int i = 0, threshold = 1000000; // to avoid infinite loop, in both msdn
										// and j-interop nothing is said about
										// enumeration
		while (true) {
			Object[] values = null;
			try {
				values = toConvertEnumVariant.next(1);
			} catch (JIException e) {
				break;
			}
			if (values != null) {
				JIArray array = (JIArray) values[0];
				res.add((JIVariant[]) array.getArrayInstance());
			}
			i++;
			if (i >= threshold) {
				break;
			}
		}
		return res.toArray(new JIVariant[res.size()][]);
	}

	public static void monitorJobState(String jobPath, VirtualizationServiceLocator service) throws JIException {
		int jobState = 0;
		IJIDispatch jobDispatch = null;
		while (true) {
			jobDispatch = service.get(jobPath);
			jobState = jobDispatch.get("JobState").getObjectAsInt();
			if (jobState != 3 && jobState != 4) {
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.warn("Monitoring Job interrupted but ignored.");
			}
		}
		if (jobState != 7) {
			jobDispatch = service.get(jobPath);
			int errorCode = jobDispatch.get("ErrorCode").getObjectAsInt();
			String errorDesc = jobDispatch.get("ErrorDescription").getObjectAsString2();
			String ls = System.getProperty("line.separator");
			logger.error("Monitoring job result error:" + ls + "\tErrorCode: " + errorCode + " ErrorDescription: "
					+ errorDesc);
			throw new JIException(errorCode, "Job finished with an error." + ls + "ErrorCode: " + errorCode
					+ ". ErrorDescription: " + errorDesc);
		}
	}

	public static String getStringProperty(String xmlObject, String name) throws ParserConfigurationException,
			SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new ByteArrayInputStream(xmlObject.getBytes()));
		Element root = doc.getDocumentElement();
		NodeList properties = root.getElementsByTagName("PROPERTY");
		for (int i = 0; i < properties.getLength(); i++) {
			Element property = (Element) properties.item(i);
			String attr = property.getAttribute("NAME");
			if (attr != null && attr.equals(name)) {
				NodeList values = property.getChildNodes();
				for (int j = 0; j < values.getLength(); j++) {
					Element value = (Element) values.item(j);
					if (value.getNodeName().equals("VALUE")) {
						return value.getTextContent();
					}
				}
			}
		}
		return null;
	}
	
	public static void sleepCheck(int timeout, AtomicBoolean flag) throws InterruptedException {
		int timer = 0;
		while (timer < timeout) {
			Thread.sleep(1000);
			timer++;
			if (flag.get())
				throw new InterruptedException("Interrupt Request Received");
		}
	}
}
