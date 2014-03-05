package com.probridge.vbox.zk.converter.copy;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

public class VboxMemoryConverter implements Converter<Object, Object, Component> {

	@Override
	public Object coerceToUi(Object val, Component comp, BindContext ctx) {
		if (val == null)
			return IGNORED_VALUE;
		return val + "MB / ";
	}

	@Override
	public Object coerceToBean(Object val, Component comp, BindContext ctx) {
		return IGNORED_VALUE;
	}
}
