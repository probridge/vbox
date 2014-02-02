package com.probridge.vbox.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.VMMapper;
import com.probridge.vbox.model.VM;

public class ViewboxAction implements Action {
	private static final Logger logger = LoggerFactory
			.getLogger(ViewboxAction.class);

	public static final String SUCCEED = "viewbox";
	public static final String SUCCEED_ALT = "viewboxalt";

	public String execute(HttpServletRequest request,
			HttpServletResponse response) {
		String uuid = request.getParameter("uuid");
		logger.debug("Setting up RDP session for " + uuid);
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		VMMapper mapper = session.getMapper(VMMapper.class);
		VM vm = mapper.selectByPrimaryKey(uuid);
		session.close();
		request.setAttribute("server", vm.getVmIpAddress());
		request.setAttribute("username", VBoxConfig.guestOSUserName);
		request.setAttribute("password", vm.getVmGuestPassword());
		return SUCCEED;
	}
}
