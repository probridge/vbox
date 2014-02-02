package com.probridge.vbox.zk;

import org.apache.ibatis.session.SqlSession;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Spinner;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Window;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.GMImageMapper;
import com.probridge.vbox.dao.PreApprovedUserMapper;
import com.probridge.vbox.dao.UsersMapper;
import com.probridge.vbox.model.GMImage;
import com.probridge.vbox.model.GMImageExample;
import com.probridge.vbox.model.PreApprovedUser;
import com.probridge.vbox.model.Users;
import com.probridge.vbox.utils.Utility;
import com.probridge.vbox.vmm.wmi.HyperVVMM;

public class UserPreApprovalEditorController extends SelectorComposer<Window> {

	private static final long serialVersionUID = -6972782398962109854L;

	@WireVariable
	Execution _execution;

	@Wire
	Button btnSave, btnCancel;

	@Wire
	Timer timer;

	@Wire
	Spinner userVhdQuota, vmCores, vmMemory, userHypervisor;

	@Wire
	Bandbox vmGoldenImage;

	@Wire
	Listbox lbImageList;

	@Wire
	Radio vmExtNet, vmIntNet;

	@Wire
	Textbox tbUserName;

	private UserPreApprovalManagementController parentController;

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
		approvalInfo = (PreApprovedUser) _execution.getArg().get("preapproval");
		parentController = ((UserPreApprovalManagementController) _execution.getArg().get("parentController"));
		//
		String constrain = "min 0 max " + (HyperVVMM.hypervisors.length - 1);
		userHypervisor.setConstraint(constrain);
		//
		if (Utility.isEmptyOrNull(approvalInfo.getPreapproveUserName()))
			tbUserName.setReadonly(false);
	}

	@Listen("onClick = button#btnCancel")
	public void closePanel() {
		parentController.reload();
		getSelf().getParent().removeChild(getSelf());
	}

	@Listen("onClick = button#btnSave")
	public void approve(Event event) {
		//
		AnnotateDataBinder binder = (AnnotateDataBinder) getSelf().getAttribute("binder");
		binder.saveAll();
		//
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		// check user existance
		UsersMapper umapper = session.getMapper(UsersMapper.class);
		Users existingUser = umapper.selectByPrimaryKey(approvalInfo.getPreapproveUserName());
		//
		boolean userExists = true;
		if (existingUser == null || Utility.isEmptyOrNull(existingUser.getUserVhdName())) {
			userExists = false;
		}
		//
		if (!userExists) {
			PreApprovedUserMapper mapper = session.getMapper(PreApprovedUserMapper.class);
			if (mapper.updateByPrimaryKey(approvalInfo) == 0)
				mapper.insert(approvalInfo);
			session.commit();
			closePanel();
		} else {
			Messagebox.show("该用户已经被批准，请重新输入用户名。", "确认", Messagebox.OK, Messagebox.INFORMATION);
		}
		session.close();
	}
}
