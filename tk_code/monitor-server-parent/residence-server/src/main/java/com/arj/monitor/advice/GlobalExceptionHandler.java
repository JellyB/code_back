package com.arj.monitor.advice;

import com.arj.monitor.common.CommonResult;
import com.arj.monitor.exception.BizException;
import com.arj.monitor.exception.ErrorResult;
import com.arj.monitor.exception.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

/**
 * @author zhouwei
 * @Description: ConstraintViolationException异常处理
 * @create 2018-10-18 下午12:20
 **/
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {


    @ResponseBody
    @ExceptionHandler(Exception.class)
    public Result handleApiConstraintViolationException(Exception ex) {
        String message = "";
        if (ex instanceof ConstraintViolationException) {
            ConstraintViolationException exception = (ConstraintViolationException) ex;
            Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
            for (ConstraintViolation<?> violation : violations) {
                message += violation.getMessage() + ", ";
            }
            return ErrorResult.create(CommonResult.INVALID_ARGUMENTS.getCode(), message);
        } else if (ex instanceof BizException) {
            log.info("业务异常可以在这里收集-->状态码：{},提示信息：{}",((BizException) ex).getErrorResult().getCode(),((BizException) ex).getErrorResult().getMessage());
            BizException exception = (BizException) ex;
            return exception.getErrorResult();
        }
       ex.printStackTrace();

        return ErrorResult.create(CommonResult.SERVICE_INTERNAL_ERROR.getCode(), CommonResult.SERVICE_INTERNAL_ERROR.getMessage());
    }

}
