package com.probridge.vbox.zk;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.CourseMapper;
import com.probridge.vbox.model.Course;
import com.probridge.vbox.model.CourseExample;

public class CourseManagementController extends SelectorComposer<Window> {
	private static final long serialVersionUID = -4592498413991042683L;

	@Wire
	Listbox lbCourseList;

	@Wire
	Textbox tbCourseID, tbCourseName, tbCourseDescription;

	@Wire
	Datebox dbCourseExpiration;

	ListModelList<Course> courseList = new ListModelList<Course>();

	public ListModelList<Course> getCourseList() {
		return courseList;
	}

	public void reload() {
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		try {
			CourseMapper mapper = session.getMapper(CourseMapper.class);
			CourseExample exp = new CourseExample();
			exp.createCriteria();
			courseList.clear();
			courseList.addAll(mapper.selectByExample(exp));
			//
		} finally {
			session.close();
		}
	}

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		reload();
	}

	@Listen("onClick = #btnDeleteCourse")
	public void deleteCourse(Event e) {
		if (lbCourseList.getSelectedItem() == null)
			return;
		//
		Messagebox.show("是否确认要删除这门课程信息？（母盘和已经分配的vBox不会被删除）", "确认", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION,
				new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						if (!Messagebox.ON_YES.equals(event.getName()))
							return;
						//
						Course thisCourse = (Course) lbCourseList.getSelectedItem().getValue();
						SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
						CourseMapper mapper = session.getMapper(CourseMapper.class);
						mapper.deleteByPrimaryKey(thisCourse.getCourseId());
						//
						session.commit();
						session.close();
						//
						reload();
					}
				});
	}
	
	@Listen("onClick = #btnEditPreApprovalList")
	public void editApprovalList(Event e) {
		Listitem thisListItem = lbCourseList.getSelectedItem();
		if (thisListItem == null)
			return;
		Course thisCourse = (Course) thisListItem.getValue();
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("course", thisCourse);
		arg.put("parentController", this);
		Window win = (Window) Executions.createComponents("/management/course_approval_list_editor.zul", getSelf(), arg);
		getSelf().appendChild(win);
	}
	

	@Listen("onClick = #btnEditCourse")
	public void editUser(Event e) {
		Listitem thisListItem = lbCourseList.getSelectedItem();
		if (thisListItem == null)
			return;
		Course thisCourse = (Course) thisListItem.getValue();
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("course", thisCourse);
		arg.put("parentController", this);
		Window win = (Window) Executions.createComponents("/management/course_editor.zul", getSelf(), arg);
		getSelf().appendChild(win);
	}

	@Listen("onClick = #btnAddCourse")
	public void createCourse(Event event) {
		Course thisCourse = new Course();
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("course", thisCourse);
		arg.put("parentController", this);
		Window win = (Window) Executions.createComponents("/management/course_editor.zul", getSelf(), arg);
		getSelf().appendChild(win);
	}
}
