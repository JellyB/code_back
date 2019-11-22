package com.huatu.tiku.position.base.config;

import com.huatu.tiku.position.base.exception.BadRequestException;
import com.huatu.tiku.position.base.exception.NoAuthException;
import com.huatu.tiku.position.base.exception.NoLoginException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

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
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ResponseVo> methodArgumentTypeMismatchException() {
		return ResponseEntity.ok(ResponseVo.builder().message("参数格式错误").status(HttpStatus.BAD_REQUEST.value()).build());
	}

//	/**
//	 * Handle 403
//	 */
//	@ExceptionHandler(AccessDeniedException.class)
//	public ResponseEntity<ResponseVo> accessDeniedException() {
//		return ResponseEntity.ok(ResponseVo.builder().message("无权访问该资源").status(HttpStatus.FORBIDDEN.value()).build());
//	}

	/**
	 * Handle 404
	 */
	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ResponseVo> accesasDeniedException() {
		return ResponseEntity.ok(ResponseVo.builder().message("资源不存在").status(HttpStatus.NOT_FOUND.value()).build());
	}

	/**
	 * Handle 405
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ResponseVo> httpRequestMethodNotSupportedException() {
		return ResponseEntity
				.ok(ResponseVo.builder().message("不支持的请求类型").status(HttpStatus.METHOD_NOT_ALLOWED.value()).build());
	}

	/**
	 * Handle 500
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

	/**
	 * 未授权异常
	 */
	@ExceptionHandler(NoAuthException.class)
	public ResponseEntity<ResponseVo> NoAuthException(Exception e) {
//		log.error("NoAuthException", e);
		return ResponseEntity.ok(ResponseVo.builder().success(false).message(e.getMessage()).status(1000).build());
	}

	/**
	 * 未登录异常
	 */
	@ExceptionHandler(NoLoginException.class)
	public ResponseEntity<ResponseVo> NoLoginException(Exception e) {
//		log.error("NoAuthException", e);
		return ResponseEntity.ok(ResponseVo.builder().success(false).message(e.getMessage()).status(2000).build());
	}

}
