package com.probridge.vbox.zk.converter;

import org.zkoss.zk.ui.Component;
import org.zkoss.zkplus.databind.TypeConverter;

public class UserVhdQuotaConverter implements TypeConverter {

	@Override
	public Object coerceToUi(Object val, Component comp) {
		if (val == null)
			return "无";
		if (val instanceof Integer) {
			if (((int) val) == 0)
				return "未分配";
			else
				return Math.abs((int) val) + "GB";
		} else
			return IGNORE;
	}

	@Override
	public Object coerceToBean(Object val, Component comp) {
		return IGNORE;
	}
}
