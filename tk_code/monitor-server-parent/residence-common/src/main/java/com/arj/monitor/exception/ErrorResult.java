package com.arj.monitor.exception;


/**
 * @author zhouwei
 * @Description: 错误结果返回
 * @create 2018-10-15 上午11:49
 **/
public class ErrorResult implements Result {
    private String message;
    private int code;
    private Object data;

    /**
     * 创建一个新的错误对象
     *
     * @param code
     * @param message
     * @return
     */
    public static final ErrorResult create(int code, String message) {
        return new ErrorResult(code, message);
    }

    public static final ErrorResult create(int code, String message, Object data) {
        return new ErrorResult(code, message, data);
    }

    public ErrorResult(int code, String message) {
        this.message = message;
        this.code = code;
    }

    public ErrorResult() {
    }

    public ErrorResult(int code, String message, Object data) {
        this.message = message;
        this.code = code;
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public int getCode() {
        return code;
    }
}
