package com.probridge.vbox.zk.vmodel;

import org.apache.shiro.SecurityUtils;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import com.probridge.vbox.servlet.OpStatus;
import com.probridge.vbox.servlet.VMTask;
import com.probridge.vbox.zk.AdminTaskManager;

public class ProgressViewModel {
	public static final String BUTTON_CANCEL = "取消";
	public static final String BUTTON_HIDE = "隐藏";
	public static final String BUTTON_CLOSE = "关闭";
	public static final String PROGRESS_READY = "就绪";
	public static final String PROGRESS_DUPLICATE = "请等待之前操作完成";

	@WireVariable
	Execution _execution;

	private String opid;
	private boolean started = false;
	private boolean running = false;
	private boolean visible = true;
	private int progress = 0;
	private String progressMsg = PROGRESS_READY;
	private String closeBtnLabel = BUTTON_CANCEL;
	protected String globalCommandName = "";

	@Init
	public void init() {
	}

	@Command
	@NotifyChange("visible")
	public void close(@ContextParam(ContextType.VIEW) Component view) {
		if (BUTTON_HIDE.equals(closeBtnLabel))
			visible = false;
		else
			view.detach();
	}

	public void submit(VMTask t) {
		if (AdminTaskManager.getInstance().submit(t) == null) {
			progressMsg = PROGRESS_DUPLICATE;
			progress = 100;
		} else {
			started = true;
			running = true;
			closeBtnLabel = BUTTON_HIDE;
		}
	}

	@Command
	@NotifyChange({ "progress", "progressMsg", "closeBtnLabel", "visible", "running" })
	public void updateProgress(@ContextParam(ContextType.VIEW) Component view) {
		OpStatus status = AdminTaskManager.getInstance().queryStatus(opid);
		if (status.getRetval() == 0 || status.getRetval() == 1) {
			progress = 100;
			progressMsg = status.getMsg();
			closeBtnLabel = BUTTON_CLOSE;
			visible = true;
			running = false;
			BindUtils.postGlobalCommand(null, null, globalCommandName, null);
		} else {
			progressMsg = status.getMsg();
		}
	}

	protected String getSid() {
		return SecurityUtils.getSubject().getSession().getId().toString();
	}

	protected String getOpId() {
		opid = String.valueOf(System.currentTimeMillis());
		return opid;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public String getProgressMsg() {
		return progressMsg;
	}

	public void setProgressMsg(String progressMsg) {
		this.progressMsg = progressMsg;
	}

	public String getCloseBtnLabel() {
		return closeBtnLabel;
	}

	public void setCloseBtnLabel(String closeBtnLabel) {
		this.closeBtnLabel = closeBtnLabel;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
}
