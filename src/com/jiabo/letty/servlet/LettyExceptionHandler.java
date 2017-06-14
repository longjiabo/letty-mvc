package com.jiabo.letty.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface LettyExceptionHandler {

	void handlerException(Exception e, HttpServletRequest hreq,
			HttpServletResponse hres);
}
