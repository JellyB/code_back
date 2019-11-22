package com.huatu.tiku.springboot.users.support;

import com.huatu.tiku.common.bean.user.UserSession;

/**
 * 谁使用谁清空，starter-users不会去清空该属性，只会尝试从该属性获取，减少redis访问次数
 * @author hanchao
 * @date 2018/1/11 17:59
 */
public class UserSessionHolder {
    private static final ThreadLocal<UserSession> CURRENT_USER = new ThreadLocal<UserSession>();
    public static void set(UserSession agent) {
        CURRENT_USER.set(agent);
    }

    public static UserSession get() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
