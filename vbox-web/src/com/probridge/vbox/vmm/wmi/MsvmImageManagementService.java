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

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.JIString;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.vmm.wmi.utils.MyIJIWinReg;
import com.probridge.vbox.vmm.wmi.utils.Utils;
import com.probridge.vbox.vmm.wmi.utils.VirtualServiceException;

/**
 * Representation of
 * http://msdn.microsoft.com/en-us/library/cc136845(VS.85).aspx
 */
public class MsvmImageManagementService {

	private static final Logger logger = LoggerFactory.getLogger(MsvmImageManagementService.class);

	private IJIDispatch msvmIMSDispatch = null;
	private VirtualizationServiceLocator serviceLocator = null;

	private static final String VOLATILE_ENVIRONMENT = "Volatile Environment";
	private static final String ENVIRONMENT = "Environment";

	MsvmImageManagementService(IJIDispatch dispatch, VirtualizationServiceLocator locator) {
		this.msvmIMSDispatch = dispatch;
		this.serviceLocator = locator;
	}

	/**
	 * Creates a differencing virtual hard disk file based on the second
	 * parameter.
	 * 
	 * @param result
	 *            the name of the resulting file
	 * @param parent
	 *            the name of the parent file
	 * @throws JIException
	 *             if the operation fails
	 */
	public void createDifferencingVirtualHardDisk(String result, String parent) throws JIException {
		JIVariant[] tmp = msvmIMSDispatch.callMethodA("CreateDifferencingVirtualHardDisk", new Object[] {
				new JIString(result), new JIString(parent), JIVariant.EMPTY_BYREF() });
		int createDiffVHDRes = tmp[0].getObjectAsInt();
		if (createDiffVHDRes == 0) {
			logger.debug("diff disk created successfully.");
		} else {
			if (createDiffVHDRes == 4096) {
				logger.debug("creating diff disk...");
				String jobPath = tmp[1].getObjectAsVariant().getObjectAsString2();
				Utils.monitorJobState(jobPath, serviceLocator);
				logger.debug("diff disk creation succeed");
			} else {
				logger.error("diff disk creation of " + result + " failed!");
				throw new JIException(createDiffVHDRes, "Cannot create VHD file " + result + " from " + parent);
			}
		}
		logger.debug("diff disk creation completed successfully!!");
	}

	/**
	 * Creates a differencing virtual hard disk file based on the second
	 * parameter.
	 * 
	 * @param result
	 *            the name of the resulting file
	 * @param parent
	 *            the name of the parent file
	 * @throws JIException
	 *             if the operation fails
	 */
	public void convertVirtualHardDisk(String source, String dest, int type) throws JIException {
		JIVariant[] tmp = msvmIMSDispatch.callMethodA("ConvertVirtualHardDisk", new Object[] { new JIString(source),
				new JIString(dest), new JIVariant(type), JIVariant.EMPTY_BYREF() });
		int convertVHDRes = tmp[0].getObjectAsInt();
		if (convertVHDRes == 0) {
			logger.debug("convert disk created successfully.");
		} else {
			if (convertVHDRes == 4096) {
				logger.debug("coverting disk...");
				String jobPath = tmp[1].getObjectAsVariant().getObjectAsString2();
				Utils.monitorJobState(jobPath, serviceLocator);
				logger.debug("convert disk succeed");
			} else {
				logger.error("convert disk failed!");
				throw new JIException(convertVHDRes, "Cannot convert VHD file from " + source + " from " + dest);
			}
		}
		logger.debug("disk convert completed successfully!!");
	}

	public void expandVirtualHardDisk(String path, long size) throws JIException {
		JIVariant[] tmp = msvmIMSDispatch.callMethodA("ExpandVirtualHardDisk", new Object[] { new JIString(path),
				new JIVariant(new JIString(String.valueOf(size))), JIVariant.EMPTY_BYREF() });
		int convertVHDRes = tmp[0].getObjectAsInt();
		if (convertVHDRes == 0) {
			logger.debug("expand disk successfully.");
		} else {
			if (convertVHDRes == 4096) {
				logger.debug("expanding disk...");
				String jobPath = tmp[1].getObjectAsVariant().getObjectAsString2();
				Utils.monitorJobState(jobPath, serviceLocator);
				logger.debug("disk expand succeed");
			} else {
				logger.error("disk expandsion of failed!");
				throw new JIException(convertVHDRes, "Cannot expand VHD file " + path + " to " + size);
			}
		}
		logger.debug("expand disk completed successfully!!");
	}

	public void createDynamicVirtualHardDisk(String path, long size) throws JIException {
		JIVariant[] tmp = msvmIMSDispatch.callMethodA("CreateDynamicVirtualHardDisk", new Object[] {
				new JIString(path), new JIVariant(new JIString(String.valueOf(size))), JIVariant.EMPTY_BYREF() });
		int convertVHDRes = tmp[0].getObjectAsInt();
		if (convertVHDRes == 0) {
			logger.debug("dynamic disk created successfully.");
		} else {
			if (convertVHDRes == 4096) {
				logger.debug("creating dynamic disk...");
				String jobPath = tmp[1].getObjectAsVariant().getObjectAsString2();
				Utils.monitorJobState(jobPath, serviceLocator);
				logger.debug("dynamic disk creation succeed");
			} else {
				logger.error("dynamic creation failed!");
				throw new JIException(convertVHDRes, "Cannot create VHD file " + path + " of " + size);
			}
		}
		// logger.debug("dynamic disk creation completed successfully!!");
	}

	/**
	 * Uses {@link MyIJIWinReg} to retrieve user temp folder on remote server.
	 * 
	 * @return the path of user's temp folder on remote server
	 * @throws UnknownHostException
	 * @throws VirtualServiceException
	 */
	public String getUserTempFolder() throws VirtualServiceException {
		MyIJIWinReg winReg;
		try {
			winReg = new MyIJIWinReg(serviceLocator.url, VBoxConfig.hypervisorUser, VBoxConfig.hypervisorPwd);
		} catch (UnknownHostException e1) {
			throw new VirtualServiceException(e1, "Cannot determine user's temp folder on " + serviceLocator.url);
		}
		String userTempRel = null;
		try {
			userTempRel = winReg.readHKCU(ENVIRONMENT, "TEMP", 1024)[0];
			if (userTempRel == null || userTempRel.equals("")) {
				userTempRel = winReg.readHKCU(ENVIRONMENT, "TMP", 1024)[0];
				if (userTempRel == null || userTempRel.equals("")) {
					throw new VirtualServiceException("Invalid Environment\\TEMP value for HKEY_CURRENT_USER on "
							+ serviceLocator.url);
				}
			}
		} catch (JIException e) {
			throw new VirtualServiceException(e, "Cannot read Environment\\TEMP on remote computer "
					+ serviceLocator.url);
		}
		String userProfile = null;
		try {
			userProfile = winReg.readHKCU(VOLATILE_ENVIRONMENT, "USERPROFILE", 1024)[0];
			if (userProfile != null && !userProfile.equals("")) {
				return (userProfile + userTempRel).replace("%USERPROFILE%", "");
			}
		} catch (JIException e) {
			// miam miam...will try with user drive et user home
		}
		try {
			String userDrive = winReg.readHKCU(VOLATILE_ENVIRONMENT, "HOMEDRIVE", 1024)[0];
			String userHome = winReg.readHKCU(VOLATILE_ENVIRONMENT, "HOMEPATH", 1024)[0];
			if (userDrive == null || userDrive.equals("") || userHome == null || userHome.equals("")) {
				throw new VirtualServiceException("Cannot determine user temp directory on " + serviceLocator.url);
			} else {
				return (userDrive + userHome + userTempRel).replace("%USERPROFILE%", "");
			}
		} catch (JIException e) {
			throw new VirtualServiceException(e, "An exception occured while reading HKEY_CURRENT_USER "
					+ VOLATILE_ENVIRONMENT);
		}
	}
}
