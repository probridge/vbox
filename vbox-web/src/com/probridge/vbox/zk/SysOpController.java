package com.probridge.vbox.zk;

import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

public class SysOpController extends SelectorComposer<Div> {

	private static final long serialVersionUID = 1L;
	@Wire
	Label txtOutput;
	
	@Wire
	Button btnRefresh;

	@Override
	public void doAfterCompose(Div comp) throws Exception {
		super.doAfterCompose(comp);
	}

	@Listen("onClick = #btnRefresh")
	public void saveSetting() {
	}
}