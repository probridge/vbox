package com.probridge.vbox.zk.vmodel;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;

import com.probridge.vbox.model.GMImage;
import com.probridge.vbox.servlet.DuplicateGoldenMasterTask;

public class ImageDuplicate extends ProgressViewModel {
	private GMImage image;
	private String newImageFileName, newImageDescription;

	@Init
	public void init() {
		image = (GMImage) _execution.getArg().get("image");
		globalCommandName = "reloadImageList";
		newImageFileName = "Copy_" + image.getGmImageFilename();
		//
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date current = new Date();
		newImageDescription = "在" + sdFormat.format(current) + " 由[" + image.getGmImageFilename() + "]生成的克隆。";
		//
	}

	@Command
	@NotifyChange({ "progress", "progressMsg", "started", "running", "closeBtnLabel" })
	public void save() {
		DuplicateGoldenMasterTask t = new DuplicateGoldenMasterTask(getSid(), getOpId(), image, newImageFileName,
				newImageDescription);
		submit(t);
		return;
	}

	public GMImage getImage() {
		return image;
	}

	public void setImage(GMImage image) {
		this.image = image;
	}

	public String getNewImageFileName() {
		return newImageFileName;
	}

	public void setNewImageFileName(String newImageFileName) {
		this.newImageFileName = newImageFileName;
	}

	public String getNewImageDescription() {
		return newImageDescription;
	}

	public void setNewImageDescription(String newImageDescription) {
		this.newImageDescription = newImageDescription;
	}

}
