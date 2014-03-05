package com.probridge.vbox.zk.converter.copy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

import com.probridge.vbox.vmm.wmi.VirtualMachine.VMState;

public class VboxStatusImageConverter implements Converter<Object, Object, Component> {

	private static HashMap<Integer, String> mapping = new HashMap<Integer, String>(5);
	static {
		for (VMState val : VMState.values())
			mapping.put(val.getValue(), val.getName());
	}

	@Override
	public Object coerceToUi(Object val, Component comp, BindContext ctx) {
		if (!(val instanceof Integer))
			return IGNORED_VALUE;
		String ret = mapping.get((Integer) val);
		return (ret == null ? IGNORED_VALUE : "../imgs/" + ret + ".gif");
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
