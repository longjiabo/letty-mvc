package com.jiabo.letty.mvc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiabo.letty.mvc.annoation.ResponseBody;
import com.jiabo.letty.servlet.LettyHandler;
import com.jiabo.letty.servlet.LettyMapper;
import com.jiabo.letty.util.Constant;
import com.jiabo.letty.util.HTTPUtil;

public class MVCHandler extends LettyHandler {
	private static final Logger log = LoggerFactory.getLogger(MVCHandler.class);

	public void handleRequest(HttpServletRequest hreq,
			HttpServletResponse hres, LettyMapper lm)
			throws IllegalAccessException, IOException, ServletException {
		String requestUrl = HTTPUtil.removeAppPrefix(HTTPUtil.getURL(hreq), lm
				.getApplication().getAppName());
		Object[] args = adapterParams(lm, hreq, hres, requestUrl);
		Object obj;
		try {
			obj = invoke(lm, args);
			resolve(hreq, hres, lm, obj);
		} catch (InvocationTargetException e) {
			log.error(e.getMessage(), e);
		}

	}

	private Object[] adapterParams(LettyMapper lm, HttpServletRequest hreq,
			HttpServletResponse hres, String requestUrl) {
		Map<String, String> paramMap = getParams(hreq);
		Method method = lm.getMethod();
		addPathVars(hreq, paramMap, lm, requestUrl);
		return setParams(hreq, hres, paramMap, method);
	}

	private void resolve(HttpServletRequest hreq, HttpServletResponse hres,
			LettyMapper lm, Object obj) throws IOException, ServletException {
		if (obj == null)
			return;
		if (lm.getMethod().getReturnType().equals(void.class))
			return;
		if (!lm.getMethod().getReturnType().equals(String.class)) {
			log.warn("method return type is not String or void,could not be resolved.");
			return;
		}
		String result = (String) obj;
		if (result.startsWith("redirect:")) {
			String url = result.split(":")[1].trim();
			if (!url.startsWith(Constant.leftSplit)) {
				url = Constant.leftSplit + url;
			}
			if (lm.getApplication().getAppName() != null
					&& !"".equals(lm.getApplication().getAppName().trim())) {
				url = Constant.leftSplit + lm.getApplication().getAppName()
						+ url;
			}
			hres.setHeader("Location", url);
			hres.setStatus(303);
			return;
		}
		if (result.startsWith("forward:")) {
			String url = result.split(":")[1].trim();
			if (!url.startsWith(Constant.leftSplit)) {
				url = Constant.leftSplit + url;
			}
			hreq.getRequestDispatcher(url).forward(hreq, hres);
			return;
		}
		if (lm.getMethod().isAnnotationPresent(ResponseBody.class)) {
			ServletOutputStream stream = hres.getOutputStream();
			stream.write(result.getBytes());
			stream.close();
		} else {
			resolve(result, hreq, hres, lm);
		}
	}

	private void resolve(String path, HttpServletRequest hreq,
			HttpServletResponse hres, LettyMapper lm) {
		ViewResolver vr = lm.getApplication().getViewResolver();
		vr.resolve(hreq, hres, path);
	}

}
