package com.probridge.vbox.zk;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.shiro.SecurityUtils;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.probridge.vbox.model.GMImage;
import com.probridge.vbox.servlet.DuplicateGoldenMasterTask;
import com.probridge.vbox.servlet.OpStatus;

public class GoldenMasterImageDuplicateController extends SelectorComposer<Window> {

	private static final long serialVersionUID = -6972782398962109854L;

	@WireVariable
	Execution _execution;

	@Wire
	Button btnClone, btnCancel;

	@Wire
	Vlayout progressRow;

	@Wire
	Timer timer;

	@Wire
	Progressmeter progressBar;

	@Wire
	Label progressInfo;

	@Wire
	Textbox tbNewImageFileName,tbNewImageDescription;

	private GoldenMasterImageManagementController parentController;

	private String opid;

	private GMImage gmImage;

	private String desc;

	public GMImage getImage() {
		return gmImage;
	}

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		gmImage = (GMImage) _execution.getArg().get("image");
		parentController = ((GoldenMasterImageManagementController) _execution.getArg().get("parentController"));
		//
		tbNewImageFileName.setValue("Copy_" + gmImage.getGmImageFilename());
		//
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date current = new Date();
		desc = "在" + sdFormat.format(current) + " 由[" + gmImage.getGmImageFilename() + "]生成的克隆。";
		//
		tbNewImageDescription.setValue(desc);
	}

	@Listen("onClick = button#btnCancel")
	public void closePanel() {
		parentController.reload();
		getSelf().getParent().removeChild(getSelf());
	}

	@Listen("onClick = button#btnClone")
	public void ready(Event event) {
		progressRow.setVisible(true);
		btnClone.setDisabled(true);
		btnCancel.setDisabled(true);
		//
		String sid = SecurityUtils.getSubject().getSession().getId().toString();
		opid = String.valueOf(System.currentTimeMillis());
		DuplicateGoldenMasterTask t = new DuplicateGoldenMasterTask(sid, opid, gmImage, tbNewImageFileName.getValue(), tbNewImageDescription.getValue());
		//
		if (AdminTaskManager.getInstance().submit(t) == null) {
			progressRow.setVisible(true);
			progressInfo.setValue("请等待之前操作完成");
		} else {
			timer.start();
		}
	}

	@Listen("onTimer = #timer")
	public void updateProgress(Event e) {
		OpStatus status = AdminTaskManager.getInstance().queryStatus(opid);
		if (status.getRetval() == 0 || status.getRetval() == 1) {
			timer.stop();
			progressBar.setValue(100);
			progressInfo.setValue(status.getMsg());
			btnCancel.setLabel("关闭");
			btnCancel.setDisabled(false);
			btnClone.setDisabled(true);
		} else {
			progressInfo.setValue(status.getMsg());
		}
	}
}
