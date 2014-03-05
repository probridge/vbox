package com.probridge.vbox.zk.converter.copy;

import org.zkoss.zk.ui.Component;
import org.zkoss.zkplus.databind.TypeConverter;

import com.probridge.vbox.utils.Utility;

public class CoursePreapprovalListConverter implements TypeConverter {
	@Override
	public Object coerceToUi(Object val, Component comp) {
		if (!(val instanceof String))
			return "0";
		String str = ((String) val);
		if (Utility.isEmptyOrNull(str))
			return "0";
		return String.valueOf(str.split("\\s*,\\s*").length);
	}

	@Override
	public Object coerceToBean(Object val, Component comp) {
		return IGNORE;
	}
}
