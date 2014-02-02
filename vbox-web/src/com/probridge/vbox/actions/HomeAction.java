package com.probridge.vbox.actions;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.WordOfTheDayMapper;
import com.probridge.vbox.model.WordOfTheDay;
import com.probridge.vbox.model.WordOfTheDayExample;

public class HomeAction implements Action {
	public static final String SUCCEED = "home";
	private static final Logger logger = LoggerFactory
			.getLogger(HomeAction.class);

	public String execute(HttpServletRequest request,
			HttpServletResponse response) {
		logger.debug("entering home");
		Session sess = SecurityUtils.getSubject().getSession();
		request.setAttribute("selected", sess.getAttribute("selected"));
		request.setAttribute("vboxlist", sess.getAttribute("vboxlist"));
		request.setAttribute("extended", sess.getAttribute("extended"));
		request.setAttribute("preferred", sess.getAttribute("preferred"));
		request.setAttribute("personal", sess.getAttribute("personal"));
		request.setAttribute("notify_info", sess.getAttribute("home_notify_info"));
		sess.removeAttribute("home_notify_info");
		//
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		WordOfTheDayMapper mapper = session.getMapper(WordOfTheDayMapper.class);
		WordOfTheDayExample exp = new WordOfTheDayExample();
		exp.setOrderByClause("RAND() LIMIT 1");
		List<WordOfTheDay> wofd = mapper.selectByExample(exp);
		session.close();
		if (wofd.size()>=1) {
			request.setAttribute("wofd", wofd.get(0).getWofdEnglish());
			request.setAttribute("wofdname", wofd.get(0).getWofdEnglishName());
		}
		return SUCCEED;
	}
}
