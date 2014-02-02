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

import com.probridge.vbox.vmm.wmi.utils.VirtualServiceException;

/**
 * Second version of the Virtual Machine interface. Adds clone capabilities and
 * private communication with virtual machine through environment update ( write
 * data ).
 */
public interface VirtualMachine {

	public enum VMState {
		Unknown("Unknown", 0), Running("Running", 2), PoweredOff("Powered Off", 3), Paused("Paused", 32768), Suspended(
				"Suspended", 32769), Starting("Starting", 32770), Saving("Saving", 32773), Stopping("Stopping", 32774), Pausing(
				"Pausing", 32776), Resuming("Resuming", 32777);

		private String name;
		private int value;

		private VMState(String name, int value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public String toString() {
			return this.name + "(" + this.value + ")";
		}

		public int getValue() {
			return this.value;
		}

		public String getName() {
			return this.name;
		}

		public static VMState getItem(int val) {
			for (VMState each : VMState.values()) {
				if (each.getValue() == val)
					return each;
			}
			return null;
		}
	}

	public enum HeartBeat {
		OK("OK", 2), Error("Error", 6), NoContact("No Contact", 12), LostCommunication("Lost Communication", 13);

		private String name;
		private int value;

		private HeartBeat(String name, int value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public String toString() {
			return this.name + "(" + this.value + ")";
		}

		public int getValue() {
			return this.value;
		}

		public String getName() {
			return this.name;
		}

		public static HeartBeat getItem(int val) {
			for (HeartBeat each : HeartBeat.values()) {
				if (each.getValue() == val)
					return each;
			}
			return null;
		}
	}

	/**
	 * To power this virtual machine on.
	 * 
	 * @return true in case of success, false otherwise.
	 * @throws VirtualServiceException
	 *             if an issue occurs.
	 */
	public boolean powerOn() throws VirtualServiceException;

	/**
	 * Powers the VM off.
	 * 
	 * @return true in case of success, false otherwise.
	 * @throws VirtualServiceException
	 */
	public boolean powerOff() throws VirtualServiceException;

	/**
	 * To put running VM into suspend mode.
	 * 
	 * @return true in case of success, false otherwise.
	 * @throws VirtualServiceException
	 *             if an issue occurs.
	 */
	public boolean suspend() throws VirtualServiceException;

	/*---------------------------------
	 * Getters & Setters
	 *--------------------------------*/

	/**
	 * This method returns the running state of this virtual machine.
	 */
	public VMState getState() throws VirtualServiceException;

	public String getName() throws VirtualServiceException;

	public String getID() throws VirtualServiceException;

	/**
	 * The method must be used virtual machine powered off. Implementation must
	 * ensure that the data are available once the virtual machine is running
	 * 
	 * @param dataKey
	 *            the key of the data to push within the vm's environment
	 * @param value
	 *            the key's value
	 * @return true in case of success false otherwise
	 * @throws VirtualServiceException
	 */
	public boolean pushData(String dataKey, String value) throws VirtualServiceException;

	/**
	 * To read a data, either set from the guest operating system or previsously
	 * set using {@link #pushData(String, String)}
	 * 
	 * @param dataKey
	 *            The key/value pair's key.
	 * @return The associated key/value pair's value if it exists, null
	 *         otherwise.
	 * @throws VirtualServiceException
	 */
	public String getData(String dataKey) throws VirtualServiceException;
}
