package com.probridge.vbox.vmm.wmi;

public class VMConfigOverride {
	public String numOfProcessor;
	public String memoryReserved;
	public String memoryLimit;
	public String networkSwitchName;
	public String masterImageVHDName;
	public int GMtype;
	public String userVHDName;

	public VMConfigOverride(int numOfProcessor, int memoryReserved,
			int memoryLimit, String networkSwitchName,
			String masterImageVHDName, int GMtype, String userVHDName) {
		this.numOfProcessor = String.valueOf(numOfProcessor);
		this.memoryReserved = String.valueOf(memoryReserved);
		this.memoryLimit = String.valueOf(memoryLimit);
		this.networkSwitchName = networkSwitchName;
		this.masterImageVHDName = masterImageVHDName;
		this.GMtype = GMtype;
		this.userVHDName = userVHDName;
	}
}
