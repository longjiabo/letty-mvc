package com.jiabo.letty.servlet;

import java.lang.reflect.Method;
import java.util.List;

import com.jiabo.letty.Application;

public class LettyMapper {

	private Method method;
	private String url;
	private List<String> pathParamNames;
	private Class<? extends LettyHandler> handler;
	private boolean reg;
	private String HTTPMethod;
	private Application application;
	private Class<?> methodClass;

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public boolean getReg() {
		return reg;
	}

	public void setReg(boolean reg) {
		this.reg = reg;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getHTTPMethod() {
		return HTTPMethod;
	}

	public void setHTTPMethod(String hTTPMethod) {
		HTTPMethod = hTTPMethod;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public List<String> getPathParamNames() {
		return pathParamNames;
	}

	public void setPathParamNames(List<String> pathParamNames) {
		this.pathParamNames = pathParamNames;
	}

	public Class<? extends LettyHandler> getHandler() {
		return handler;
	}

	public void setHandler(Class<? extends LettyHandler> handler) {
		this.handler = handler;
	}

	public Class<?> getMethodClass() {
		return methodClass;
	}

	public void setMethodClass(Class<?> methodClass) {
		this.methodClass = methodClass;
	}

}
