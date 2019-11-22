package com.huatu.ztk.commons.exception;

/**
 * 正常响应
 * Created by shaojieyue
 * Created time 2016-06-06 15:47
 */
public class SuccessResponse implements Result{
    private static final long serialVersionUID = 1L;
    private static final int SUCCESS_CODE=1000000;
    private Object data;

    public SuccessResponse() {
    }

    public SuccessResponse(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getCode() {
        return SUCCESS_CODE;
    }
}
