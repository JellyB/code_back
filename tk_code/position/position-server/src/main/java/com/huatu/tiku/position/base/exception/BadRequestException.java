package com.huatu.tiku.position.base.exception;

/**
 * 参数校验异常
 * 
 * @author Geek-S
 * 
 */
public class BadRequestException extends RuntimeException {

	private static final long serialVersionUID = 6204748494965851932L;

	public BadRequestException(String message) {
		super(message);
	}
}
