package com.huatu.tiku.course.common;

/**
 * 活动返回状态
 * 
 * @author zhangchong
 *
 */
public enum ActivityStatusEnum {
	SUCCESS(1, "签到成功"),

	SIGNED(0, "已经签到"),

	ERROR(-1, "签到异常"),

	END(2, "活动结束"),
	
	UNDO(3, "未签到");

	private final Integer code;
	private final String des;

	private ActivityStatusEnum(int code, String des) {
		this.code = code;
		this.des = des;
	}

	public Integer getCode() {
		return code;
	}

	public String getDes() {
		return des;
	}

	public static ActivityStatusEnum create(int code) {
		for (ActivityStatusEnum status : values()) {
			if (status.getCode() == code) {
				return status;
			}
		}
		return null;
	}

}
