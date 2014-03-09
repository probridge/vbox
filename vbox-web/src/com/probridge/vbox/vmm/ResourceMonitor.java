package com.probridge.vbox.vmm;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jinterop.dcom.common.JIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.vmm.wmi.HyperVVMM;
import com.probridge.vbox.vmm.wmi.WindowsManagementServiceLocator;
import com.probridge.vbox.vmm.wmi.utils.Utils;

public class ResourceMonitor implements Runnable {

	public static ResourceMonitor[] instances;
	private static final Logger logger = LoggerFactory.getLogger(ResourceMonitor.class);

	public static void initialize() {
		logger.info("Resource Monitor starting.");
		instances = new ResourceMonitor[HyperVVMM.hypervisors.length];
		for (int i = 0; i < HyperVVMM.hypervisors.length; i++) {
			instances[i] = new ResourceMonitor(HyperVVMM.getHyperVVMM(i));
		}
	}

	private HyperVVMM vmm;
	private WindowsManagementServiceLocator win = null;
	private long pollInterval;
	private String osInfo, cpuInfo, sysInfo;
	private long diskSize, diskFreeSpace, memorySize, memoryFreeSpace;
	private int lCores, vCores;
	@SuppressWarnings("unused")
	private int running, poweredoff, saved, paused;
	private Map<Long, Integer> cpuUtilHistory;
	private Map<Long, Long> diskWriteRespHistory;
	private Map<Long, Long> diskReadRespHistory;
	private HashMap<String, Integer> vmmStatus;
	private AtomicBoolean isInterruptted = new AtomicBoolean(false);
	public boolean connected = false;

	private ResourceMonitor(HyperVVMM vmm) {
		this.vmm = vmm;
		pollInterval = VBoxConfig.monitorPoolingInterval;
		cpuUtilHistory = Collections.synchronizedMap(new LinkedHashMap<Long, Integer>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(Map.Entry<Long, Integer> eldest) {
				return this.size() > VBoxConfig.cpuMaxHistory;
			}
		});
		diskReadRespHistory = Collections.synchronizedMap(new LinkedHashMap<Long, Long>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(Map.Entry<Long, Long> eldest) {
				return this.size() > VBoxConfig.cpuMaxHistory;
			}
		});
		diskWriteRespHistory = Collections.synchronizedMap(new LinkedHashMap<Long, Long>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(Map.Entry<Long, Long> eldest) {
				return this.size() > VBoxConfig.cpuMaxHistory;
			}
		});
		// initialize
		Long timestamp = System.currentTimeMillis();
		cpuUtilHistory.put(timestamp, 0);
		diskReadRespHistory.put(timestamp, 0L);
		diskWriteRespHistory.put(timestamp, 0L);
	}

	@Override
	public void run() {
		Thread.currentThread().setName("vBox Hypervisor Performance Collection Thread.");
		logger.info("Resource Monitor[" + vmm.vmmId + "] started.");
		// one time info fetch
		try {
			win = new WindowsManagementServiceLocator(vmm.url);
			cpuInfo = win.getCpuInformation();
			osInfo = win.getOsInformation();
			sysInfo = VBoxConfig.systemVersion;
		} catch (UnknownHostException | JIException e1) {
			logger.error("error getting system information.", e1);
			return;
		}
		connected = true;
		while (true) {
			// keep polling
			try {
				long[] diskVal = win.getDiskSize(VBoxConfig.dataDrive);
				diskSize = diskVal[0];
				diskFreeSpace = diskVal[1];

				long[] memoryVal = win.getMemoryInfo();
				memorySize = memoryVal[0];
				memoryFreeSpace = memoryVal[1];

				int[] coresVal = win.getHyperVProcessorNum();
				lCores = coresVal[0];
				vCores = coresVal[1];

				vmmStatus = win.getHyperVSummary();
				running = vmmStatus.get("正在运行");
				poweredoff = vmmStatus.get("已关机");
				saved = vmmStatus.get("已保存");
				paused = vmmStatus.get("已暂停");

				cpuUtilHistory.put(System.currentTimeMillis(), win.getCombinedCpuUtilization());
				//
				long[] diskPerf1 = win.getDiskPerformance(VBoxConfig.dataDrive);
				//

				Utils.sleepCheck((int) (pollInterval / 1000), isInterruptted);
				//
				long[] diskPerf2 = win.getDiskPerformance(VBoxConfig.dataDrive);
				//
				long readDelayMs = 0, writeDelayMs = 0;
				readDelayMs = (diskPerf2[1] == diskPerf1[1]) ? 0
						: ((diskPerf2[0] - diskPerf1[0]) * 1000 / diskPerf1[4] / (diskPerf2[1] - diskPerf1[1]));
				writeDelayMs = (diskPerf2[3] == diskPerf1[3]) ? 0 : ((diskPerf2[2] - diskPerf1[2]) * 1000
						/ diskPerf1[4] / (diskPerf2[3] - diskPerf1[3]));
				//
				if (readDelayMs < 0)
					readDelayMs = 0l;
				if (writeDelayMs < 0)
					writeDelayMs = 0l;
				//
				long timestamp = System.currentTimeMillis();
				diskReadRespHistory.put(timestamp, readDelayMs);
				diskWriteRespHistory.put(timestamp, writeDelayMs);
				//
			} catch (JIException e) {
				logger.error("error polling information.", e);
				logger.warn("Resource Monitor waiting for 15sec to retry connection");
				while (true) {
					if (win != null)
						win.destroySession();
					try {
						Utils.sleepCheck(15, isInterruptted);
						win = new WindowsManagementServiceLocator(vmm.url);
						cpuInfo = win.getCpuInformation();
						osInfo = win.getOsInformation();
						sysInfo = VBoxConfig.systemVersion;
						break;
					} catch (UnknownHostException | JIException e1) {
						logger.error("Error retreiving monitor data again.", e1);
						logger.info("Next attempt to retry in 15s...");
					} catch (InterruptedException e1) {
						logger.warn("Interruptted, resource Monitor exiting.");
						break;
					}
				}
				if (isInterruptted.get())
					break;
			} catch (InterruptedException e) {
				logger.warn("Interruptted, resource Monitor exiting.");
				break;
			}
		}
		if (win != null)
			win.destroySession();
	}

	public void notifyInterrupt() {
		isInterruptted.set(true);
	}

	public String getOsInfo() {
		return osInfo;
	}

	public String getCpuInfo() {
		return cpuInfo;
	}

	public String getSysInfo() {
		return sysInfo;
	}

	public long getPollInterval() {
		return pollInterval;
	}

	public long getDiskSize() {
		return diskSize;
	}

	public long getDiskFreeSpace() {
		return diskFreeSpace;
	}

	public long getMemorySize() {
		return memorySize;
	}

	public long getMemoryFreeSpace() {
		return memoryFreeSpace;
	}

	public int getlCores() {
		return lCores;
	}

	public int getvCores() {
		return vCores;
	}

	public HashMap<String, Integer> getVmmStatus() {
		return vmmStatus;
	}

	public Map<Long, Integer> getCpuUtilHistory() {
		return cpuUtilHistory;
	}

	public Map<Long, Long> getDiskWriteRespHistory() {
		return diskWriteRespHistory;
	}

	public Map<Long, Long> getDiskReadRespHistory() {
		return diskReadRespHistory;
	}

}
