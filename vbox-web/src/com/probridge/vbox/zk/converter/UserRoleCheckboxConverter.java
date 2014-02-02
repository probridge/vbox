package com.probridge.vbox.zk.converter;

import java.util.List;
import java.util.StringTokenizer;

import org.zkoss.zk.ui.Component;
import org.zkoss.zkplus.databind.TypeConverter;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Hlayout;

public class UserRoleCheckboxConverter implements TypeConverter {

	@Override
	public Object coerceToUi(Object val, Component comp) {
		if (comp instanceof Hlayout && val instanceof String) {
			List<Component> c = ((Hlayout) comp).getChildren();
			StringTokenizer st = new StringTokenizer((String) val);
			while (st.hasMoreTokens()) {
				String ss = st.nextToken();
				for (Component cc : c)
					if (cc instanceof Checkbox
							&& ((Checkbox) cc).getLabel()
									.equals(ss))
						((Checkbox) cc).setChecked(true);
			}
		}
		return val;
	}

	@Override
	public Object coerceToBean(Object val, Component comp) {
		return IGNORE;
	}
}
