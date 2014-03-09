package com.probridge.vbox.zk.vmodel;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.VMMapper;
import com.probridge.vbox.model.GMImage;
import com.probridge.vbox.model.VM;
import com.probridge.vbox.model.VMExample;
import com.probridge.vbox.servlet.GoldenMasterMaintenanceTask;

public class ImageMaintenance extends ProgressViewModel {
	private GMImage image;
	private List<VM> vmList;
	//
	private boolean allowMaint = false;
	private boolean allowReady = false;
	private boolean allowSync = false;

	@Init
	public void init() {
		image = (GMImage) _execution.getArg().get("image");
		globalCommandName = "reloadImageList";
		//
		if ("0".equals(image.getGmImageLock()))
			allowMaint = true;
		else if ("1".equals(image.getGmImageLock()))
			allowReady = true;
		else if ("2".equals(image.getGmImageLock()))
			allowSync = true;
		//
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		VMMapper vmapper = session.getMapper(VMMapper.class);

		VMExample exp = new VMExample();
		exp.or().andVmVhdGmImageEqualTo(image.getGmImageFilename()).andVmVhdGmTypeNotEqualTo("2");
		vmList = vmapper.selectByExample(exp);
		session.close();
	}

	@Command
	@NotifyChange({ "progress", "progressMsg", "started", "running", "closeBtnLabel", "allowMaint", "allowReady",
			"allowSync" })
	public void maintImage() {
		allowMaint = allowReady = allowSync = false;
		GoldenMasterMaintenanceTask t = new GoldenMasterMaintenanceTask(getSid(), getOpId(), image, 0);
		submit(t);
		return;
	}

	@Command
	@NotifyChange({ "progress", "progressMsg", "started", "running", "closeBtnLabel", "allowMaint", "allowReady",
			"allowSync" })
	public void readyImage() {
		allowMaint = allowReady = allowSync = false;
		GoldenMasterMaintenanceTask t = new GoldenMasterMaintenanceTask(getSid(), getOpId(), image, 1);
		submit(t);
		return;
	}

	@Command
	@NotifyChange({ "progress", "progressMsg", "started", "running", "closeBtnLabel", "allowMaint", "allowReady",
			"allowSync" })
	public void syncImage() {
		allowMaint = allowReady = allowSync = false;
		GoldenMasterMaintenanceTask t = new GoldenMasterMaintenanceTask(getSid(), getOpId(), image, 2);
		submit(t);
		return;
	}

	public GMImage getImage() {
		return image;
	}

	public void setImage(GMImage image) {
		this.image = image;
	}

	public List<VM> getVmList() {
		return vmList;
	}

	public void setVmList(List<VM> vmList) {
		this.vmList = vmList;
	}

	public boolean isAllowMaint() {
		return allowMaint;
	}

	public void setAllowMaint(boolean allowMaint) {
		this.allowMaint = allowMaint;
	}

	public boolean isAllowReady() {
		return allowReady;
	}

	public void setAllowReady(boolean allowReady) {
		this.allowReady = allowReady;
	}

	public boolean isAllowSync() {
		return allowSync;
	}

	public void setAllowSync(boolean allowSync) {
		this.allowSync = allowSync;
	}

}
