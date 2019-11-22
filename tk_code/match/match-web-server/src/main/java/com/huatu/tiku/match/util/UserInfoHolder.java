package com.huatu.tiku.match.util;

/**
 * 
 * @author zhangchong
 * 用户id存储
 */
public class UserInfoHolder {

	private static final ThreadLocal<Integer> CURRENT_USER_ID = new ThreadLocal<>();

	public static void set(Integer userId) {
		CURRENT_USER_ID.set(userId);
	}

	public static Integer get() {
		return CURRENT_USER_ID.get();
	}

	public static void clear() {
		CURRENT_USER_ID.remove();
	}

}
