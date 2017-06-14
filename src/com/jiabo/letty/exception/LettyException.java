package com.jiabo.letty.exception;

/**
 * framework exception
 * @author jialong
 *
 */
public class LettyException extends RuntimeException {


	private static final long serialVersionUID = 1532974881955810842L;

	public LettyException(String msg) {
		super(msg);
	}

	public LettyException(String msg, Throwable e) {
		super(msg, e);
	}
}
