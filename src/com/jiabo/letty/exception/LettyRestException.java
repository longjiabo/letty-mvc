package com.jiabo.letty.exception;

public class LettyRestException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2517391634109894927L;

	public LettyRestException(String msg, Integer httpCode) {
		super(msg);
		this.httpCode = httpCode;
	}

	public LettyRestException(String msg, Integer httpCode, Throwable e) {
		super(msg, e);
		this.httpCode = httpCode;
	}

	public Integer getHttpCode() {
		return httpCode;
	}

	public void setHttpCode(Integer httpCode) {
		this.httpCode = httpCode;
	}

	private Integer httpCode;

}
