package com.huatu.tiku.essay.exception;

import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.ic.common.exception.HTRuntimeException;
import com.huatu.ic.common.util.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by duanxiangchao on 2019/7/24
 */
@ControllerAdvice
@Order(1)
@Slf4j
public class DefaultExceptionHandler {

    Logger LOG = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Object springValidationException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        FieldError fieldError = fieldErrors.get(0);
        printLog(LOG, fieldError.getField() + fieldError.getDefaultMessage());
        return ErrorResult.create(1000001,   fieldError.getDefaultMessage());
    }



    @ExceptionHandler(BizException.class)
    @ResponseBody
    public Object htException(BizException e) {
        return e.getErrorResult();
    }

    private void printLog(Logger LOG, String str){
        LogUtils.error(LOG, str);
    }

}
