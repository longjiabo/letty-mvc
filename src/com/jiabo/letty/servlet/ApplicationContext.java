package com.jiabo.letty.servlet;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiabo.letty.Application;
import com.jiabo.letty.exception.LettyException;
import com.jiabo.letty.mvc.HandlerInterceptorAdapter;
import com.jiabo.letty.mvc.MVCHandler;
import com.jiabo.letty.mvc.ViewResolver;
import com.jiabo.letty.mvc.annoation.Autowired;
import com.jiabo.letty.mvc.annoation.Controller;
import com.jiabo.letty.mvc.annoation.RequestMapping;
import com.jiabo.letty.rest.RestHandler;
import com.jiabo.letty.rest.annoation.DELETE;
import com.jiabo.letty.rest.annoation.GET;
import com.jiabo.letty.rest.annoation.PATCH;
import com.jiabo.letty.rest.annoation.PATH;
import com.jiabo.letty.rest.annoation.POST;
import com.jiabo.letty.rest.annoation.PUT;
import com.jiabo.letty.rest.annoation.REST;
import com.jiabo.letty.util.ClassUtil;
import com.jiabo.letty.util.Constant;
import com.jiabo.letty.util.HTTPUtil;
import com.jiabo.letty.util.LettyUtil;
import com.jiabo.letty.util.StringUtil;

/**
 * the applicationContext of all the applications.
 * 
 * @author jialong
 * 
 */
public class ApplicationContext {

	private final Logger log = LoggerFactory
			.getLogger(ApplicationContext.class);
	private ServletConfig servletConfig;
	private Map<Class<?>, Object> beanFactory = new HashMap<Class<?>, Object>();
	private List<Application> runningApplications = new ArrayList<Application>();

	public LettyMapper mapperRequest(HttpServletRequest hreq) {
		String url = HTTPUtil.getURL(hreq);
		if ("".equals(url))
			url = Constant.leftSplit;
		String httpMethod = hreq.getMethod();
		Application app = LettyUtil.getApplication(hreq);
		url = HTTPUtil.removeAppPrefix(url, app.getAppName());
		LettyMapper mapper = mapCommon(url, app, httpMethod);
		if (mapper == null) {
			mapper = mapReg(url, app, httpMethod);
		}
		if (mapper == null)
			throw new LettyException("the url " + url + " not matched");
		return mapper;
	}

	private LettyMapper mapReg(String url, Application app, String method) {
		for (LettyMapper m : app.getMappers()) {
			if (m.getReg()) {
				if (url.matches(m.getUrl()))
					if (m.getMethod() == null
							|| method.equalsIgnoreCase(m.getHTTPMethod()))
						return m;
			}
		}
		return null;

	}

	private LettyMapper mapCommon(String url, Application app, String method) {
		for (LettyMapper m : app.getMappers()) {
			if (!m.getReg()) {
				if (url.equalsIgnoreCase(m.getUrl())) {
					if (m.getHTTPMethod() == null
							|| method.equalsIgnoreCase(m.getHTTPMethod()))
						return m;
				}
			}
		}
		return null;
	}

	/**
	 * mapping the app with url
	 * 
	 * @param url
	 * @return
	 */
	public Application getApplicationByUrl(String url) {
		Application noAppNameApp = null;
		for (Application app : runningApplications) {
			if (StringUtil.isEmpty(app.getAppName())) {
				noAppNameApp = app;
			} else {
				String appName = app.getAppName();
				if (url.startsWith(Constant.leftSplit + appName))
					return app;
			}
		}
		return noAppNameApp;
	}

	public ApplicationContext(ServletConfig servletConfig) {
		this.servletConfig = servletConfig;
	}

	/**
	 * ApplicationContext init
	 * 
	 */
	public void init() {
		log.info("initRunningApplication...");
		scannerApplications();
	}

	private void scannerApplications() {
		String packages = servletConfig.getInitParameter("scanPackage");
		if (packages == null || "".equals(packages.trim()))
			throw new LettyException(
					"no application found...you need add scanPackage params in the servlet");
		for (String pack : packages.split(Constant.split)) {
			initapp(pack);
		}
	}

	private void initInterceptor(Application app, Set<Class<?>> set) {
		List<HandlerInterceptorAdapter> list = app.getInterceptor();
		if (list == null) {
			list = new ArrayList<HandlerInterceptorAdapter>();
			app.setInterceptor(list);
		}
		for (Class<?> c : set) {
			if (HandlerInterceptorAdapter.class.isAssignableFrom(c)) {
				list.add((HandlerInterceptorAdapter) getBean(c));
			}
		}

	}

	private void initapp(String pack) {
		log.info("get the package:{}", pack);
		Set<Class<?>> set = ClassUtil.getClasses(pack);
		log.info("scan the application...");
		Application app = getApplication(set);
		if (app == null) {
			log.error("no application find in {}", pack);
			return;
		}
		runningApplications.add(app);
		log.info("scan the viewResolver...");
		ViewResolver vr = getViewResolver(set);
		if (vr == null) {
			log.warn("no viewResolver find in {}", pack);
		}
		app.setViewResolver(vr);
		log.info("scan the interceptor...");
		initInterceptor(app, set);
		log.info("scan the url mapping...");
		initURLMapping(app, set);
		log.info("call the application start...");
		app.start();
	}

	private ViewResolver getViewResolver(Set<Class<?>> set) {
		for (Class<?> c : set) {
			if (ViewResolver.class.isAssignableFrom(c)) {
				ViewResolver vr = (ViewResolver) getBean(c);
				vr.init(servletConfig.getServletContext());
				return vr;
			}
		}
		return null;
	}

	private Application getApplication(Set<Class<?>> set) {
		Application app = null;
		for (Class<?> c : set) {
			if (Application.class.isAssignableFrom(c)) {
				log.info("init the application");
				app = (Application) getBean(c);
			}
		}
		return app;
	}

	/**
	 * getBean from beanContainer.
	 * 
	 * @param clazz
	 * @return
	 */
	public static <T> T getBean(Class<T> clazz) {
		return LettyServlet.getApplicationContext().getBeanByClass(clazz);
	}

	/**
	 * use the exist obj replace the beanfactory
	 * 
	 * @param obj
	 * @param clazz
	 */
	public static <T> void proxyBean(T obj, Class<T> clazz) {
		LettyServlet.getApplicationContext().updateBean(obj, clazz);
	}

	@SuppressWarnings("unchecked")
	public <T> T getBeanByClass(Class<T> clazz) {
		Object obj = beanFactory.get(clazz);
		if (obj == null) {
			obj = initBean(clazz);
		}
		return (T) obj;
	}

	public <T> void updateBean(T obj, Class<T> clazz) {
		beanFactory.put(clazz, obj);
	}

	/**
	 * init bean
	 * 
	 * @param clazz
	 * @return
	 */
	private <T> T initBean(Class<T> clazz) {
		T obj = null;
		try {
			obj = clazz.newInstance();
			beanFactory.put(clazz, obj);
			initField(obj);
		} catch (InstantiationException | IllegalAccessException e) {
			log.error("can not init the bean " + clazz, e);
		}
		return obj;
	}

	/**
	 * init the fileds of the given bean
	 * 
	 * @param obj
	 */
	private void initField(Object obj) {
		for (Field field : obj.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(Autowired.class)) {
				field.setAccessible(true);
				try {
					field.set(obj, getBeanByClass(field.getType()));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					log.error("can not init the field " + field.getName(), e);
				}
			}
		}
	}

	private void initURLMapping(Application app, Set<Class<?>> set) {
		initController(app, set);
		initRest(app, set);
	}

	private void initRest(Application app, Set<Class<?>> set) {
		for (Class<?> c : set) {
			if (!c.isAnnotationPresent(REST.class))
				continue;
			String url = c.getAnnotation(REST.class).value();
			if (StringUtil.isEmpty(url))
				continue;
			url = formatUrl(url);
			for (Method m : c.getMethods()) {
				String httpMethod = getHttpMethod(m);
				if (httpMethod == null)
					continue;
				String path = m.isAnnotationPresent(PATH.class) ? m
						.getAnnotation(PATH.class).value() : "";
				path = url + formatUrl(path);
				LettyMapper lm = new LettyMapper();
				lm.setHTTPMethod(httpMethod);
				lm.setMethod(m);
				lm.setPathParamNames(parseVars(path));
				lm.setApplication(app);
				lm.setHandler(RestHandler.class);
				lm.setMethodClass(c);
				lm.setUrl(path);
				Pattern p = Pattern.compile(Constant.Path_reg);
				Matcher matcher = p.matcher(path);
				if (matcher.find()) {
					lm.setReg(true);
					lm.setUrl(path.replaceAll(Constant.Path_reg,
							Constant.Path_replace_reg));
				}
				List<LettyMapper> list = app.getMappers();
				if (list == null) {
					list = new ArrayList<LettyMapper>();
					app.setMappers(list);
				}
				System.out.println(lm.getUrl());
				list.add(lm);
			}
		}
	}

	private String formatUrl(String url) {
		if (url == null)
			return null;
		if (url.isEmpty())
			return url;
		if (!url.startsWith(Constant.leftSplit))
			url = Constant.leftSplit + url;
		if (url.endsWith(Constant.leftSplit))
			url = url.substring(0, url.length() - 1);
		return url;
	}

	private List<String> parseVars(String path) {
		List<String> vars = new ArrayList<String>();
		Pattern p = Pattern.compile(Constant.Path_reg);
		Matcher matcher = p.matcher(path);
		while (matcher.find()) {
			String pathVar = matcher.group(1);
			vars.add(pathVar);
		}
		return vars;
	}

	private String getHttpMethod(Method m) {
		if (m.isAnnotationPresent(POST.class))
			return Constant.POST;
		if (m.isAnnotationPresent(PUT.class))
			return Constant.PUT;
		if (m.isAnnotationPresent(GET.class))
			return Constant.GET;
		if (m.isAnnotationPresent(PATCH.class))
			return Constant.PATCH;
		if (m.isAnnotationPresent(DELETE.class))
			return Constant.DELETE;
		return null;
	}

	private void initController(Application app, Set<Class<?>> set) {
		for (Class<?> c : set) {
			if (!c.isAnnotationPresent(Controller.class))
				continue;
			Method[] methods = c.getMethods();
			for (Method m : methods) {
				if (!m.isAnnotationPresent(RequestMapping.class))
					continue;
				String url = m.getAnnotation(RequestMapping.class).value();
				LettyMapper lm = new LettyMapper();
				lm.setMethod(m);
				lm.setPathParamNames(parseVars(url));
				lm.setApplication(app);
				lm.setHandler(MVCHandler.class);
				lm.setMethodClass(c);
				lm.setUrl(url);
				if (url.matches(Constant.Path_reg)) {
					lm.setReg(true);
					lm.setUrl(url.replaceAll(Constant.Path_reg,
							Constant.Path_replace_reg));
				}
				List<LettyMapper> list = app.getMappers();
				if (list == null) {
					list = new ArrayList<LettyMapper>();
					app.setMappers(list);
				}
				list.add(lm);
			}
		}
	}
}
