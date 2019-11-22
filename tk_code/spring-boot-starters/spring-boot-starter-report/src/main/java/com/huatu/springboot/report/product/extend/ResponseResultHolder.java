package com.huatu.springboot.report.product.extend;

/**
 * @author hanchao
 * @date 2018/3/28 17:08
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
