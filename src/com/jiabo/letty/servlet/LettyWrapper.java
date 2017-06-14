package com.jiabo.letty.servlet;

import com.jiabo.letty.Application;

public abstract class LettyWrapper {
	protected Class<?> controller;
	protected Application application;
	protected String regUrl;

	public Class<?> getController() {
		return controller;
	}

	public void setController(Class<?> controller) {
		this.controller = controller;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public String getRegUrl() {
		return regUrl;
	}

	public void setRegUrl(String regUrl) {
		this.regUrl = regUrl;
	}

	public abstract Class<? extends LettyHandler> getHandler();

}
