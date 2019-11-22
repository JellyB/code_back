package com.arj.monitor.common;


import lombok.Data;

@Data
public class BaseResult {

	private int code;
	private String message;
	private Object data;

	public static final BaseResult create(String message) {
		if ("".equals(message)) {
			return new BaseResult(1000000, "请求成功", null);
		} else {
			return new BaseResult(500, message, null);
		}
	}

	public static final BaseResult create(int code) {
		return new BaseResult(code, null, null);
	}

	public static final BaseResult create(int code, String message) {
		return new BaseResult(code, message, null);
	}

	public static final BaseResult create(int code, String message, Object data) {
		return new BaseResult(code, message, data);
	}

	protected BaseResult(int code, String message, Object data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public BaseResult setCode(int code) {
		this.code = code;
		return this;
	}

	public String getMessage() {
		return message;
	}

	public BaseResult setMessage(String message) {
		this.message = message;
		return this;
	}

	public Object getData() {
		return data;
	}

	public BaseResult setData(Object data) {
		this.data = data;
		return this;
	}

}
