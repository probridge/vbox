package com.probridge.vbox.servlet;

public class OpStatus {
	private String opid;
	private int retval; // 0-ok 1-failed 2-not found 3-not start 4-running
	private String msg;
	private long timestamp;

	public OpStatus(String opid, String msg) {
		super();
		this.opid = opid;
		retval = 3;
		this.msg = msg;
		timestamp = System.currentTimeMillis();
	}

	public int getRetval() {
		return retval;
	}

	public void setRetval(int retval) {
		this.retval = retval;
		this.timestamp = System.currentTimeMillis();
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
		this.timestamp = System.currentTimeMillis();
	}

	public String getOpid() {
		return opid;
	}

	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString() {
		return "opid:" + opid + ",retval=" + retval + ",msg=" + msg
				+ ",timestamp=" + timestamp;
	}
}
