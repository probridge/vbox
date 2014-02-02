package com.probridge.vbox.actions;

import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.UsersMapper;
import com.probridge.vbox.model.Users;

import edu.sjtu.jaccount.JAccountManager;

public class JAccountAction implements Action {
	public static final String SUCCEED = "landing";
	public static final String PROCEED = "jaccount";
	public static final String ERROR = "problem";
	private static final Logger logger = LoggerFactory.getLogger(JAccountAction.class);

	public String execute(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("entering jaccount processing");
		JAccountManager jam = new JAccountManager(VBoxConfig.jAccountSiteId, VBoxConfig.configPath);
		@SuppressWarnings("rawtypes")
		Hashtable ht = jam.checkLogin(request, response, request.getSession(), request.getRequestURI());
		if (ht != null && ht.get("uid") != null) {
			// check user db - insert user
			String uid = ht.get("uid").toString().toLowerCase() + VBoxConfig.jAccountSuffix;
			String userDesc = ht.get("dept") + " " + ht.get("chinesename");
			//
			SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
			UsersMapper mapper = session.getMapper(UsersMapper.class);
			Users thisUser = mapper.selectByPrimaryKey(uid);
			String encodedPwd = new Sha512Hash(uid, "jaccount_salt").toHex().substring(0, 20);
			if (thisUser == null) {
				thisUser = new Users();
				thisUser.setUserName(uid);
				thisUser.setUserEnabled("1");
				thisUser.setUserDescription(userDesc);
				thisUser.setUserExpiration(null);
				thisUser.setUserPassword(encodedPwd);
				thisUser.setUserPwdExpire(null);
				thisUser.setUserRole("ROLE_USER");
				thisUser.setUserType("1");
				thisUser.setUserVhdName(null);
				thisUser.setUserVhdQuota(null);
				thisUser.setUserHypervisorId(null);
				mapper.insert(thisUser);
				session.commit();
			}
			//
			UsernamePasswordToken loginToken = new UsernamePasswordToken(uid, encodedPwd);
			loginToken.setRememberMe(true);
			Subject currentUser = SecurityUtils.getSubject();
			try {
				currentUser.login(loginToken);
			} catch (Exception e) {
				request.setAttribute("error", "登录发生错误，请联系我们。" + e.getMessage());
				return ERROR;
			}
			// perform login
			return SUCCEED;
		} else {
			jam.logout(request, response, request.getRequestURI());
			return null;
		}
	}
}
