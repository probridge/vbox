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
package com.probridge.vbox.vmm.wmi.utils;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.jinterop.dcom.impls.automation.IJIDispatch;

/**
 * A helper class that wrap J-Interop IJIDispatch
 */
public class MyIJIDispatch {
	private final IJIDispatch dispatch;

	/**
	 * Creates a new {@link MyIJIDispatch} based on an existing an functional
	 * IJIDispatch
	 * 
	 * @param dispatch
	 */
	public MyIJIDispatch(IJIDispatch dispatch) {
		this.dispatch = dispatch;
	}

	/**
	 * Creates a new {@link MyIJIDispatch} based on a JIVariant instance. First
	 * querying IJIDispatch interface on associated com object and calls
	 * JIObjectFactory.narrowObject on the result.
	 * 
	 * @param dispatch
	 *            the com object to wrap
	 * @throws JIException
	 */
	public MyIJIDispatch(JIVariant dispatch) throws JIException {
		this.dispatch = (IJIDispatch) JIObjectFactory.narrowObject(dispatch.getObjectAsComObject().queryInterface(
				IJIDispatch.IID));
	}

	public IJIDispatch getBase() {
		return dispatch;
	}

	/**
	 * to update a property value owned by the wrapped com object
	 * 
	 * @param property
	 *            the name of the property
	 * @param value
	 *            the new value
	 * @throws JIException
	 */
	public void put(String property, JIVariant value) throws JIException {
		this.dispatch.put(property, value);
	}

	/**
	 * To get a property as a string
	 * 
	 * @param property
	 *            the name of the property
	 * @return the string value of the property
	 * @throws JIException
	 */
	public String getString(String property) throws JIException {
		return this.dispatch.get(property).getObjectAsString2();
	}

	/**
	 * To get a property as a boolean
	 * 
	 * @param property
	 *            the name of the property
	 * @return the boolean value of the property
	 * @throws JIException
	 */
	public boolean getBoolean(String property) throws JIException {
		return this.dispatch.get(property).getObjectAsBoolean();
	}

	/**
	 * To clone the com object. On the com server, an identical object is
	 * created with different UUID. ( from SWbemObject )
	 * 
	 * @return the newly created object
	 * @throws JIException
	 */
	public MyIJIDispatch clone_() throws JIException {
		JIVariant[] tmp = this.dispatch.callMethodA("Clone_", null);
		return new MyIJIDispatch((IJIDispatch) JIObjectFactory.narrowObject(tmp[0].getObjectAsComObject()
				.queryInterface(IJIDispatch.IID)));
	}

	/**
	 * This object is a class definition, though, a call to this method returns
	 * a new instance of this class.
	 * 
	 * @return a com object with this object as type.
	 * @throws JIException
	 */
	public MyIJIDispatch spawnInstance_() throws JIException {
		JIVariant[] tmp = this.dispatch.callMethodA("SpawnInstance_", null);
		return new MyIJIDispatch((IJIDispatch) JIObjectFactory.narrowObject(tmp[0].getObjectAsComObject()
				.queryInterface(IJIDispatch.IID)));
	}

	/**
	 * To get a XML String representation of this com object. ( from SWbemObject
	 * )
	 * 
	 * @return
	 * @throws JIException
	 */
	public String getText_() throws JIException {
		return this.dispatch.callMethodA("GetText_", new Object[] { new Integer(1) })[0].getObjectAsString2();
	}

	public String getObjectText() throws JIException {
		return this.dispatch.callMethodA("GetObjectText_", new Object[] { new Integer(1) })[0].getObjectAsString2();
	}

	/**
	 * Retrieves a property and returns it as a string array
	 * 
	 * @param property
	 *            the property's name
	 * @return the property value
	 * @throws JIException
	 */
	public String[] getStringArray(String property) throws JIException {
		JIVariant[] tmp = (JIVariant[]) this.dispatch.get(property).getObjectAsArray().getArrayInstance();
		String[] res = new String[tmp.length];
		for (int i = 0; i < tmp.length; i++) {
			res[i] = tmp[i].getObjectAsString2();
		}
		return res;
	}

	/**
	 * To get a property as a com object
	 * 
	 * @param property
	 *            the property's name
	 * @return a {@link MyIJIDispatch} representing the property's value as a
	 *         com object
	 * @throws JIException
	 */
	public MyIJIDispatch getDispatch(String property) throws JIException {
		JIVariant tmp = this.dispatch.get(property);
		return new MyIJIDispatch((IJIDispatch) JIObjectFactory.narrowObject(tmp.getObjectAsComObject().queryInterface(
				IJIDispatch.IID)));
	}

	/**
	 * To get a property as an int
	 * 
	 * @param property
	 *            the name of the property
	 * @return the property's value as an int
	 * @throws JIException
	 */
	public int getInt(String property) throws JIException {
		return this.dispatch.get(property).getObjectAsInt();
	}

	/**
	 * To get a property as an long
	 * 
	 * @param property
	 *            the name of the property
	 * @return the property's value as an long
	 * @throws JIException
	 */
	public long getLong(String property) throws JIException {
		return this.dispatch.get(property).getObjectAsLong();
	}

	/**
	 * To get a property as an short
	 * 
	 * @param property
	 *            the name of the property
	 * @return the property's value as an short
	 * @throws JIException
	 */
	public short getShort(String property) throws JIException {
		return this.dispatch.get(property).getObjectAsShort();
	}
}
