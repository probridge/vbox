package com.probridge.vbox.zk;

import java.util.List;

public interface NavbarPageConfig {
	/** get pages of this application **/
	public List<NavbarPage> getPages();

	/** get page **/
	public NavbarPage getPage(String name);
}