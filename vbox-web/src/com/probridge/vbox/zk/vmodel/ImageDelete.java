package com.probridge.vbox.zk.vmodel;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;

import com.probridge.vbox.model.GMImage;
import com.probridge.vbox.servlet.DeleteImageTask;

public class ImageDelete extends ProgressViewModel {
	private GMImage image;

	@Init
	public void init() {
		image = (GMImage) _execution.getArg().get("image");
		globalCommandName = "reloadImageList";
		//
	}

	@Command
	@NotifyChange({ "progress", "progressMsg", "started", "running", "closeBtnLabel" })
	public void delete() {
		DeleteImageTask t = new DeleteImageTask(getSid(), getOpId(), image);
		submit(t);
		return;
	}

	public GMImage getImage() {
		return image;
	}

	public void setImage(GMImage image) {
		this.image = image;
	}
}
