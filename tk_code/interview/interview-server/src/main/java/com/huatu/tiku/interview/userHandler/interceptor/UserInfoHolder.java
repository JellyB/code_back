package com.huatu.tiku.interview.userHandler.interceptor;

/**
 * 本地线程 根据用户ID 存储用户信息
 * Created by junli on 2018/4/11.
 */
public class UserInfoHolder {

    private static final ThreadLocal<Long> USER_INFO = new ThreadLocal<>();

    public static void set(Long data) {
        USER_INFO.set(data);
    }

    public static long get() {
        return USER_INFO.get();
    }

    public static void clear() {
        USER_INFO.remove();
    }

}
