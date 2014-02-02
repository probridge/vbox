package com.probridge.vbox.zk;

import org.apache.shiro.SecurityUtils;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

public class BannerController extends SelectorComposer<Div> {

	private static final long serialVersionUID = 1L;
	@Wire
	Label userID;

	@Override
	public void doAfterCompose(Div comp) throws Exception {
		super.doAfterCompose(comp);
		userID.setValue(SecurityUtils.getSubject().getPrincipal().toString());
	}
}