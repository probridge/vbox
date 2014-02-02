package com.probridge.vbox.zk;

import java.util.Date;
import java.util.HashMap;
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
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Window;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.actions.ApplyPlacedAction;
import com.probridge.vbox.dao.CourseMapper;
import com.probridge.vbox.dao.UsersMapper;
import com.probridge.vbox.dao.VboxRequestMapper;
import com.probridge.vbox.model.Course;
import com.probridge.vbox.model.CourseExample;
import com.probridge.vbox.model.Users;
import com.probridge.vbox.model.VboxRequest;
import com.probridge.vbox.servlet.OpStatus;
import com.probridge.vbox.servlet.PreAppoveTask;
import com.probridge.vbox.utils.Utility;

public class CourseApproveController extends SelectorComposer<Window> {

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
	Textbox tbApprovalInfo;

	private CourseRequestApprovalController parentController;

	private String opid;

	private VboxRequest vboxRequest;

	public VboxRequest getVboxRequest() {
		return vboxRequest;
	}

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		vboxRequest = (VboxRequest) _execution.getArg().get("request");
		parentController = ((CourseRequestApprovalController) _execution.getArg().get("parentController"));
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
		//
		String identity = vboxRequest.getVboxRequestOwner();
		//
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		CourseMapper mapper = session.getMapper(CourseMapper.class);
		CourseExample courseExp = new CourseExample();
		courseExp.or().andCourseIdEqualTo(vboxRequest.getVboxRequestCode())
				.andCourseExpirationGreaterThanOrEqualTo(new Date());
		courseExp.or().andCourseIdEqualTo(vboxRequest.getVboxRequestCode()).andCourseExpirationIsNull();
		List<Course> courseInfo = mapper.selectByExample(courseExp);
		// check user status
		UsersMapper umapper = session.getMapper(UsersMapper.class);
		Users requester = umapper.selectByPrimaryKey(identity);
		session.close();
		//
		if (requester != null && !Utility.isEmptyOrNull(requester.getUserVhdName())) {
			if (courseInfo != null && courseInfo.size() == 1) {
				HashMap<String, Object> commandMap = new HashMap<String, Object>(3);
				commandMap.put("identity", identity);
				commandMap.put("hypervisor", requester.getUserHypervisorId().intValue());
				commandMap.put("courseVBox", ApplyPlacedAction.getCourseVBoxConfig(courseInfo.get(0), identity,
						requester.getUserHypervisorId().intValue()));
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
				progressInfo.setValue("用户申请的课程不存在或者已经过期，请检查！");
			}
		} else {
			btnCancel.setDisabled(false);
			btnCancel.setLabel("关闭");
			progressBar.setValue(100);
			progressInfo.setValue("用户数据文件不存在，请检查用户是否已经通过审批");
		}
	}

	@Listen("onClick = button#btnDeny")
	public void deny(Event event) {
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		try {
			VboxRequestMapper mapper = session.getMapper(VboxRequestMapper.class);
			vboxRequest.setVboxRequestStatus("2");
			if (!Utility.isEmptyOrNull(tbApprovalInfo.getText()))
				vboxRequest.setVboxRequestApprovalInfo(tbApprovalInfo.getText());
			vboxRequest.setVboxRequestApprovalTimestamp(new Date());
			mapper.updateByPrimaryKey(vboxRequest);
			session.commit();
		} finally {
			session.close();
		}
		closePanel();
	}

	@Listen("onTimer = #timer")
	public void updateProgress(Event e) {
		OpStatus status = AdminTaskManager.getInstance().queryStatus(opid);
		if (status.getRetval() == 0) {
			SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
			try {
				VboxRequestMapper mapper = session.getMapper(VboxRequestMapper.class);
				vboxRequest.setVboxRequestStatus("1");
				if (!Utility.isEmptyOrNull(tbApprovalInfo.getText()))
					vboxRequest.setVboxRequestApprovalInfo(tbApprovalInfo.getText());
				vboxRequest.setVboxRequestApprovalTimestamp(new Date());
				mapper.updateByPrimaryKey(vboxRequest);
				session.commit();
			} finally {
				session.close();
			}
		}
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
