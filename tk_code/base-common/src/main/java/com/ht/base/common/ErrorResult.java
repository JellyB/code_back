package com.ht.base.common;

/**
 * 错误结果返回
 * Created by shaojieyue
 * Created time 2016-04-18 11:35
 */
public class ErrorResult implements Result {
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

    public static final ErrorResult create(int code,String message,Object data){
        return new ErrorResult(code,message,data);
    }

    protected ErrorResult(int code, String message) {
        this.message = message;
        this.code = code;
    }

    public ErrorResult(int code,String message,  Object data) {
        this.message = message;
        this.code = code;
        this.data = data;
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

    @Override
    public int getCode() {
        return code;
    }
}
