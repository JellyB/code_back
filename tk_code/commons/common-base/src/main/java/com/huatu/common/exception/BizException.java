package com.huatu.common.exception;

import com.huatu.common.ErrorResult;
import com.huatu.common.ISystemStatusEnum;

/**
 * 业务异常类
 * Created by shaojieyue
 * Created time 2016-04-24 14:07
 */
public class BizException extends RuntimeException {
    private ErrorResult errorResult;
    private String customMessage;//个性化信息

    public BizException(ErrorResult errorResult) {
        super(errorResult.getMessage());
        this.errorResult = errorResult;
    }

    public BizException(ErrorResult errorResult,String customMessage) {
        super(customMessage);
        this.errorResult = errorResult;
        this.customMessage = customMessage;
    }

    public BizException(ISystemStatusEnum systemStatusEnum) {
        super(systemStatusEnum.getMessage());
        ErrorResult errorResult = ErrorResult.create(systemStatusEnum.getCode(), systemStatusEnum.getMessage());
        this.errorResult = errorResult;
        this.customMessage = systemStatusEnum.getMessage();
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
