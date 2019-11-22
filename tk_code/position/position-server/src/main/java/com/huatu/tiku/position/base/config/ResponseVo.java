package com.huatu.tiku.position.base.config;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * ResponseVo
 * 
 * @author Geek-S
 *
 */
@Data
@Builder
public class ResponseVo implements Serializable {

	private static final long serialVersionUID = 1484562282465216596L;

	/**
	 * 响应数据
	 */
	private Object data;

	/**
	 * 是否成功
	 */
	private Boolean success;

	/**
	 * 信息
	 */
	private String message;

	/**
	 * HttpStatus
	 */
	private Integer status;

	/**
	 * 返回数据
	 * 
	 * @param data
	 *            响应数据
	 * @return ResponseVo
	 */
	public static ResponseVo success(Object data) {
		return ResponseVo.builder().status(HttpStatus.OK.value()).success(true).data(data).build();
	}

	/**
	 * 返回数据
	 * 
	 * @param data
	 *            响应数据
	 * @return ResponseVo
	 */
	public static ResponseVo fail(String message) {
		return ResponseVo.builder().status(HttpStatus.OK.value()).success(false).message(message).build();
	}
}
