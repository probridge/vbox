package com.probridge.vbox.zk;

import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;
import org.apache.shiro.SecurityUtils;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Row;
import org.zkoss.zul.Spinner;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Window;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.actions.ApplyPlacedAction;
import com.probridge.vbox.dao.GMImageMapper;
import com.probridge.vbox.dao.UsersMapper;
import com.probridge.vbox.model.GMImage;
import com.probridge.vbox.model.GMImageExample;
import com.probridge.vbox.model.PreApprovedUser;
import com.probridge.vbox.model.Users;
import com.probridge.vbox.servlet.OpStatus;
import com.probridge.vbox.servlet.PreAppoveTask;
import com.probridge.vbox.utils.Utility;
import com.probridge.vbox.vmm.wmi.HyperVVMM;

public class UserApproveController extends SelectorComposer<Window> {

	private static final long serialVersionUID = -6972782398962109854L;

	@WireVariable
	Execution _execution;

	@Wire
	Button btnApprove, btnDeny, btnCancel;

	@Wire
	Row progressRow;

	@Wire
	Timer timer;

	@Wire
	Progressmeter progressBar;

	@Wire
	Label progressInfo;

	@Wire
	Spinner userVhdQuota, vmCores, vmMemory, userHypervisor;

	@Wire
	Bandbox vmGoldenImage;

	@Wire
	Listbox lbImageList;

	@Wire
	Radio vmExtNet, vmIntNet;

	private UserRequestApprovalController parentController;

	private String opid;

	private Users requestUser;

	private PreApprovedUser approvalInfo;

	public PreApprovedUser getApprovalInfo() {
		return approvalInfo;
	}

	private ListModelList<GMImage> imageList;

	public ListModelList<GMImage> getImageList() {
		if (imageList == null) {
			SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
			try {
				GMImageMapper mapper = session.getMapper(GMImageMapper.class);
				GMImageExample exp = new GMImageExample();
				exp.createCriteria().andGmImageLockEqualTo("0");
				imageList = new ListModelList<GMImage>();
				imageList.addAll(mapper.selectByExample(exp));
				//
			} finally {
				session.close();
			}
		}
		return imageList;
	}

	@Listen("onSelect = listbox#lbImageList")
	public void selectImage(Event e) {
		approvalInfo.setPreapproveVmGoldenMaster((((GMImage) lbImageList.getModel().getElementAt(
				lbImageList.getSelectedIndex())).getGmImageFilename()));
		AnnotateDataBinder binder = (AnnotateDataBinder) getSelf().getAttribute("binder");
		binder.loadComponent(vmGoldenImage);
		vmGoldenImage.close();
	}

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		requestUser = (Users) _execution.getArg().get("user");
		parentController = ((UserRequestApprovalController) _execution.getArg().get("parentController"));
		//
		approvalInfo = new PreApprovedUser();
		approvalInfo.setPreapproveUserName(requestUser.getUserName());
		approvalInfo.setPreapproveVhdQuota(Math.abs(requestUser.getUserVhdQuota()));
		approvalInfo.setPreapproveVmCores(VBoxConfig.defaultCPUCores);
		approvalInfo.setPreapproveVmGoldenMaster(VBoxConfig.defaultGoldenImage);
		approvalInfo.setPreapproveVmMemory(VBoxConfig.defaultMemory);
		approvalInfo.setPreapproveVmNetwork(VBoxConfig.defaultNetwork);
		approvalInfo.setPreapproveHypervisorId(VBoxConfig.repositoryLocation);
		//
		String constrain = "min 0 max " + (HyperVVMM.hypervisors.length - 1);
		userHypervisor.setConstraint(constrain);
		//
	}

	@Listen("onClick = button#btnCancel")
	public void closePanel() {
		parentController.reload();
		getSelf().getParent().removeChild(getSelf());
	}

	@Listen("onClick = button#btnApprove")
	public void approve(Event event) {
		progressRow.setVisible(true);
		btnApprove.setDisabled(true);
		btnDeny.setDisabled(true);
		btnCancel.setDisabled(true);
		userVhdQuota.setDisabled(true);
		vmCores.setDisabled(true);
		vmMemory.setDisabled(true);
		vmGoldenImage.setDisabled(true);
		vmExtNet.setDisabled(true);
		vmIntNet.setDisabled(true);
		userHypervisor.setDisabled(true);
		//
		String identity = requestUser.getUserName();
		//
		AnnotateDataBinder binder = (AnnotateDataBinder) getSelf().getAttribute("binder");
		binder.saveAll();
		//
		if (Utility.isEmptyOrNull(requestUser.getUserVhdName())) {
			HashMap<String, Object> commandMap = new HashMap<String, Object>(3);
			commandMap.put("identity", identity);
			commandMap.put("hypervisor", approvalInfo.getPreapproveHypervisorId().intValue());
			commandMap.put("personalVhd",
					ApplyPlacedAction.getPersonalVHDQuota(approvalInfo.getPreapproveVhdQuota(), null));
			commandMap.put("userVBox", ApplyPlacedAction.getPersonalVBoxConfig(approvalInfo, identity));
			String sid = SecurityUtils.getSubject().getSession().getId().toString();
			opid = String.valueOf(System.currentTimeMillis());
			PreAppoveTask t = new PreAppoveTask(sid, opid, commandMap);
			if (AdminTaskManager.getInstance().submit(t) == null) {
				progressRow.setVisible(true);
				progressInfo.setValue("请等待之前操作完成");
			} else {
				timer.start();
			}
		} else {
			btnCancel.setDisabled(false);
			btnCancel.setLabel("关闭");
			progressBar.setValue(100);
			progressInfo.setValue("用户数据文件已经分配，请确认用户还未通过审批");
		}
	}

	@Listen("onClick = button#btnDeny")
	public void deny(Event event) {
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		try {
			requestUser.setUserVhdName(null);
			requestUser.setUserVhdQuota(0);
			UsersMapper mapper = session.getMapper(UsersMapper.class);
			mapper.updateByPrimaryKey(requestUser);
			session.commit();
		} finally {
			session.close();
		}
		closePanel();
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
			btnApprove.setDisabled(true);
			btnDeny.setDisabled(true);
		} else {
			progressInfo.setValue(status.getMsg());
		}
	}
}
