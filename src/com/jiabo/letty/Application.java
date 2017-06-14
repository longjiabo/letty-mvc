package com.jiabo.letty;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.jiabo.letty.mvc.HandlerInterceptorAdapter;
import com.jiabo.letty.mvc.ViewResolver;
import com.jiabo.letty.servlet.LettyExceptionHandler;
import com.jiabo.letty.servlet.LettyMapper;

/**
 * the class for the detail application to implement.
 * 
 * @author jialong
 *
 */
public abstract class Application {

	protected Properties props;
	private ViewResolver viewResolver;
	private List<HandlerInterceptorAdapter> interceptor;
	private List<LettyExceptionHandler> exceptionHandler;
	private List<LettyMapper> mappers;

	/**
	 * get application name
	 * 
	 * @return name
	 */
	public abstract String getAppName();

	/**
	 * application init
	 */
	public abstract void start();

	public List<HandlerInterceptorAdapter> getInterceptor() {
		return interceptor;
	}

	public void setInterceptor(List<HandlerInterceptorAdapter> interceptor) {
		this.interceptor = interceptor;
	}

	public ViewResolver getViewResolver() {
		return viewResolver;
	}

	public void setViewResolver(ViewResolver viewResolver) {
		this.viewResolver = viewResolver;
	}

	public List<LettyExceptionHandler> getExceptionHandler() {
		return exceptionHandler;
	}

	public void setExceptionHandler(List<LettyExceptionHandler> exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

	public void registerExceptionHandler(LettyExceptionHandler handler) {
		if (exceptionHandler == null) {
			exceptionHandler = new ArrayList<LettyExceptionHandler>();
		}
		exceptionHandler.add(handler);
	}

	public void registerInterceptor(HandlerInterceptorAdapter adapter) {
		if (interceptor == null) {
			interceptor = new ArrayList<HandlerInterceptorAdapter>();
		}
		interceptor.add(adapter);
	}

	public List<LettyMapper> getMappers() {
		return mappers;
	}

	public void setMappers(List<LettyMapper> mappers) {
		this.mappers = mappers;
	}

}
