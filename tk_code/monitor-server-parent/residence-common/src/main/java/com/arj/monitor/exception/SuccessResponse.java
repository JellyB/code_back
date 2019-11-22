package com.arj.monitor.exception;


/**
 * @author zhouwei
 * @Description: 正常相应
 * @create 2018-10-15 上午12:08
 **/
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
