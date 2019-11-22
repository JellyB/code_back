package com.huatu.tiku.interview.handler;


import com.huatu.tiku.interview.entity.result.Result;
import com.huatu.tiku.interview.exception.ReqException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * 2017-07-30 17:44
 */
@RestControllerAdvice
public class SomeExceptionHandler {



    //拦截自定义异常
	@ExceptionHandler(value = ReqException.class)
    public Result bizExceptionHandler(HttpServletRequest request, HttpServletResponse response, ReqException e) {
		System.out.println(e.getMessage());
		return Result.build(e.getCode(), e.getMessage());
    }
	

    
}
