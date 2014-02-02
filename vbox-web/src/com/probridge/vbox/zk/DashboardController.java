package com.probridge.vbox.zk;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Caption;
import org.zkoss.zul.CategoryModel;
import org.zkoss.zul.Div;
import org.zkoss.zul.Flashchart;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Separator;
import org.zkoss.zul.SimpleCategoryModel;
import org.zkoss.zul.SimplePieModel;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Vlayout;

import com.probridge.vbox.utils.Utility;
import com.probridge.vbox.vmm.ResourceMonitor;
import com.probridge.vbox.vmm.wmi.HyperVVMM;

public class DashboardController extends SelectorComposer<Div> {

	private static final long serialVersionUID = -6972782398962109854L;

	@Wire
	Radiogroup hypervisorList;

	@Wire
	Label lbOsInfo, lbCpuInfo, lbMemDiskInfo, lbSysInfo;

	@Wire
	Flashchart fMemoryUsage, fDiskUsage, fCpuUtils, fDiskPerf;

	@Wire
	Vlayout vBoxStatistic;

	@Wire
	Groupbox gbCores;

	@Wire
	Timer timer;

	@WireVariable
	Desktop _desktop;

	private SimplePieModel memoryUsage, diskUsage;

	private CategoryModel diskPerf, cpuUtil;

	private int selected = 0;

	@Override
	public void doAfterCompose(Div comp) throws Exception {
		super.doAfterCompose(comp);
		int index = 0;
		for (HyperVVMM eachVmm : HyperVVMM.hypervisors) {
			Radio eachVmmRadio = new Radio(eachVmm.hypervisorName + "[" + eachVmm.url + "]");
			eachVmmRadio.setValue(index);
			if (index == selected)
				eachVmmRadio.setSelected(true);
			index++;
			eachVmmRadio.addEventListener("onClick", new EventListener<MouseEvent>() {
				@Override
				public void onEvent(MouseEvent arg0) throws Exception {
					loaddata();
				}				
			});
			hypervisorList.appendChild(eachVmmRadio);
		}
		loaddata();
	}

	private void loaddata() {
		selected = hypervisorList.getSelectedIndex();
		if (selected < 0) {
			selected = 0;
			hypervisorList.setSelectedIndex(0);
		}
		ResourceMonitor resMgr = ResourceMonitor.instances[selected];
		lbCpuInfo.setValue("已安装的处理器：" + resMgr.getCpuInfo());
		lbOsInfo.setValue("操作系统：" + resMgr.getOsInfo());
		lbMemDiskInfo.setValue("已安装的内存总量：" + Utility.formatSize(resMgr.getMemorySize()) + " 数据磁盘容量："
				+ Utility.formatSize(resMgr.getDiskSize()));
		lbSysInfo.setValue("平台版本信息：" + resMgr.getSysInfo());
		//
		memoryUsage = new SimplePieModel();
		diskUsage = new SimplePieModel();
		fMemoryUsage.setModel(memoryUsage);
		fDiskUsage.setModel(diskUsage);
		//
		diskPerf = new SimpleCategoryModel();
		cpuUtil = new SimpleCategoryModel();
		//
		fDiskPerf.setModel(diskPerf);
		fCpuUtils.setModel(cpuUtil);
		//
		update();
		timer.start();
	}

	@Listen("onTimer = #timer")
	public void update() {
		ResourceMonitor resMgr = ResourceMonitor.instances[selected];
		//
		Iterator<Component> itrComp = vBoxStatistic.getChildren().iterator();
		while (itrComp.hasNext())
			if (itrComp.next() != null)
				itrComp.remove();
		//
		HashMap<String, Integer> status = resMgr.getVmmStatus();
		if (status != null) {
			Iterator<String> itr = status.keySet().iterator();
			while (itr.hasNext()) {
				String entry = itr.next();
				Integer val = status.get(entry);
				if (val > 0) {
					Label thisLabel = new Label(entry + " : " + val);
					vBoxStatistic.appendChild(thisLabel);
				}
			}
		}
		//
		itrComp = gbCores.getChildren().iterator();
		while (itrComp.hasNext())
			if (!(itrComp.next() instanceof Caption))
				itrComp.remove();
		//
		int vcores = resMgr.getvCores();
		int lcores = resMgr.getlCores();
		gbCores.appendChild(new Label("虚拟："));
		for (int i = 0; i < vcores; i++) {
			Image vCoreImg = new Image("../imgs/plus.png");
			gbCores.appendChild(vCoreImg);
		}
		//
		Separator sep = new Separator();
		sep.setBar(true);
		gbCores.appendChild(sep);
		gbCores.appendChild(new Label("逻辑："));
		for (int i = 0; i < lcores; i++) {
			Image lCoreImg = new Image("../imgs/plus.png");
			gbCores.appendChild(lCoreImg);
		}
		//
		memoryUsage.setValue("已用",
				Utility.roundDouble((resMgr.getMemorySize() - resMgr.getMemoryFreeSpace()) * 1.0d / Utility.GB));
		memoryUsage.setValue("剩余", Utility.roundDouble(resMgr.getMemoryFreeSpace() * 1.0d / Utility.GB));
		//
		diskUsage.setValue("已用",
				Utility.roundDouble((resMgr.getDiskSize() - resMgr.getDiskFreeSpace()) * 1.0d / Utility.GB));
		diskUsage.setValue("剩余", Utility.roundDouble(resMgr.getDiskFreeSpace() * 1.0d / Utility.GB));
		//
		Map<Long, Integer> cpuUtilData = resMgr.getCpuUtilHistory();
		Iterator<Long> itr2 = cpuUtilData.keySet().iterator();
		//
		while (itr2.hasNext()) {
			long time = itr2.next();
			cpuUtil.setValue("CPU%", Utility.formatDate(time), cpuUtilData.get(time));
		}
		//
		Map<Long, Long> diskReadData = resMgr.getDiskReadRespHistory();
		Map<Long, Long> diskWriteData = resMgr.getDiskWriteRespHistory();
		//
		Iterator<Long> itr3 = diskReadData.keySet().iterator();
		while (itr3.hasNext()) {
			long time = itr3.next();
			diskPerf.setValue("读", Utility.formatDate(time), diskReadData.get(time));
		}

		Iterator<Long> itr4 = diskWriteData.keySet().iterator();
		while (itr4.hasNext()) {
			long time = itr4.next();
			diskPerf.setValue("写", Utility.formatDate(time), diskWriteData.get(time));
		}
	}
}
