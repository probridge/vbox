package com.probridge.vbox;

import java.io.IOException;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFilenameFilter;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.dao.SysParamMapper;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class VBoxConfig {
	private static final Logger logger = LoggerFactory.getLogger(VBoxConfig.class);

	public static String configPath;
	public static SqlSessionFactory sqlSessionFactory = null;

	public static int vmAdminTaskThreadPoolSize = 20;
	public static int vmMonitorThreadPoolSize = 100;

	public static long vmShutdownDelay = 60000;
	public static long monitorPoolingInterval = 10000;
	public static int CourseRequestPerUserLimit = 5;
	public static int cpuMaxHistory = 120;
	public static int[] quotaSettings = { 5, 10, 20, 50 };

	public static int defaultVHDQuota = 5;
	public static int defaultCPUCores = 1;
	public static int defaultMemory = 1024;
	public static String defaultNetwork = "1";
	public static String defaultGoldenImage = "";

	public static String userVhdPrefix = "";
	public static String gmVhdPrefix = "";

	public static String PersonalVMName = "";
	public static String CourseVMName = "";
	public static String MaintVMName = "";

	public static String guestOSUserName = "";
	public static String shareName = "";
	public static String osManagementAccount = "";
	public static String osManagementPassword = "";

	public static String hypervisorUrl = "";
	public static String hypervisorConsole = "";
	public static String hypervisorUser = "";
	public static String hypervisorPwd = "";

	public static String dataDrive = "";
	public static String goldenMasterImageDirectory = "";
	public static String userImageDirectory = "";
	public static String userDataDirectory = "";

	public static String vmTemplateName = "";
	public static String vmUserVhdTemplateName = "";
	public static String vmInternalNetworkSwitchName = "";
	public static String vmExternalNetworkSwitchName = "";

	public static String jAccountSiteId = "";
	public static String jAccountSuffix = "";

	public static String gatewayServerName = "";
	public static String gatewayServerPort = "";

	public static String globalNotice = "";

	public static int repositoryLocation = 0;
	public static String repositoryShareName = "";
	public static String vmMaintLandingZone = "";
	
	public static String smbClientBufferSize = "";

	public static int vBoxStatusChangeTimeout = 180;

	public static String systemVersion = "vBox云计算平台 v2.0.4 build 20140210 (C) ProBridge, 2013 - 2014";

	public static SmbFilenameFilter fileFilter = new SmbFilenameFilter() {
		@Override
		public boolean accept(SmbFile dir, String name) throws SmbException {
			if (name.equalsIgnoreCase("$RECYCLE.BIN"))
				return false;
			if (name.equalsIgnoreCase("System Volume Information"))
				return false;
			if (name.toLowerCase().startsWith("ntuser"))
				return false;
			if (name.equalsIgnoreCase("AppData"))
				return false;
			if (name.equalsIgnoreCase("Desktop.ini"))
				return false;
			if (name.startsWith("."))
				return false;
			return true;
		}
	};

	static {
		String prefix = "WEB-INF/";
		String path = VBoxConfig.class.getResource("/").getPath();
		configPath = path.substring(0, path.indexOf(prefix) + prefix.length());

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(lc);
		lc.reset();
		try {
			configurator.doConfigure(configPath + "logback.xml");
		} catch (JoranException e) {
			e.printStackTrace();
		}
		logger.info("Logger started, config file is: " + configPath + "logback.xml");

		SqlSession sess = null;
		try {
			logger.info("Initializing system parameters.");
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsStream("../mybatis.xml"));
			sess = sqlSessionFactory.openSession();
			SysParamMapper spMapper = sess.getMapper(SysParamMapper.class);

			vmAdminTaskThreadPoolSize = Integer.parseInt(spMapper.selectByPrimaryKey("vmAdminTaskThreadPoolSize")
					.getSysparamValue());
			vmMonitorThreadPoolSize = Integer.parseInt(spMapper.selectByPrimaryKey("vmMonitorThreadPoolSize")
					.getSysparamValue());
			vmShutdownDelay = Integer.parseInt(spMapper.selectByPrimaryKey("vmShutdownDelay").getSysparamValue());
			monitorPoolingInterval = Integer.parseInt(spMapper.selectByPrimaryKey("monitorPoolingInterval")
					.getSysparamValue());
			CourseRequestPerUserLimit = Integer.parseInt(spMapper.selectByPrimaryKey("CourseRequestPerUserLimit")
					.getSysparamValue());

			cpuMaxHistory = Integer.parseInt(spMapper.selectByPrimaryKey("cpuMaxHistory").getSysparamValue());

			String[] strQuotaSettings = spMapper.selectByPrimaryKey("quotaSettings").getSysparamValue().split(",");
			quotaSettings = new int[strQuotaSettings.length];
			for (int i = 0; i < strQuotaSettings.length; i++) {
				quotaSettings[i] = Integer.parseInt(strQuotaSettings[i]);
			}

			defaultVHDQuota = Integer.parseInt(spMapper.selectByPrimaryKey("defaultVHDQuota").getSysparamValue());
			defaultCPUCores = Integer.parseInt(spMapper.selectByPrimaryKey("defaultCPUCores").getSysparamValue());
			defaultMemory = Integer.parseInt(spMapper.selectByPrimaryKey("defaultMemory").getSysparamValue());

			defaultNetwork = spMapper.selectByPrimaryKey("defaultNetwork").getSysparamValue();
			defaultGoldenImage = spMapper.selectByPrimaryKey("defaultGoldenImage").getSysparamValue();

			userVhdPrefix = spMapper.selectByPrimaryKey("userVhdPrefix").getSysparamValue();
			gmVhdPrefix = spMapper.selectByPrimaryKey("gmVhdPrefix").getSysparamValue();

			PersonalVMName = spMapper.selectByPrimaryKey("PersonalVMName").getSysparamValue();
			CourseVMName = spMapper.selectByPrimaryKey("CourseVMName").getSysparamValue();
			MaintVMName = spMapper.selectByPrimaryKey("MaintVMName").getSysparamValue();

			guestOSUserName = spMapper.selectByPrimaryKey("guestOSUserName").getSysparamValue();
			shareName = spMapper.selectByPrimaryKey("shareName").getSysparamValue();
			osManagementAccount = spMapper.selectByPrimaryKey("osManagementAccount").getSysparamValue();
			osManagementPassword = spMapper.selectByPrimaryKey("osManagementPassword").getSysparamValue();

			hypervisorConsole = spMapper.selectByPrimaryKey("hypervisorConsole").getSysparamValue();
			hypervisorUrl = spMapper.selectByPrimaryKey("hypervisorUrl").getSysparamValue();
			hypervisorUser = spMapper.selectByPrimaryKey("hypervisorUser").getSysparamValue();
			hypervisorPwd = spMapper.selectByPrimaryKey("hypervisorPwd").getSysparamValue();

			dataDrive = spMapper.selectByPrimaryKey("dataDrive").getSysparamValue();
			goldenMasterImageDirectory = spMapper.selectByPrimaryKey("goldenMasterImageDirectory").getSysparamValue();
			userImageDirectory = spMapper.selectByPrimaryKey("userImageDirectory").getSysparamValue();
			userDataDirectory = spMapper.selectByPrimaryKey("userDataDirectory").getSysparamValue();

			vmTemplateName = spMapper.selectByPrimaryKey("vmTemplateName").getSysparamValue();
			vmUserVhdTemplateName = spMapper.selectByPrimaryKey("vmUserVhdTemplateName").getSysparamValue();
			vmInternalNetworkSwitchName = spMapper.selectByPrimaryKey("vmInternalNetworkSwitchName").getSysparamValue();
			vmExternalNetworkSwitchName = spMapper.selectByPrimaryKey("vmExternalNetworkSwitchName").getSysparamValue();

			jAccountSuffix = spMapper.selectByPrimaryKey("jAccountSuffix").getSysparamValue();
			jAccountSiteId = spMapper.selectByPrimaryKey("jAccountSiteId").getSysparamValue();

			gatewayServerName = spMapper.selectByPrimaryKey("gatewayServerName").getSysparamValue();
			gatewayServerPort = spMapper.selectByPrimaryKey("gatewayServerPort").getSysparamValue();

			globalNotice = spMapper.selectByPrimaryKey("globalNotice").getSysparamValue().trim();

			repositoryLocation = Integer.parseInt(spMapper.selectByPrimaryKey("repositoryLocation").getSysparamValue());
			repositoryShareName = spMapper.selectByPrimaryKey("repositoryShareName").getSysparamValue().trim();

			vmMaintLandingZone = spMapper.selectByPrimaryKey("vmMaintLandingZone").getSysparamValue().trim();

			vBoxStatusChangeTimeout = Integer.parseInt(spMapper.selectByPrimaryKey("vBoxStatusChangeTimeout")
					.getSysparamValue());
			
			logger.info("System parameters initialized.");
		} catch (IOException e) {
			logger.error("DB Connection Failed.", e);
		} finally {
			sess.close();
		}
	}
}
