package com.probridge.vbox.zk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class NavbarPageConfigImpl implements NavbarPageConfig {

	HashMap<String, NavbarPage> pageMap = new LinkedHashMap<String, NavbarPage>();

	public NavbarPageConfigImpl() {
		pageMap.put("fn0", new NavbarPage("fn0", "首页", "/imgs/management/Home_icon.png", "landing.zul"));
		pageMap.put("fn1", new NavbarPage("fn1", "仪表盘", "/imgs/management/Dashboard_icon.png", "dashboard.zul"));
		pageMap.put("fn2", new NavbarPage("fn2", "vBox", "/imgs/management/vBox_icon.png", "vm_mgmt_new.zul"));
		pageMap.put("fn3", new NavbarPage("fn3", "用户", "/imgs/management/Users_icon.png", "user_mgmt_new.zul"));
		pageMap.put("fn4", new NavbarPage("fn4", "课程", "/imgs/management/Courses_icon.png", "course_mgmt_new.zul"));
		pageMap.put("fn5",
				new NavbarPage("fn5", "母盘", "/imgs/management/Images_icon.png", "golden_master_mgmt_new.zul"));
		pageMap.put("fn6", new NavbarPage("fn6", "审核中心", "/imgs/management/Review_icon.png", "approval_mgmt.zul"));
		pageMap.put("fn7", new NavbarPage("fn7", "系统设置", "/imgs/management/Settings_icon.png", "configuration.zul"));
		pageMap.put("fn8", new NavbarPage("fn8", "控制台", "/imgs/management/World_icon.png", "console_access.zul"));
		pageMap.put("fn9", new NavbarPage("fn9", "安全退出", "/imgs/management/Logout_icon.png", "../logout.do?mgmt=1"));
	}

	public List<NavbarPage> getPages() {
		return new ArrayList<NavbarPage>(pageMap.values());
	}

	public NavbarPage getPage(String name) {
		return pageMap.get(name);
	}
}