package com.huatu.tiku.schedule.base.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.huatu.tiku.schedule.base.exception.BadRequestException;

import lombok.extern.slf4j.Slf4j;

/**
 * 全局异常处理
 * 
 * @author Geek-S
 *
 */
@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {

	/**
	 * Handle 400
	 * 
	 * @param exception
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ResponseVo> methodArgumentTypeMismatchException() {
		return ResponseEntity.ok(ResponseVo.builder().message("参数格式错误").status(HttpStatus.BAD_REQUEST.value()).build());
	}

	/**
	 * Handle 403
	 * 
	 * @param exception
	 */
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ResponseVo> accessDeniedException() {
		return ResponseEntity.ok(ResponseVo.builder().message("无权访问该资源").status(HttpStatus.FORBIDDEN.value()).build());
	}

	/**
	 * Handle 404
	 * 
	 * @param exception
	 */
	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ResponseVo> accesasDeniedException() {
		return ResponseEntity.ok(ResponseVo.builder().message("资源不存在").status(HttpStatus.NOT_FOUND.value()).build());
	}

	/**
	 * Handle 405
	 * 
	 * @param exception
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ResponseVo> httpRequestMethodNotSupportedException() {
		return ResponseEntity
				.ok(ResponseVo.builder().message("不支持的请求类型").status(HttpStatus.METHOD_NOT_ALLOWED.value()).build());
	}

	/**
	 * Handle 500
	 * 
	 * @param exception
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ResponseVo> exception(Exception e) {
		log.error("Uncaught exception", e);

		Integer status = null;

		if (e instanceof BadRequestException) {
			status = HttpStatus.OK.value();
		} else {
			HttpStatus.INTERNAL_SERVER_ERROR.value();
		}

		return ResponseEntity.ok(ResponseVo.builder().success(false).message(e.getMessage()).status(status).build());
	}

}
