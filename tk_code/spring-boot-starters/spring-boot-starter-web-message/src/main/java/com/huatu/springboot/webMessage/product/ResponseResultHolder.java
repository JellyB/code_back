package com.huatu.springboot.webMessage.product;

/**
 * Created by junli on 2018/4/10.
 */
public class ResponseResultHolder {
    private static final ThreadLocal<Object> RESULT = new ThreadLocal<Object>();
    public static void set(Object data) {
        RESULT.set(data);
    }

    public static Object get() {
        return RESULT.get();
    }

    public static void clear() {
        RESULT.remove();
    }
}
