package com.huatu.tiku.interview.entity.result;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/12 9:09
 * @Modefied By:
 */
public class ErrorResult implements BaseResult {
    private String message;
    private Integer code;
    private Object data;

    public ErrorResult() {
    }

    public static final ErrorResult create(Integer code, String message) {
        return new ErrorResult(code, message);
    }

    public static final ErrorResult create(Integer code, String message, Object data) {
        return new ErrorResult(code, message, data);
    }

    protected ErrorResult(Integer code, String message) {
        this.message = message;
        this.code = code;
    }

    protected ErrorResult(Integer code, String message, Object data) {
        this.message = message;
        this.code = code;
        this.data = data;
    }

    public Object getData() {
        return this.data;
    }

    public String getMessage() {
        return this.message;
    }

    public Integer getCode() {
        return this.code;
    }
}
