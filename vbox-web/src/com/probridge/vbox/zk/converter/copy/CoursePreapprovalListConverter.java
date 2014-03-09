package com.probridge.vbox.zk.converter.copy;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

import com.probridge.vbox.utils.Utility;

public class CoursePreapprovalListConverter implements Converter<Object, Object, Component> {
	@Override
	public Object coerceToUi(Object val, Component comp, BindContext ctx) {
		if (!(val instanceof String))
			return "0";
		String str = ((String) val);
		if (Utility.isEmptyOrNull(str))
			return "0";
		return String.valueOf(str.split("\\s*,\\s*").length);
	}

	@Override
	public Object coerceToBean(Object val, Component comp, BindContext ctx) {
		return IGNORED_VALUE;
	}
}
