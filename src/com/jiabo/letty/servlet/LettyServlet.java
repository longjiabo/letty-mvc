package com.jiabo.letty.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiabo.letty.Application;
import com.jiabo.letty.exception.LettyException;
import com.jiabo.letty.mvc.HandlerInterceptorAdapter;
import com.jiabo.letty.util.Constant;
import com.jiabo.letty.util.HTTPUtil;

@WebServlet
public class LettyServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1095192250678362142L;
	private final Logger log = LoggerFactory.getLogger(LettyServlet.class);
	private static ApplicationContext ac;

	public static ApplicationContext getApplicationContext() {
		return ac;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		ac = new ApplicationContext(config);
		ac.init();
	}

	private void setCharacterEncode(HttpServletRequest req,
			HttpServletResponse res) throws UnsupportedEncodingException {
		req.setCharacterEncoding("utf-8");
		res.setCharacterEncoding("utf-8");
	}

	@Override
	public void service(ServletRequest req, ServletResponse res)
			throws ServletException, IOException {
		HttpServletRequest hreq = (HttpServletRequest) req;
		HttpServletResponse hres = (HttpServletResponse) res;
		setCharacterEncode(hreq, hres);
		try {
			handleRequest(hreq, hres);
		} catch (IllegalAccessException | IllegalArgumentException
				| LettyException e) {
			log.error("invoke error", e);
		}
	}

	private void handleRequest(HttpServletRequest hreq, HttpServletResponse hres)
			throws IllegalAccessException, IOException, ServletException {
		String url = HTTPUtil.getURL(hreq);
		if ("".equals(url))
			url = Constant.leftSplit;
		Application app = ac.getApplicationByUrl(url);
		if (app == null)
			throw new LettyException("no application found for " + url);
		hreq.setAttribute(Constant.ATTRIBUTE_APPLICATION, app);
		for (HandlerInterceptorAdapter intercepotr : app.getInterceptor()) {
			boolean result = intercepotr.preHandle(hreq, hres);
			if (!result)
				return;
		}
		LettyMapper mapper = ac.mapperRequest(hreq);
		ApplicationContext.getBean(mapper.getHandler()).handleRequest(hreq,
				hres, mapper);
		for (HandlerInterceptorAdapter intercepotr : app.getInterceptor()) {
			boolean result = intercepotr.afterHandle(hreq, hres);
			if (!result)
				return;
		}
	}

}
