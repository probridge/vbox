package com.probridge.vbox.zk.converter.copy;

import java.util.Date;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

import com.probridge.vbox.model.Users;

public class UserStatusImageConverter implements Converter<Object, Object, Component> {

	@Override
	public Object coerceToUi(Object val, Component comp, BindContext ctx) {
		if (!(val instanceof Users))
			return IGNORED_VALUE;
		Users user = (Users) val;
		if ("0".equals(user.getUserEnabled()))
			return "../imgs/user_disabled.gif";
		else if ("1".equals(user.getUserEnabled())) {
			if (user.getUserExpiration() == null || user.getUserExpiration().after(new Date()))
				return "../imgs/user_enabled.gif";
			else
				return "../imgs/user_expired.gif";
		} else
			return IGNORED_VALUE;
	}

	@Override
	public Object coerceToBean(Object val, Component comp, BindContext ctx) {
		return IGNORED_VALUE;
	}
}
