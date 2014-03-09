package com.probridge.vbox.zk.vmodel;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;

import com.probridge.vbox.model.Users;
import com.probridge.vbox.servlet.DeleteUserTask;

public class UserDelete extends ProgressViewModel {
	private Users user;

	@Init
	public void init() {
		user = (Users) _execution.getArg().get("user");
		globalCommandName = "reloadUserList";
	}

	@Command
	@NotifyChange({ "progress", "progressMsg", "started", "running", "closeBtnLabel" })
	public void delete() {
		DeleteUserTask t = new DeleteUserTask(getSid(), getOpId(), user);
		submit(t);
		return;
	}

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
	}

}
