package com.ht.base.common;

/**
 * 正常响应
 * Created by shaojieyue
 * Created time 2016-06-06 15:47
 */
public class SuccessResponse implements Result {
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

    @Override
    public int getCode() {
        return SUCCESS_CODE;
    }
}
