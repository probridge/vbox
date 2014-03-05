package com.probridge.vbox.zk.converter.copy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.zkoss.zk.ui.Component;
import org.zkoss.zkplus.databind.TypeConverter;

public class GoldenMasterReadyStatusConverter implements TypeConverter {

	private static HashMap<String, String> mapping = new HashMap<String, String>(5);
	static {
		mapping.put("0", "就绪");
		mapping.put("1", "维护");
		mapping.put("2", "未同步");
	}

	@Override
	public Object coerceToUi(Object val, Component comp) {
		if (!(val instanceof String))
			return IGNORE;
		String ret = mapping.get(val);
		return (ret == null ? IGNORE : ret);
	}

	@Override
	public Object coerceToBean(Object val, Component comp) {
		if (!(val instanceof String))
			return IGNORE;
		Iterator<Entry<String, String>> it = mapping.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> cur = it.next();
			if (cur.getValue().equals(val))
				return cur.getKey();
		}
		return IGNORE;
	}
}
