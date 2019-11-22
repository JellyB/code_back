package com.ht.base.exception;

import com.ht.base.common.ErrorResult;

import java.io.Serializable;


/**
 * @author jbzm
 * @date Create on 2018/3/2 12:03
 */
public class MyException extends RuntimeException implements Serializable {

    private ErrorResult errorResult;
    private String customMessage;//个性化信息
    //hessian序列化问题
    public MyException() {

    }

    public MyException(ErrorResult errorResult) {
        super(errorResult.getMessage());
        this.errorResult = errorResult;
    }

    public MyException(ErrorResult errorResult, String customMessage) {
        super(customMessage);
        this.errorResult = errorResult;
        this.customMessage = customMessage;
    }


    /**
     * 获取错误结果
     *
     * @return
     */
    public ErrorResult getErrorResult() {
        return errorResult;
    }


    public String getCustomMessage() {
        return customMessage;
    }
}

