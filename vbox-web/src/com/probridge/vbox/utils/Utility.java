package com.probridge.vbox.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.probridge.vbox.VBoxConfig;

public class Utility {

	public static boolean isZeroOrNull(Integer val) {
		return (val == null || val == 0);
	}

	public static boolean isEmptyOrNull(String val) {
		return (val == null || val.isEmpty());
	}

	public static String getStringVal(Object obj) {
		return (obj == null ? null : obj.toString());
	}

	public static String normalized(String str) {
		return str.replace('\'', '_').replace(',', '_').replace(' ', '_').replace('\"', '_');
	}

	public static long KB = 1024;
	public static long MB = 1024 * KB;
	public static long GB = 1024 * MB;
	public static long TB = 1024 * GB;

	public static String formatSize(long bytes) {
		DecimalFormat df = new DecimalFormat(".0");
		if (bytes > TB)
			return df.format(bytes * 1.0d / TB) + "TB";
		if (bytes > GB)
			return df.format(bytes * 1.0d / GB) + "GB";
		if (bytes > MB)
			return df.format(bytes * 1.0d / MB) + "MB";
		if (bytes > KB)
			return df.format(bytes * 1.0d / KB) + "KB";
		return bytes + "B";
	}
	
	public static String formatDate(long date) {
		SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
		return sdf.format(new Date(date));
	}
	
	public static double roundDouble(double iVal) {
		DecimalFormat df = new DecimalFormat(".0");
		String tmp = df.format(iVal);
		return Double.parseDouble(tmp);
	}

	public static String generateUserVhdFileName(String userid) {
		return VBoxConfig.userVhdPrefix + normalized(userid) + ".vhd";
	}
}
