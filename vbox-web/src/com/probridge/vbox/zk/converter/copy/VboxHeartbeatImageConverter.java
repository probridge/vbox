package com.probridge.vbox.zk.converter.copy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

public class VboxHeartbeatImageConverter implements Converter<Object, Object, Component> {

	private static HashMap<Integer, String> mapping = new HashMap<Integer, String>(5);
	static {
		mapping.put(2, "../imgs/hb_ok.gif");
		mapping.put(6, "../imgs/hb_error.gif");
		mapping.put(12, "../imgs/hb_disc.gif");
		mapping.put(13, "../imgs/hb_error.gif");
	}

	@Override
	public Object coerceToUi(Object val, Component comp, BindContext ctx) {
		if (!(val instanceof Integer))
			return IGNORED_VALUE;
		String ret = mapping.get((Integer) val);
		return (ret == null ? IGNORED_VALUE : ret);
	}

	@Override
	public Object coerceToBean(Object val, Component comp, BindContext ctx) {
		if (!(val instanceof String))
			return IGNORED_VALUE;
		Iterator<Entry<Integer, String>> it = mapping.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, String> cur = it.next();
			if (cur.getValue().equals(val))
				return cur.getKey();
		}
		return IGNORED_VALUE;
	}
}
