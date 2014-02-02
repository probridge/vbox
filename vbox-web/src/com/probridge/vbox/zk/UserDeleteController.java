package com.probridge.vbox.zk;

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
import org.zkoss.zul.Row;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Window;

import com.probridge.vbox.model.Users;
import com.probridge.vbox.servlet.DeleteUserTask;
import com.probridge.vbox.servlet.OpStatus;

public class UserDeleteController extends SelectorComposer<Window> {

	private static final long serialVersionUID = -6972782398962109854L;

	@WireVariable
	Execution _execution;

	@Wire
	Button btnCancel, btnDelete;

	@Wire
	Row progressRow;

	@Wire
	Timer timer;

	@Wire
	Progressmeter progressBar;

	@Wire
	Label progressInfo;

	private UserManagementController parentController;

	private String opid;

	private Users user;

	public Users getUser() {
		return user;
	}

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		user = (Users) _execution.getArg().get("user");
		parentController = ((UserManagementController) _execution.getArg().get("parentController"));
	}

	@Listen("onClick = button#btnCancel")
	public void closePanel(Event event) {
		parentController.reload();
		getSelf().getParent().removeChild(getSelf());
	}

	@Listen("onClick = button#btnDelete")
	public void updateVM(Event event) {
		doDelete();
	}

	private void doDelete() {
		progressRow.setVisible(true);
		btnDelete.setDisabled(true);
		btnCancel.setDisabled(true);
		String sid = SecurityUtils.getSubject().getSession().getId().toString();
		opid = String.valueOf(System.currentTimeMillis());
		DeleteUserTask t = new DeleteUserTask(sid, opid, user);
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
			btnDelete.setDisabled(true);
		} else {
			progressInfo.setValue(status.getMsg());
		}
	}
}
