package com.probridge.vbox.fileupload;

import java.io.UnsupportedEncodingException;

import org.apache.shiro.codec.Hex;

public class Utility {
	public static String fromHex(String hexString) {
		try {
			return new String(Hex.decode(hexString),"utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
}
