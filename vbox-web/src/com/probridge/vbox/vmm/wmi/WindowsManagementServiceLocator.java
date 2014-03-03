/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package com.probridge.vbox.vmm.wmi;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.IJIComObject;
import org.jinterop.dcom.core.JIClsid;
import org.jinterop.dcom.core.JIComServer;
import org.jinterop.dcom.core.JIString;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.vmm.wmi.utils.MyIJIDispatch;
import com.probridge.vbox.vmm.wmi.utils.Utils;
import com.probridge.vbox.vmm.wmi.utils.VirtualServiceException;

/**
 * Representation of winmngt of WMI Script engine ( see
 * http://msdn.microsoft.com/en-us/library/aa386179%28VS.85%29.aspx )
 */
public class WindowsManagementServiceLocator extends AbstractServiceLocator {
	private static final Logger logger = LoggerFactory.getLogger(WindowsManagementServiceLocator.class);
	private static final String namespace = "root\\cimv2";

	private static final String METHOD_CONNECT_SERVER = "connectServer";

	/**
	 * Instantiate a new WBemService Locator based on \root\cimv2 namespace
	 * 
	 * @throws UnknownHostException
	 * @throws JIException
	 */
	public WindowsManagementServiceLocator(String url) throws UnknownHostException, JIException {
		super(url);
	}

	/**
	 * Implement connection to \root\cimv2 namespace {@inheritDoc}
	 */
	@Override
	protected void connectServerImpl() throws UnknownHostException, JIException {
		JIComServer comServer = new JIComServer(JIClsid.valueOf(wbemLocator), this.url, session);
		IJIComObject wimObject = comServer.createInstance();
		IJIDispatch locatorDispatch = (IJIDispatch) JIObjectFactory.narrowObject((IJIComObject) wimObject
				.queryInterface(IJIDispatch.IID));
		Object[] params = new Object[] { new JIString(this.url), new JIString(namespace), JIVariant.OPTIONAL_PARAM(),
				JIVariant.OPTIONAL_PARAM(), JIVariant.OPTIONAL_PARAM(), JIVariant.OPTIONAL_PARAM(), new Integer(0),
				JIVariant.OPTIONAL_PARAM() };
		JIVariant[] res = locatorDispatch.callMethodA(METHOD_CONNECT_SERVER, params);
		service = (IJIDispatch) JIObjectFactory.narrowObject(res[0].getObjectAsComObject());
	}

	public boolean fileExists(String drive, String path, String fileName, String extension) throws JIException,
			VirtualServiceException {
		String query = "Select * From CIM_DataFile Where Drive='" + drive + "' And Path='"
				+ path.toLowerCase().replace("\\", "\\\\") + "' And FileName='" + fileName + "' And Extension='"
				+ extension + "'";
		logger.debug("Checking " + path.toLowerCase() + " - " + fileName + " - " + extension + " existance.");
		logger.debug("Query: " + query);
		JIVariant[] res = service.callMethodA("ExecQuery", new Object[] { new JIString(query) });
		JIVariant[][] fileSet = Utils.enumToJIVariantArray(res);
		return (fileSet.length == 1);
	}

	/**
	 * To delete a file on the managed host
	 * 
	 * @param path
	 *            an absolute path, filename and extension included
	 * @param fileName
	 *            the filename to remove
	 * @param extension
	 *            the extension of the file ( without dot )
	 * @return the execution result ( see
	 *         http://msdn.microsoft.com/en-us/library/aa389875(VS.85).aspx )
	 * @throws JIException
	 * @throws VirtualServiceException
	 */
	public int deleteFile(String drive, String path, String fileName, String extension) throws JIException,
			VirtualServiceException {
		String query = "Select * From CIM_DataFile Where Drive='" + drive + "' And Path='"
				+ path.toLowerCase().replace("\\", "\\\\") + "' And FileName='" + fileName + "' And Extension='"
				+ extension + "'";
		logger.debug("Deleting " + path.toLowerCase() + " - " + fileName + " - " + extension);
		logger.debug("Query: " + query);
		JIVariant[] res = service.callMethodA("ExecQuery", new Object[] { new JIString(query) });
		JIVariant[][] fileSet = Utils.enumToJIVariantArray(res);
		if (fileSet.length != 1) {
			throw new VirtualServiceException("Cannot identify the vhd to delete: " + path);
		}
		IJIDispatch fileDispatch = (IJIDispatch) JIObjectFactory.narrowObject(fileSet[0][0].getObjectAsComObject()
				.queryInterface(IJIDispatch.IID));

		res = fileDispatch.callMethodA("Delete", null);
		int result = res[0].getObjectAsInt();
		return result;
	}

	public int copyFile(String drive, String path, String fileName, String extension, String newName)
			throws JIException, VirtualServiceException {
		String query = "Select * From CIM_DataFile Where Drive='" + drive + "' And Path='"
				+ path.toLowerCase().replace("\\", "\\\\") + "' And FileName='" + fileName + "' And Extension='"
				+ extension + "'";
		logger.debug("Copying " + path.toLowerCase() + " - " + fileName + " - " + extension + " to [" + drive + path
				+ newName + "]");
		logger.debug("Query: " + query);
		JIVariant[] res = service.callMethodA("ExecQuery", new Object[] { new JIString(query) });
		JIVariant[][] fileSet = Utils.enumToJIVariantArray(res);
		if (fileSet.length != 1) {
			throw new VirtualServiceException("Cannot identify the vhd to delete: " + path);
		}
		IJIDispatch fileDispatch = (IJIDispatch) JIObjectFactory.narrowObject(fileSet[0][0].getObjectAsComObject()
				.queryInterface(IJIDispatch.IID));

		res = fileDispatch.callMethodA("Copy", new Object[] { new JIString(drive + path + newName) });
		int result = res[0].getObjectAsInt();
		return result;
	}

	public List<String> listFiles(String drive, String path, String extension) throws JIException {
		ArrayList<String> fileList = new ArrayList<String>();
		//
		String query = "Select * From CIM_DataFile Where Drive='" + drive + "' And Path='"
				+ path.toLowerCase().replace("\\", "\\\\") + "'";
		if (extension != null)
			query += " And Extension='" + extension + "'";
		//
		logger.debug("Listing file for " + drive + path.toLowerCase() + " - ext filter=" + extension + ".");
		logger.debug("Query: " + query);
		JIVariant[] res = service.callMethodA("ExecQuery", new Object[] { new JIString(query) });
		JIVariant[][] fileSet = Utils.enumToJIVariantArray(res);
		for (int i = 0; i < fileSet.length; i++) {
			MyIJIDispatch file = new MyIJIDispatch(fileSet[i][0]);
			String fileName = file.getString("FileName") + "." + file.getString("Extension");
			fileList.add(fileName);
		}
		return fileList;
	}

	public String getCpuInformation() throws JIException {
		String query = "select * from Win32_Processor";
		//
		JIVariant[] res = service.callMethodA("ExecQuery", new Object[] { new JIString(query) });
		JIVariant[][] cpuSet = Utils.enumToJIVariantArray(res);
		int numOfCpu = cpuSet.length;
		String cpuName = "";
		int numOfLogicalCpu = 0;
		for (int i = 0; i < numOfCpu; i++) {
			MyIJIDispatch cpu = new MyIJIDispatch(cpuSet[i][0]);
			cpuName = cpu.getString("Name");
			numOfLogicalCpu += cpu.getInt("NumberOfLogicalProcessors");
		}
		return cpuName + " 共" + numOfCpu + "颗  总核心数：" + numOfLogicalCpu;
	}

	/**
	 * Get host cpu utilization (without hyper-v guest usage - so not accurate)
	 * 
	 * @return utilization in percentage
	 * @throws JIException
	 */
	public int getHostCpuUtilization() throws JIException {
		String query = "select PercentProcessorTime from Win32_PerfFormattedData_PerfOS_Processor where Name='_Total'";
		//
		JIVariant[] res = service.callMethodA("ExecQuery", new Object[] { new JIString(query) });
		JIVariant[][] cpuSet = Utils.enumToJIVariantArray(res);
		int numOfEntry = cpuSet.length;
		int util = 0;
		for (int i = 0; i < numOfEntry; i++) {
			MyIJIDispatch cpu = new MyIJIDispatch(cpuSet[i][0]);
			util = Integer.parseInt(cpu.getString("PercentProcessorTime"));
		}
		return util;
	}

	/**
	 * @return { totalCapactiy, available } in bytes
	 * @throws JIException
	 */
	public long[] getMemoryInfo() throws JIException {
		String query = "select Capacity from Win32_PhysicalMemory";
		//
		JIVariant[] res = service.callMethodA("ExecQuery", new Object[] { new JIString(query) });
		JIVariant[][] memSet = Utils.enumToJIVariantArray(res);
		//
		int numOfMem = memSet.length;
		long numOfCapacity = 0;
		for (int i = 0; i < numOfMem; i++) {
			MyIJIDispatch mem = new MyIJIDispatch(memSet[i][0]);
			numOfCapacity += Long.parseLong(mem.getString("Capacity"));
		}
		String query2 = "select * from Win32_PerfFormattedData_PerfOS_Memory";
		//
		JIVariant[] res2 = service.callMethodA("ExecQuery", new Object[] { new JIString(query2) });
		JIVariant[][] memSet2 = Utils.enumToJIVariantArray(res2);
		int numOfMem2 = memSet2.length;
		long availableBytes = 0;
		for (int i = 0; i < numOfMem2; i++) {
			MyIJIDispatch mem = new MyIJIDispatch(memSet2[i][0]);
			availableBytes += Long.parseLong(mem.getString("AvailableBytes"));
		}
		return new long[] { numOfCapacity, availableBytes };
	}

	/**
	 * @param drive
	 *            like 'C:', 'D:' no slash
	 * @return {total,free} in bytes
	 * @throws JIException
	 */
	public long[] getDiskSize(String drive) throws JIException {
		String query = "select * from Win32_Volume where DriveLetter='" + drive + "'";
		//
		JIVariant[] res = service.callMethodA("ExecQuery", new Object[] { new JIString(query) });
		JIVariant[][] volSet = Utils.enumToJIVariantArray(res);
		int numOfVol = volSet.length;
		long totalBytes = 0;
		long freeBytes = 0;
		for (int i = 0; i < numOfVol; i++) {
			MyIJIDispatch vol = new MyIJIDispatch(volSet[i][0]);
			totalBytes += Long.parseLong(vol.getString("Capacity"));
			freeBytes += Long.parseLong(vol.getString("FreeSpace"));
		}
		return new long[] { totalBytes, freeBytes };
	}

	/**
	 * @param drive
	 * @return { secPerRead, secPerReadBase, secPerWrite, secPerWriteBase,
	 *         timebase }
	 * @throws JIException
	 */
	public long[] getDiskPerformance(String drive) throws JIException {
		String query = "select * from Win32_PerfRawData_PerfDisk_LogicalDisk where Name='" + drive + "'";
		//
		JIVariant[] res = service.callMethodA("ExecQuery", new Object[] { new JIString(query) });
		JIVariant[][] volSet = Utils.enumToJIVariantArray(res);
		int numOfVol = volSet.length;
		long secPerRead = 0;
		long secPerWrite = 0;
		long secPerReadBase = 0;
		long secPerWriteBase = 0;
		long timebase = 0;
		for (int i = 0; i < numOfVol; i++) {
			MyIJIDispatch vol = new MyIJIDispatch(volSet[i][0]);
			secPerRead = vol.getInt("AvgDisksecPerRead");
			secPerReadBase = vol.getInt("AvgDisksecPerRead_Base");
			secPerWrite = vol.getInt("AvgDisksecPerWrite");
			secPerWriteBase = vol.getInt("AvgDisksecPerWrite_Base");
			timebase = Long.parseLong(vol.getString("Frequency_PerfTime"));
		}
		//
		return new long[] { secPerRead, secPerReadBase, secPerWrite, secPerWriteBase, timebase };
	}

	/**
	 * @return OS info
	 * @throws JIException
	 */
	public String getOsInformation() throws JIException {
		String query = "select * from Win32_OperatingSystem";
		//
		JIVariant[] res = service.callMethodA("ExecQuery", new Object[] { new JIString(query) });
		JIVariant[][] osSet = Utils.enumToJIVariantArray(res);
		int numOfOs = osSet.length;
		String osInfo = "";
		for (int i = 0; i < numOfOs; i++) {
			MyIJIDispatch os = new MyIJIDispatch(osSet[i][0]);
			osInfo = os.getString("Caption") + os.getString("CSDVersion") + " " + os.getString("OSArchitecture");
		}
		return osInfo;
	}

	/**
	 * @return Hashmap key=status, value=count
	 * @throws JIException
	 */
	public HashMap<String, Integer> getHyperVSummary() throws JIException {
		String query = "select * from Win32_PerfFormattedData_VmmsVirtualMachineStats_HyperVVirtualMachineSummary";
		//
		JIVariant[] res = service.callMethodA("ExecQuery", new Object[] { new JIString(query) });
		JIVariant[][] summarySet = Utils.enumToJIVariantArray(res);
		int numOfEntries = summarySet.length;
		HashMap<String, Integer> summaryInfo = new HashMap<String, Integer>(11);
		synchronized (summaryInfo) {
			for (int i = 0; i < numOfEntries; i++) {
				MyIJIDispatch entry = new MyIJIDispatch(summarySet[i][0]);
				summaryInfo.put("正在运行", entry.getInt("Running"));
				summaryInfo.put("已保存", entry.getInt("Saved"));
				summaryInfo.put("已关机", entry.getInt("TurnedOff"));
				summaryInfo.put("已暂停", entry.getInt("Paused"));
				summaryInfo.put("正在保存", entry.getInt("Saving"));
				summaryInfo.put("正在恢复", entry.getInt("Resuming"));
				summaryInfo.put("正在开机", entry.getInt("Starting"));
				summaryInfo.put("正在关机", entry.getInt("Stopping"));
				summaryInfo.put("正在重启", entry.getInt("Resetting"));
				summaryInfo.put("正在暂停", entry.getInt("Pausing"));
				summaryInfo.put("等待启动", entry.getInt("WaitingtoStart"));
			}
		}
		return summaryInfo;
	}

	/**
	 * @return {numOfLogicalProcessor, numOfVirtualProcessor}
	 * @throws JIException
	 */
	public int[] getHyperVProcessorNum() throws JIException {
		String query = "select * from Win32_PerfFormattedData_HvStats_HyperVHypervisorLogicalProcessor where Name!='_Total'";
		JIVariant[] res = service.callMethodA("ExecQuery", new Object[] { new JIString(query) });
		JIVariant[][] LPSet = Utils.enumToJIVariantArray(res);
		int numOfLP = LPSet.length;
		//
		String query2 = "select * from Win32_PerfFormattedData_HvStats_HyperVHypervisorVirtualProcessor where Name!='_Total'";
		JIVariant[] res2 = service.callMethodA("ExecQuery", new Object[] { new JIString(query2) });
		JIVariant[][] VPSet = Utils.enumToJIVariantArray(res2);
		int numOfVP = VPSet.length;
		//
		return new int[] { numOfLP, numOfVP };
	}

	/**
	 * @return combined cpu utilization in percentage
	 * @throws JIException
	 */
	public int getCombinedCpuUtilization() throws JIException {
		String query = "select * from Win32_PerfFormattedData_HvStats_HyperVHypervisorLogicalProcessor where Name='_Total'";
		//
		JIVariant[] res = service.callMethodA("ExecQuery", new Object[] { new JIString(query) });
		JIVariant[][] cpuSet = Utils.enumToJIVariantArray(res);
		int numOfEntry = cpuSet.length;
		int util = 0;
		for (int i = 0; i < numOfEntry; i++) {
			MyIJIDispatch cpu = new MyIJIDispatch(cpuSet[i][0]);
			util = Integer.parseInt(cpu.getString("PercentTotalRunTime"));
		}
		return util;
	}

}
