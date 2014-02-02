package com.probridge.vbox.servlet;

import com.probridge.vbox.zk.AdminTaskManager;

public class VMTask implements Runnable {

	protected String sid;
	protected String opid;
	protected OpStatus ops;

	public VMTask(String sid, String opid) {
		this.sid = sid;
		this.opid = opid;
		ops = new OpStatus(opid, "等待");
		AdminTaskManager.getInstance().getOpResults().put(opid, ops);
	}

	@Override
	public void run() {
		Thread.currentThread().setName("vBox Task Worker [" + opid + "] Thread");
		AdminTaskManager.getInstance().getThreadlist().put(sid, Thread.currentThread());
		ops.setRetval(4);
	}

	public String getSid() {
		return sid;
	}

	public String getOpid() {
		return opid;
	}
}
