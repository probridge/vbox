package com.probridge.vbox.zk;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
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
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.VMMapper;
import com.probridge.vbox.model.GMImage;
import com.probridge.vbox.model.VM;
import com.probridge.vbox.model.VMExample;
import com.probridge.vbox.servlet.GoldenMasterMaintenanceTask;
import com.probridge.vbox.servlet.OpStatus;

public class GoldenMasterImageMaintController extends SelectorComposer<Window> {

	private static final long serialVersionUID = -6972782398962109854L;

	@WireVariable
	Execution _execution;

	@Wire
	Button btnReady, btnMaint, btnSync, btnCancel;

	@Wire
	Vlayout progressRow;

	@Wire
	Timer timer;

	@Wire
	Progressmeter progressBar;

	@Wire
	Label progressInfo;

	@Wire
	Label lbStatus;

	private GoldenMasterImageManagementController parentController;

	private String opid;

	private GMImage gmImage;

	private List<VM> vmList;

	public List<VM> getVmList() {
		return vmList;
	}

	public GMImage getImage() {
		return gmImage;
	}

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		gmImage = (GMImage) _execution.getArg().get("image");
		parentController = ((GoldenMasterImageManagementController) _execution.getArg().get("parentController"));
		//
		if ("0".equals(gmImage.getGmImageLock()))
			btnMaint.setDisabled(false);
		else if ("1".equals(gmImage.getGmImageLock()))
			btnReady.setDisabled(false);
		else if ("2".equals(gmImage.getGmImageLock()))
			btnSync.setDisabled(false);
		//
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		VMMapper vmapper = session.getMapper(VMMapper.class);

		VMExample exp = new VMExample();
		exp.or().andVmVhdGmImageEqualTo(gmImage.getGmImageFilename()).andVmVhdGmTypeNotEqualTo("2");
		vmList = vmapper.selectByExample(exp);

		session.close();
	}

	@Listen("onClick = button#btnCancel")
	public void closePanel() {
		parentController.reload();
		getSelf().getParent().removeChild(getSelf());
	}

	@Listen("onClick = #btnMaint")
	public void maint(Event event) {
		progressRow.setVisible(true);
		btnReady.setDisabled(true);
		btnMaint.setDisabled(true);
		btnSync.setDisabled(true);
		btnCancel.setDisabled(true);
		//
		String sid = SecurityUtils.getSubject().getSession().getId().toString();
		opid = String.valueOf(System.currentTimeMillis());
		GoldenMasterMaintenanceTask t = new GoldenMasterMaintenanceTask(sid, opid, gmImage, 0);
		//
		if (AdminTaskManager.getInstance().submit(t) == null) {
			progressRow.setVisible(true);
			progressInfo.setValue("请等待之前操作完成");
		} else {
			timer.start();
		}
	}

	@Listen("onClick = button#btnReady")
	public void ready(Event event) {
		progressRow.setVisible(true);
		btnReady.setDisabled(true);
		btnMaint.setDisabled(true);
		btnSync.setDisabled(true);
		btnCancel.setDisabled(true);
		//
		String sid = SecurityUtils.getSubject().getSession().getId().toString();
		opid = String.valueOf(System.currentTimeMillis());
		GoldenMasterMaintenanceTask t = new GoldenMasterMaintenanceTask(sid, opid, gmImage, 1);
		//
		if (AdminTaskManager.getInstance().submit(t) == null) {
			progressRow.setVisible(true);
			progressInfo.setValue("请等待之前操作完成");
		} else {
			timer.start();
		}
	}

	@Listen("onClick = button#btnSync")
	public void sync(Event event) {
		progressRow.setVisible(true);
		btnReady.setDisabled(true);
		btnMaint.setDisabled(true);
		btnSync.setDisabled(true);
		btnCancel.setDisabled(true);
		//
		String sid = SecurityUtils.getSubject().getSession().getId().toString();
		opid = String.valueOf(System.currentTimeMillis());
		GoldenMasterMaintenanceTask t = new GoldenMasterMaintenanceTask(sid, opid, gmImage, 2);
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
			btnReady.setDisabled(true);
			btnMaint.setDisabled(true);
		} else {
			progressInfo.setValue(status.getMsg());
		}
	}
}
