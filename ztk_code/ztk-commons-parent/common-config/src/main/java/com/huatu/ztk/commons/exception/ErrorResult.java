package com.huatu.ztk.commons.exception;

/**
 * 错误结果返回
 * Created by shaojieyue
 * Created time 2016-04-18 11:35
 */
public class ErrorResult implements Result {
    private static final long serialVersionUID = 1L;
    private String message;
    private int code;
    private Object data;

    public ErrorResult() {
    }

    /**
     * 创建一个新的错误对象
     * @param code
     * @param message
     * @return
     */
    public static final ErrorResult create(int code, String message){
        return new ErrorResult(code,message);
    }

    private ErrorResult(int code, String message) {
        this.message = message;
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }
}
