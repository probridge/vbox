package com.probridge.vbox.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.VMMapper;
import com.probridge.vbox.model.VM;

public class Viewbox2Action implements Action {
	private static final Logger logger = LoggerFactory
			.getLogger(Viewbox2Action.class);

	public static final String SUCCEED = "viewbox2";
	public static final String SUCCEED_ALT = "viewboxalt";

	public String execute(HttpServletRequest request,
			HttpServletResponse response) {
		String uuid = request.getParameter("uuid");
		logger.debug("Setting up RDP session for " + uuid);
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		VMMapper mapper = session.getMapper(VMMapper.class);
		VM vm = mapper.selectByPrimaryKey(uuid);
		session.close();
		//
		Session sess = SecurityUtils.getSubject().getSession();		
		sess.setAttribute("rdp_target", vm.getVmIpAddress());
		sess.setAttribute("rdp_username", VBoxConfig.guestOSUserName);
		sess.setAttribute("rdp_password", vm.getVmGuestPassword());
		//
		return SUCCEED;
	}
}
