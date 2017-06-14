package com.jiabo.letty.util;

import javax.servlet.http.HttpServletRequest;

import com.jiabo.letty.Application;

public class LettyUtil {

	public static Application getApplication(HttpServletRequest hreq) {
		return (Application) hreq.getAttribute(Constant.ATTRIBUTE_APPLICATION);
	}

	public static boolean isdev() {
		return System.getProperty("os.name").contains("Window");
	}

}
