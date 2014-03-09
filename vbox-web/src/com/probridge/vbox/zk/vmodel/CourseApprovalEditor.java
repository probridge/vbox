package com.probridge.vbox.zk.vmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.UsersMapper;
import com.probridge.vbox.model.Course;
import com.probridge.vbox.model.Users;
import com.probridge.vbox.model.UsersExample;
import com.probridge.vbox.servlet.SaveCourseTask;

public class CourseApprovalEditor extends ProgressViewModel {
	private Course course;

	private ArrayList<Users> userList;
	private Users selectedUser = null;
	private ArrayList<Users> preApprovalUserList = new ArrayList<Users>();
	private ArrayList<Users> pickedUsers = new ArrayList<Users>();

	@Init
	public void init() {
		course = (Course) _execution.getArg().get("course");
		globalCommandName = "reloadCourseList";
		SqlSession session = null;
		try {
			session = VBoxConfig.sqlSessionFactory.openSession();
			UsersMapper uMapper = session.getMapper(UsersMapper.class);
			UsersExample uExp = new UsersExample();
			uExp.createCriteria().andUserExpirationGreaterThanOrEqualTo(new Date());
			uExp.or(uExp.createCriteria().andUserExpirationIsNull());
			userList = new ArrayList<Users>(uMapper.selectByExample(uExp));
			//
			List<String> items = Arrays.asList(course.getCoursePreapproveList().split("\\s*,\\s*"));
			//
			for (String each : items) {
				Users user = uMapper.selectByPrimaryKey(each);
				if (user != null)
					preApprovalUserList.add(user);
			}
		} catch (Exception e) {

		} finally {
			if (session != null)
				session.close();
		}
		//

	}

	@Command
	@NotifyChange({ "preApprovalUserList", "selectedUser" })
	public void addUser() {
		for (Users each : preApprovalUserList)
			if (each.getUserName().equals(selectedUser.getUserName())) {
				selectedUser = null;
				return;
			}
		preApprovalUserList.add(selectedUser);
		selectedUser = null;
	}

	@Command
	@NotifyChange("preApprovalUserList")
	public void removeUser() {
		Iterator<Users> it = preApprovalUserList.iterator();
		while (it.hasNext())
			if (pickedUsers.contains(it.next()))
				it.remove();
	}

	@Command
	@NotifyChange({ "progress", "progressMsg", "started", "running", "closeBtnLabel" })
	public void save() {
		StringBuffer sb = new StringBuffer();
		for (Users eachExisting : preApprovalUserList)
			sb.append(eachExisting.getUserName()).append(",");
		//
		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		course.setCoursePreapproveList(sb.toString());
		SaveCourseTask t = new SaveCourseTask(getSid(), getOpId(), course);
		submit(t);
		return;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public ArrayList<Users> getUserList() {
		return userList;
	}

	public void setUserList(ArrayList<Users> userList) {
		this.userList = userList;
	}

	public Users getSelectedUser() {
		return selectedUser;
	}

	public void setSelectedUser(Users selectedUser) {
		this.selectedUser = selectedUser;
	}

	public ArrayList<Users> getPreApprovalUserList() {
		return preApprovalUserList;
	}

	public void setPreApprovalUserList(ArrayList<Users> preApprovalUserList) {
		this.preApprovalUserList = preApprovalUserList;
	}

	public ArrayList<Users> getPickedUsers() {
		return pickedUsers;
	}

	public void setPickedUsers(ArrayList<Users> pickedUsers) {
		this.pickedUsers = pickedUsers;
	}

}
