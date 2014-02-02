package com.probridge.vbox.zk.converter;

import org.zkoss.zk.ui.Component;
import org.zkoss.zkplus.databind.TypeConverter;

public class VboxMemoryConverter implements TypeConverter {

	@Override
	public Object coerceToUi(Object val, Component comp) {
		if (val == null)
			return IGNORE;
		return val + "MB / ";
	}

	@Override
	public Object coerceToBean(Object val, Component comp) {
		return IGNORE;
	}
}
