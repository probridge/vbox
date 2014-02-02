package com.probridge.vbox.vmm.wmi;

import com.probridge.vbox.vmm.wmi.VirtualMachine.HeartBeat;

public class VMGuestStatus {
	private final String ipAddress;
	private final VirtualMachine.HeartBeat heartBeat;
	private final String credential;

	/**
	 * This class is intended to be used exclusively as information holder.
	 * information provided by {@link VirtualMachine} implementation.
	 * 
	 * @param ipAddress
	 *            The list of identified IP Addresses attributed to the virtual
	 *            machine asked for guest status. If unknown or if cannot be
	 *            determined, empty String[]. A null value indicates that
	 *            something went wrong.
	 * @param macAddresses
	 *            The list of mounted NIC's MAC Addresses of the virtual machine
	 *            asked for guest status. If unknown or if cannot be determined,
	 *            empty String[]. A null value indicates that something went
	 *            wrong.
	 * @param heartBeat
	 *            The heart beat of the virtual machine asked for guest status.
	 *            If unknown or if cannot be determined, empty String[]. A null
	 *            value indicates that something went wrong.
	 */
	public VMGuestStatus(VirtualMachine.HeartBeat heartBeat, String ipAddresses, String credential) {
		this.heartBeat = heartBeat;
		this.ipAddress = ipAddresses;
		this.credential = credential;
	}

	/**
	 * Returns the list of known IP Addresses attributed to the guest operating
	 * system
	 * 
	 * @return A string array filled in with the known guest's IP addresses. A
	 *         null value indicates an error.
	 */
	public String getIPAddress() {
		return ipAddress;
	}

	/**
	 * To get the guest operating system's heart beat.
	 * 
	 * @return One of {@link HeartBeat} depending on the guest operating
	 *         system's heart beat. A null value indicates an error.
	 */
	public VirtualMachine.HeartBeat getHeartBeat() {
		return heartBeat;
	}

	public String getCredidential() {
		return credential;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("VM Guest heartbeat: ");
		sb.append(this.heartBeat.getName());
		sb.append(" - IP Address: ");
		sb.append(ipAddress);
		sb.append(" - Credential: ").append(this.credential);
		return sb.toString();
	}

	@Override
	public boolean equals(Object dest) {
		if (dest instanceof VMGuestStatus) {
			VMGuestStatus toTest = (VMGuestStatus) dest;
			boolean result = true;
			result = result && (this.heartBeat.getValue() == toTest.getHeartBeat().getValue());
			//
			if (this.ipAddress != null && toTest.getIPAddress() != null) {
				result = result && ipAddress.equals(toTest.getIPAddress());
			} else if (this.ipAddress == null && toTest.getIPAddress() == null) {
				result = result && true;
			} else {
				result = false;
			}
			//
			if (this.credential != null && toTest.getCredidential() != null) {
				result = result && this.credential.equals(toTest.getCredidential());
			} else if (this.credential == null && toTest.getCredidential() == null) {
				result = result && true;
			} else {
				result = false;
			}
			//
			return result;
		} else {
			return false;
		}
	}
}
