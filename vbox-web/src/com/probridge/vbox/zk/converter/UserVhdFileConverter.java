package com.probridge.vbox.zk.converter;

import org.zkoss.zk.ui.Component;
import org.zkoss.zkplus.databind.TypeConverter;

import com.probridge.vbox.utils.Utility;

public class UserVhdFileConverter implements TypeConverter {

	@Override
	public Object coerceToUi(Object val, Component comp) {
		if (!(val instanceof String))
			return IGNORE;
		String ret = (String) val;
		return (Utility.isEmptyOrNull(ret) ? "未分配" : ret);
	}

	@Override
	public Object coerceToBean(Object val, Component comp) {
		return IGNORE;
	}
}
