package com.probridge.vbox.zk;

import java.util.Arrays;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.CourseMapper;
import com.probridge.vbox.model.Course;
import com.probridge.vbox.utils.Utility;

public class CourseApprovalListEditorController extends SelectorComposer<Window> {

	private static final long serialVersionUID = -6972782398962109854L;

	@WireVariable
	Execution _execution;

	@Wire
	Button btnCancel, btnSave;

	@Wire
	Div divApprovedList;

	@Wire
	Textbox tbAddUser;

	private CourseManagementController parentController;

	private Course course;

	public Course getCourse() {
		return course;
	}

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		course = (Course) _execution.getArg().get("course");
		parentController = ((CourseManagementController) _execution.getArg().get("parentController"));
		String userlist = course.getCoursePreapproveList();

		if (Utility.isEmptyOrNull(userlist))
			return;
		List<String> items = Arrays.asList(userlist.split("\\s*,\\s*"));
		for (String eachUser : items) {
			Checkbox cb = new Checkbox(eachUser);
			cb.setChecked(true);
			divApprovedList.appendChild(cb);
		}
	}

	@Listen("onClick = #btnAddUser")
	public void addUser(Event e) {
		String val = tbAddUser.getValue();
		System.out.println(val);
		if (!Utility.isEmptyOrNull(val)) {
			List<Component> list = divApprovedList.getChildren();
			for (Component eachExisting : list)
				if (val.equals(((Checkbox) eachExisting).getLabel()))
					return;
			Checkbox cb = new Checkbox(val);
			cb.setChecked(true);
			divApprovedList.appendChild(cb);
		}
	}

	@Listen("onClick = button#btnCancel")
	public void closePanel() {
		parentController.reload();
		getSelf().getParent().removeChild(getSelf());
	}

	@Listen("onClick = button#btnSave")
	public void updateCourse(Event event) {
		List<Component> list = divApprovedList.getChildren();
		StringBuffer sb = new StringBuffer();
		for (Component eachExisting : list)
			if (((Checkbox) eachExisting).isChecked())
				sb.append(((Checkbox) eachExisting).getLabel()).append(",");
		//
		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		course.setCoursePreapproveList(sb.toString());
		saveSettings();
		closePanel();
	}

	private void saveSettings() {
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		CourseMapper mapper = session.getMapper(CourseMapper.class);
		mapper.updateByPrimaryKey(course);
		session.commit();
		session.close();
	}
}
