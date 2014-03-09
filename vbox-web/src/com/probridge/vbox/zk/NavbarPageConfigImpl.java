package com.probridge.vbox.zk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class NavbarPageConfigImpl implements NavbarPageConfig {

	HashMap<String, NavbarPage> pageMap = new LinkedHashMap<String, NavbarPage>();

	public NavbarPageConfigImpl() {
		pageMap.put("fn1", new NavbarPage("fn1", "仪表盘", "/imgs/demo.png", "dashboard.zul"));
		pageMap.put("fn2", new NavbarPage("fn2", "vBox", "/imgs/doc.png", "vm_mgmt_new.zul"));
		pageMap.put("fn3", new NavbarPage("fn3", "用户", "/imgs/fn.png", "user_mgmt_new.zul"));
		pageMap.put("fn4", new NavbarPage("fn4", "课程", "/imgs/fn.png", "course_mgmt_new.zul"));
		pageMap.put("fn5", new NavbarPage("fn5", "母盘", "/imgs/fn.png", "golden_master_mgmt.zul"));
		pageMap.put("fn6", new NavbarPage("fn6", "审核中心", "/imgs/fn.png", "approval_mgmt.zul"));
		pageMap.put("fn7", new NavbarPage("fn7", "系统设置", "/imgs/fn.png", "configuration.zul"));
		// pageMap.put("fn8", new NavbarPage("fn8", "管理控制", "/imgs/fn.png",
		// "sysop.zul"));
		pageMap.put("fn9", new NavbarPage("fn9", "控制台", "/imgs/site.png", "console_access.zul"));
		pageMap.put("fn20", new NavbarPage("fn20", "安全退出", "/imgs/fn.png", "../logout.do?mgmt=1"));
	}

	public List<NavbarPage> getPages() {
		return new ArrayList<NavbarPage>(pageMap.values());
	}

	public NavbarPage getPage(String name) {
		return pageMap.get(name);
	}
}