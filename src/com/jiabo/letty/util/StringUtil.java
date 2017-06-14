package com.jiabo.letty.util;

public class StringUtil {
	/**
	 * String is null or " "
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		return str == null || "".equals(str.trim());
	}
}
