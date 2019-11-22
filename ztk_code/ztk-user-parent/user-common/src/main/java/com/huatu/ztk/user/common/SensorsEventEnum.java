package com.huatu.ztk.user.common;

/**
 * 神策事件
 * @author zhangchong
 *
 */
public enum SensorsEventEnum {
	LOGIN_SUCCEED("HuaTuOnline_app_HuaTuOnline_LoginSucceed", "登录成功");

	private String code;
	private String desc;

	SensorsEventEnum(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public String getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
	
}
