package com.probridge.vbox.actions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.CourseMapper;
import com.probridge.vbox.dao.PreApprovedUserMapper;
import com.probridge.vbox.dao.UsersMapper;
import com.probridge.vbox.dao.VboxRequestMapper;
import com.probridge.vbox.model.Course;
import com.probridge.vbox.model.PreApprovedUser;
import com.probridge.vbox.model.Users;
import com.probridge.vbox.model.VM;
import com.probridge.vbox.model.VboxRequest;
import com.probridge.vbox.utils.CourseUtils;
import com.probridge.vbox.utils.Utility;

public class ApplyPlacedAction implements Action {
	public static final String SUCCEED = "applyplaced";
	public static final String REDIRECT = "home";
	private static final Logger logger = LoggerFactory.getLogger(ApplyPlacedAction.class);

	public String execute(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("apply placed");
		Session sess = SecurityUtils.getSubject().getSession();
		if (sess.getAttribute("Ticket") == null) {
			return REDIRECT;
		}

		sess.removeAttribute("Ticket");
		String identity = Utility.getStringVal(request.getAttribute("identity"));

		String requestedQuota = Utility.getStringVal(sess.getAttribute("RequestedQuota"));
		String courseCode = Utility.getStringVal(sess.getAttribute("CourseCode"));
		String justification = Utility.getStringVal(sess.getAttribute("Justification"));
		boolean firstTimeUser = "true".equalsIgnoreCase(Utility.getStringVal(sess.getAttribute("FirstTimeUser")));
		//
		logger.debug("requestedQuota:" + requestedQuota);
		logger.debug("courseCode:" + courseCode);
		logger.debug("justification:" + justification);
		logger.debug("InSessionfirstTimeUser:" + sess.getAttribute("FirstTimeUser"));
		logger.debug("firstTimeUser:" + firstTimeUser);
		//
		if ("GET".equalsIgnoreCase(request.getMethod())) {
			if (firstTimeUser) {
				Course courseInfo = null;
				if (!Utility.isEmptyOrNull(courseCode))
					courseInfo = isInCoursePreApproveList(identity, courseCode);
				logger.debug("courseInfo:" + courseInfo);

				PreApprovedUser userPreApproveInfo = isUserPreApproved(identity);
				logger.debug("userPreApproveInfo:" + userPreApproveInfo);

				if (userPreApproveInfo == null && courseInfo == null) {
					// manual
					logger.debug("Neither Pre-approved, entering manual");
					Users newUser = new Users();
					int quotaSelection = Integer.parseInt(requestedQuota);
					newUser.setUserName(identity);
					newUser.setUserVhdQuota(-1 * VBoxConfig.quotaSettings[quotaSelection]);
					newUser.setUserVhdName("");

					SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
					UsersMapper umapper = session.getMapper(UsersMapper.class);
					umapper.updateByPrimaryKeySelective(newUser);
					session.commit();
					session.close();

					logger.debug("User vhd request saved: " + (-1 * VBoxConfig.quotaSettings[quotaSelection]));
					// save course req (validate first)
					if (!Utility.isEmptyOrNull(courseCode))
						saveCourseRequest(identity, courseCode, justification);
				} else {
					logger.debug("First time user got preapproved, generating command for vhd, uservbox");
					int hypervisorId = userPreApproveInfo.getPreapproveHypervisorId().intValue();
					HashMap<String, Object> commandMap = new HashMap<String, Object>(5);
					commandMap.put("identity", identity);
					commandMap.put("hypervisor", hypervisorId);
					commandMap.put("personalVhd",
							getPersonalVHDQuota(userPreApproveInfo.getPreapproveVhdQuota(), courseInfo));
					commandMap.put("userVBox", getPersonalVBoxConfig(userPreApproveInfo, identity));
					if (courseInfo != null) {
						logger.debug("And course vbox");
						commandMap.put("courseVBox", getCourseVBoxConfig(courseInfo, identity, hypervisorId));
					} else if (!Utility.isEmptyOrNull(courseCode)) {
						logger.debug("course not pre-approved");
						saveCourseRequest(identity, courseCode, justification);
					}
					String secureKey = String.valueOf(System.currentTimeMillis());
					logger.debug("Command key saved to session: " + secureKey);
					sess.setAttribute(secureKey, commandMap);
					request.setAttribute("cmdkey", secureKey);
				}
			} else {
				logger.debug("Existing user");
				if (!Utility.isEmptyOrNull(courseCode)) {
					Course courseInfo = isInCoursePreApproveList(identity, courseCode);
					logger.debug("courseInfo: " + courseInfo);
					if (courseInfo != null) {
						Integer hypervisorId = VBoxConfig.repositoryLocation;
						//
						SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
						UsersMapper uMapper = session.getMapper(UsersMapper.class);
						Users user = uMapper.selectByPrimaryKey(identity);
						if (user != null)
							hypervisorId = user.getUserHypervisorId();
						session.close();

						logger.debug("course pre-approved... generating cmd for course vbox");
						VM courseConfig = getCourseVBoxConfig(courseInfo, identity,hypervisorId);
						//
						HashMap<String, Object> commandMap = new HashMap<String, Object>(5);
						commandMap.put("identity", identity);
						commandMap.put("hypervisor", courseConfig.getVmHypervisorId().intValue());
						commandMap.put("courseVBox", courseConfig);
						String secureKey = String.valueOf(System.currentTimeMillis());
						logger.debug("Command key saved to session: " + secureKey);
						sess.setAttribute(secureKey, commandMap);
						request.setAttribute("cmdkey", secureKey);
					} else {
						logger.debug("Course not pre-approved.");
						saveCourseRequest(identity, courseCode, justification);
					}
				} else {
					logger.debug("Existing user need course code anyway");
					// bullshit fuck off hacker
					throw new RuntimeException("非法请求");
				}
			}
		}
		return SUCCEED;
	}

	private boolean saveCourseRequest(String identity, String courseCode, String justification) {
		if (CourseUtils.isCourseValid(courseCode)) {
			logger.debug("saveCourseRequest: courseCode=" + courseCode + ", justify=" + justification);
			SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
			VboxRequestMapper vReqMapper = session.getMapper(VboxRequestMapper.class);
			VboxRequest req = new VboxRequest();
			req.setVboxRequestCode(courseCode);
			req.setVboxRequestJustification(justification);
			req.setVboxRequestOwner(identity);
			vReqMapper.insertSelective(req);
			session.commit();
			session.close();
			return true;
		}
		return false;
	}

	private PreApprovedUser isUserPreApproved(String userID) {
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		PreApprovedUserMapper mapper = session.getMapper(PreApprovedUserMapper.class);
		PreApprovedUser preApprovedUser = mapper.selectByPrimaryKey(userID);
		session.close();
		return preApprovedUser;
	}

	private Course isInCoursePreApproveList(String userID, String courseID) {
		if (!CourseUtils.isCourseValid(courseID))
			throw new RuntimeException("课程不存在，请重试");
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		CourseMapper mapper = session.getMapper(CourseMapper.class);
		Course thisCourse = mapper.selectByPrimaryKey(courseID);
		session.close();
		String approvedUserList = thisCourse.getCoursePreapproveList();
		if (Utility.isEmptyOrNull(approvedUserList))
			return null;
		List<String> items = Arrays.asList(approvedUserList.split("\\s*,\\s*"));
		if (items.contains(userID))
			return thisCourse;
		else
			return null;
	}

	public static int getPersonalVHDQuota(Integer preApprovedQuota, Course courseInfo) {
		int userVhdQuota = VBoxConfig.defaultVHDQuota;
		if (preApprovedQuota != null && !Utility.isZeroOrNull(preApprovedQuota))
			userVhdQuota = Math.max(userVhdQuota, preApprovedQuota);

		if (courseInfo != null && !Utility.isZeroOrNull(courseInfo.getCoursePreapproveQuota()))
			userVhdQuota = Math.max(userVhdQuota, courseInfo.getCoursePreapproveQuota());

		return userVhdQuota;
	}

	public static VM getPersonalVBoxConfig(PreApprovedUser preApproveInfo, String identity) {
		VM personalVM = new VM();
		personalVM.setVmId("--");
		personalVM.setVmType("1");
		personalVM.setVmName(VBoxConfig.PersonalVMName + Utility.normalized(identity));
		personalVM.setVmVhdGmType("0");
		personalVM.setVmOwner(identity);
		personalVM.setVmPersistance("1");
		personalVM.setVmTitle("我的vBox");
		personalVM.setVmDescription("使用属于我自己的vBox");
		if (preApproveInfo != null) {
			personalVM
					.setVmCores(Utility.isZeroOrNull(preApproveInfo.getPreapproveVmCores()) ? VBoxConfig.defaultCPUCores
							: preApproveInfo.getPreapproveVmCores());
			personalVM
					.setVmMemory(Utility.isZeroOrNull(preApproveInfo.getPreapproveVmMemory()) ? VBoxConfig.defaultMemory
							: preApproveInfo.getPreapproveVmMemory());
			personalVM
					.setVmNetworkType(Utility.isEmptyOrNull(preApproveInfo.getPreapproveVmNetwork()) ? VBoxConfig.defaultNetwork
							: preApproveInfo.getPreapproveVmNetwork());
			personalVM
					.setVmVhdGmImage(Utility.isEmptyOrNull(preApproveInfo.getPreapproveVmGoldenMaster()) ? VBoxConfig.defaultGoldenImage
							: preApproveInfo.getPreapproveVmGoldenMaster());
			personalVM.setVmHypervisorId(preApproveInfo.getPreapproveHypervisorId());
		} else {
			personalVM.setVmCores(VBoxConfig.defaultCPUCores);
			personalVM.setVmMemory(VBoxConfig.defaultMemory);
			personalVM.setVmNetworkType(VBoxConfig.defaultNetwork);
			personalVM.setVmVhdGmImage(VBoxConfig.defaultGoldenImage);
			personalVM.setVmHypervisorId(VBoxConfig.repositoryLocation);
		}
		return personalVM;
	}

	public static VM getCourseVBoxConfig(Course courseInfo, String identity, int hypervisorId) {
		VM courseVM = new VM();
		courseVM.setVmId("--");
		courseVM.setVmType("0");
		courseVM.setVmCourseCode(courseInfo.getCourseId());
		courseVM.setVmName(VBoxConfig.CourseVMName + Utility.normalized(courseInfo.getCourseId()) + "_"
				+ Utility.normalized(identity));
		courseVM.setVmVhdGmType("0");
		courseVM.setVmOwner(identity);
		courseVM.setVmPersistance("1");
		courseVM.setVmTitle(courseInfo.getCourseName());
		courseVM.setVmDescription(courseInfo.getCourseDescription());
		//
		courseVM.setVmCores(Utility.isZeroOrNull(courseInfo.getCourseVmCores()) ? VBoxConfig.defaultCPUCores
				: courseInfo.getCourseVmCores());
		courseVM.setVmMemory(Utility.isZeroOrNull(courseInfo.getCourseVmMemory()) ? VBoxConfig.defaultMemory
				: courseInfo.getCourseVmMemory());
		courseVM.setVmNetworkType(Utility.isEmptyOrNull(courseInfo.getCourseVmNetwork()) ? VBoxConfig.defaultNetwork
				: courseInfo.getCourseVmNetwork());
		courseVM.setVmVhdGmImage(Utility.isEmptyOrNull(courseInfo.getCourseVmGoldenMaster()) ? VBoxConfig.defaultGoldenImage
				: courseInfo.getCourseVmGoldenMaster());
		courseVM.setVmHypervisorId(hypervisorId);
		return courseVM;
	}
}
