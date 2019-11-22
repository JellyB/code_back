package com.huatu.ztk.commons.exception;

/**
 * 业务异常类
 * Created by shaojieyue
 * Created time 2016-04-24 14:07
 */
public class BizException extends Exception {
    private static final long serialVersionUID = 1L;
    private ErrorResult errorResult;
    private String customMessage;//个性化信息

    //hessian序列化问题
    public BizException(){

    }

    public BizException(ErrorResult errorResult) {
        super(errorResult.getMessage());
        this.errorResult = errorResult;
    }

    public BizException(ErrorResult errorResult,String customMessage) {
        super(customMessage);
        this.errorResult = errorResult;
        this.customMessage = customMessage;
    }


    /**
     * 获取错误结果
     * @return
     */
    public ErrorResult getErrorResult(){
        return errorResult;
    }


    public String getCustomMessage() {
        return customMessage;
    }
}
