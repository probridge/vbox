package com.probridge.vbox.zk.converter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.zkoss.zk.ui.Component;
import org.zkoss.zkplus.databind.TypeConverter;

import com.probridge.vbox.vmm.wmi.VirtualMachine.VMState;

public class VboxStatusConverter implements TypeConverter {

	private static HashMap<Integer, String> mapping = new HashMap<Integer, String>(5);
	static {
		for (VMState val : VMState.values())
			mapping.put(val.getValue(), val.getName());
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
