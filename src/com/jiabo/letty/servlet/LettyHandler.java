package com.jiabo.letty.servlet;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiabo.letty.util.ClassUtil;
import com.jiabo.letty.util.DateTimeUtil;

public abstract class LettyHandler {
	private static final Logger log = LoggerFactory
			.getLogger(LettyHandler.class);

	public abstract void handleRequest(HttpServletRequest hreq,
			HttpServletResponse hres, LettyMapper lm)
			throws IllegalAccessException, IOException, ServletException;

	public Map<String, String> getParams(HttpServletRequest hreq) {
		Map<String, String> paramMap = new HashMap<String, String>();
		Enumeration<String> names = hreq.getParameterNames();
		while (names.hasMoreElements()) {
			String key = names.nextElement();
			paramMap.put(key, hreq.getParameter(key));
		}
		return paramMap;
	}

	public void addPathVars(HttpServletRequest hreq,
			Map<String, String> paramsMap, LettyMapper lm, String requesturl) {
		if (lm.getPathParamNames() == null || lm.getPathParamNames().isEmpty())
			return;
		Pattern p = Pattern.compile(lm.getUrl());
		Matcher matcher = p.matcher(requesturl);
		if (matcher.matches()) {
			List<String> vars = lm.getPathParamNames();
			for (int i = 0; i < matcher.groupCount(); i++) {
				paramsMap.put(vars.get(i), matcher.group(i + 1));
			}
		}

	}

	public Object invoke(LettyMapper lm, Object[] args)
			throws IllegalAccessException, InvocationTargetException {
		Object obj = lm.getMethod().invoke(ApplicationContext.getBean(lm.getMethodClass()), args);
		return obj;
	}

	public Object[] setParams(HttpServletRequest hreq,
			HttpServletResponse hres, Map<String, String> paramMap,
			Method method) {
		Object[] params = new Object[method.getParameterTypes().length];
		if (params.length == 0)
			return params;
		int index = 0;
		for (Class<?> c : method.getParameterTypes()) {
			if (c.equals(int.class) || c.equals(Integer.class)) {
				params[index] = new Integer(paramMap.get(ClassUtil
						.getMethodParamsName(method, index)));
			} else if (c.equals(String.class)) {
				params[index] = paramMap.get(ClassUtil.getMethodParamsName(
						method, index));
			} else if (c.equals(HttpServletRequest.class)) {
				params[index] = hreq;
			} else if (c.equals(Date.class)) {
				String date = paramMap.get(ClassUtil.getMethodParamsName(
						method, index));
				if (date != null && !"".equals(date.trim())) {
					params[index] = DateTimeUtil.parseDate(date, null);
				}
			} else if (c.equals(HttpServletResponse.class)) {
				params[index] = hres;
			} else {
				Object obj = null;
				try {
					obj = c.newInstance();
				} catch (InstantiationException | IllegalAccessException e1) {
					log.error("", e1);
					continue;
				}
				for (Field f : c.getDeclaredFields()) {
					if (paramMap.containsKey(f.getName())) {
						try {
							f.setAccessible(true);
							String str = paramMap.get(f.getName());
							if (f.getType().equals(Integer.class)
									|| f.getType().equals(int.class)) {
								f.set(obj,
										str == null || "".equals(str.trim()) ? null
												: new Integer(str));
							} else if (f.getType().equals(Double.class)
									|| f.getType().equals(double.class)) {
								f.set(obj,
										str == null || "".equals(str.trim()) ? null
												: new Double(str));
							} else if (f.getType().equals(Date.class)) {
								f.set(obj, DateTimeUtil.parseDate(str, null));
							} else {
								f.set(obj, str);
							}
						} catch (IllegalAccessException e) {
							log.error("fill the params error", e);
							continue;
						}
					}
				}
				params[index] = obj;

			}
			index++;
		}
		return params;
	}

}
