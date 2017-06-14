package com.jiabo.letty.util;

import javax.servlet.http.HttpServletRequest;

public class HTTPUtil {
	public static String getURL(HttpServletRequest hreq) {
		return hreq.getRequestURI().substring(hreq.getContextPath().length());
	}

	public static String removeAppPrefix(String url, String appName) {
		if (url == null)
			return null;
		if (StringUtil.isEmpty(appName))
			return url;
		if (!url.startsWith(Constant.leftSplit))
			url = Constant.leftSplit + url;
		if (!url.startsWith(Constant.leftSplit + appName))
			return url;
		url = url.substring(appName.length() + 1);
		if ("".equals(url.trim()))
			url = Constant.leftSplit;
		if (!url.startsWith(Constant.leftSplit))
			url = Constant.leftSplit + url;
		return url;
	}
}
