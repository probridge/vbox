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
import java.util.logging.Level;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.common.JISystem;
import org.jinterop.dcom.core.JISession;
import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.vmm.wmi.utils.MyIJIDispatch;

/**
 * Parent Service Locator Class. A service locator implementation is a
 * consumption of a windows service, WbemScripting.SWbemLocator on
 * \root\virtualization or \root\cimv2 namespaces for example.
 */
public abstract class AbstractServiceLocator {

	private static final Logger logger = LoggerFactory.getLogger(AbstractServiceLocator.class);

	/** The Windows Wbem Script engine service locator */
	protected final String wbemLocator = "76a64158-cb41-11d1-8b02-00600806d9b6";

	/** The {@link MyIJIDispatch} instance associated with this service locator. */
	protected IJIDispatch service = null;
	/** The holding session */
	protected JISession session = null;
	/** Session url **/
	protected String url;

	/** Connection information */

	/**
	 * Public constructor
	 * 
	 * @throws UnknownHostException
	 * @throws JIException
	 */
	public AbstractServiceLocator(String url) throws UnknownHostException, JIException {
		JISystem.getLogger().setLevel(Level.WARNING);
		this.url = url;
		session = JISession.createSession(this.url, VBoxConfig.hypervisorUser, VBoxConfig.hypervisorPwd);
		connectServer();
	}

	/**
	 * Sets up the J-Interop environment and calls
	 * {@link AbstractServiceLocator#connectServerImpl(String, String, String)}
	 * 
	 * @throws UnknownHostException
	 * @throws JIException
	 */
	protected void connectServer() throws UnknownHostException, JIException {
		JISystem.setAutoRegisteration(true);
		try {
			connectServerImpl();
			return;
		} catch (Exception e) {
			logger.warn("Connection with autoregisteration has failed. Trying without autoregisteration");
		}
		JISystem.setAutoRegisteration(false);
		connectServerImpl();
	}

	/**
	 * To destroy an opened session
	 */
	public void destroySession() {
		try {
			JISession.destroySession(session);
			logger.debug("WMI Session destroyed.");
		} catch (JIException e) {
			try {
				JISession.destroySession(session);
			} catch (JIException e1) {
				logger.warn("Cannot destroy WMI Session.", e1);
			}
		}
	}

	/**
	 * This method is called when a J-Interop session is already opened, to
	 * connect to the good service locator. Some example of services:
	 * \root\virtualization ( especially for Microsoft Hyper-V ), \root\cimv2 (
	 * implementation of DMTF's CIM for management )...
	 * 
	 * @throws UnknownHostException
	 * @throws JIException
	 */
	abstract protected void connectServerImpl() throws UnknownHostException, JIException;
}
