package com.probridge.vbox.zk;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.shiro.SecurityUtils;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.VboxRequestMapper;
import com.probridge.vbox.model.VboxRequest;
import com.probridge.vbox.model.VboxRequestExample;

public class CourseRequestApprovalController extends
		SelectorComposer<Div> {
	private static final long serialVersionUID = -4592498413991042683L;

	@Wire
	Listbox lbRequestList;

	@Wire
	Button btnProcess;

	ListModelList<VboxRequest> requestList = new ListModelList<VboxRequest>();

	public ListModelList<VboxRequest> getRequestList() {
		return requestList;
	}

	public void reload() {
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		try {
			VboxRequestMapper mapper = session.getMapper(VboxRequestMapper.class);
			VboxRequestExample exp = new VboxRequestExample();
			exp.createCriteria().andVboxRequestStatusNotEqualTo("3");
			requestList.clear();
			requestList.addAll(mapper.selectByExample(exp));
			//
		} finally {
			session.close();
		}
	}

	@Override
	public void doAfterCompose(Div comp) throws Exception {
		super.doAfterCompose(comp);
		reload();
	}

	@Listen("onClick = #btnDelete")
	public void deleteRequest(Event e) {
		if (lbRequestList.getSelectedItem() == null)
			return;
		//
		Messagebox.show("是否要不加处理直接删除这个课程请求？",
				"确认", Messagebox.YES | Messagebox.NO,
				Messagebox.QUESTION, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						if (!Messagebox.ON_YES.equals(event.getName()))
							return;
						//
						VboxRequest selectedReq = lbRequestList.getSelectedItem().getValue();
						//
						SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
						try {
							VboxRequestMapper mapper = session.getMapper(VboxRequestMapper.class);
							selectedReq.setVboxRequestStatus("3");
							selectedReq.setVboxRequestApprovalInfo("被" + SecurityUtils.getSubject().getPrincipal().toString() + "删除");
							selectedReq.setVboxRequestApprovalTimestamp(new Date());
							mapper.updateByPrimaryKey(selectedReq);
							session.commit();
						} finally {
							session.close();
						}
						reload();
					}
				});
	}

	@Listen("onClick = #btnProcess")
	public void process(Event e) {
		Listitem thisListItem = lbRequestList.getSelectedItem();
		if (thisListItem == null)
			return;
		VboxRequest thisRequest = (VboxRequest) thisListItem.getValue();
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("request", thisRequest);
		arg.put("parentController", this);
		Window win = (Window) Executions.createComponents(
				"/management/course_approve.zul", getSelf(), arg);
		getSelf().appendChild(win);
	}
}
