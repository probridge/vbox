package com.probridge.vbox.zk.converter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.zkoss.zk.ui.Component;
import org.zkoss.zkplus.databind.TypeConverter;

public class VboxHeartbeatConverter implements TypeConverter {

	private static HashMap<Integer, String> mapping = new HashMap<Integer, String>(5);
	static {
		mapping.put(2, "正常");
		mapping.put(6, "错误");
		mapping.put(12, "无联系");
		mapping.put(13, "断开");
	}

	@Override
	public Object coerceToUi(Object val, Component comp) {
		if (!(val instanceof Integer))
			return IGNORE;
		String ret = mapping.get((Integer) val);
		return (ret == null ? IGNORE : ret);
	}

	@Override
	public Object coerceToBean(Object val, Component comp) {
		if (!(val instanceof String))
			return IGNORE;
		Iterator<Entry<Integer, String>> it = mapping.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, String> cur = it.next();
			if (cur.getValue().equals(val))
				return cur.getKey();
		}
		return IGNORE;
	}
}
