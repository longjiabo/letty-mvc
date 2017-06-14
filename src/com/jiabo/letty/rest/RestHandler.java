package com.jiabo.letty.rest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jiabo.letty.exception.LettyRestException;
import com.jiabo.letty.servlet.LettyHandler;
import com.jiabo.letty.servlet.LettyMapper;
import com.jiabo.letty.util.Constant;
import com.jiabo.letty.util.HTTPUtil;
import com.jiabo.letty.util.IOUtil;

public class RestHandler extends LettyHandler {

	private static final Logger log = LoggerFactory
			.getLogger(RestHandler.class);

	private Object[] adapterParams(HttpServletRequest hreq,
			HttpServletResponse hres, LettyMapper lm, String requestUrl) {
		Map<String, String> paramMap = getParams(hreq);
		addStreamParams(hreq, paramMap);
		addPathVars(hreq, paramMap, lm, requestUrl);
		return setParams(hreq, hres, paramMap, lm.getMethod());
	}

	private void addStreamParams(HttpServletRequest hreq,
			Map<String, String> paramMap) {
		try {
			ServletInputStream stream = hreq.getInputStream();
			String str = IOUtil.readToString(stream);
			JSONObject obj = JSON.parseObject(str);
			if (obj != null) {
				for (Entry<String, Object> entry : obj.entrySet()) {
					paramMap.put(entry.getKey(),
							String.valueOf(entry.getValue()));
				}
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}

	@Override
	public void handleRequest(HttpServletRequest hreq,
			HttpServletResponse hres, LettyMapper lm)
			throws IllegalAccessException, IOException, ServletException {
		String requestUrl = HTTPUtil.removeAppPrefix(HTTPUtil.getURL(hreq), lm
				.getApplication().getAppName());
		Object[] args = adapterParams(hreq, hres, lm, requestUrl);
		try {
			Object obj = invoke(lm, args);
			if (!lm.getMethod().getReturnType().equals(void.class)
					&& obj != null) {
				hres.setContentType(Constant.CONTENT_JSON);
				hres.setContentLength(obj.toString().getBytes().length);
				hres.getWriter().write(obj.toString());
			}
		} catch (InvocationTargetException e) {
			log.error(e.getMessage());
			Throwable target = e.getTargetException();
			if (target instanceof LettyRestException) {
				LettyRestException ee = (LettyRestException) e
						.getTargetException();
				hres.setStatus(ee.getHttpCode());
			} else {
				log.error(e.getMessage(), e);
			}
		}

	}

}
