package com.probridge.vbox.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.zk.AdminTaskManager;

public class DummyTask extends VMTask {
	private static final Logger logger = LoggerFactory.getLogger(DummyTask.class);
	//
	private int timeout = 0;

	public DummyTask(String sid, String opid, int timeout) {
		super(sid, opid);
		this.timeout = timeout;
	}

	@Override
	public void run() {
		super.run();
		logger.debug("Starting to process process dummy task..");
		ops.setMsg("开始空白操作");
		try {
			while (timeout > 0) {
				ops.setMsg("正在等待倒计时：" + timeout);
				Thread.sleep(1000);
				timeout--;
			}
			ops.setMsg("倒计时结束");
			logger.debug("Finished");
			ops.setRetval(0);
		} catch (Exception e) {
			ops.setMsg("倒计时失败:" + e.getMessage());
			ops.setRetval(1);
			logger.error("error counting down", e);
		} finally {
			AdminTaskManager.getInstance().getThreadlist().remove(sid);
		}
	}
}
