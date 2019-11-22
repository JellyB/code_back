package com.huatu.ztk.user.common;

import lombok.Data;

/**
 * 微信小程序相关常量
 * 
 * @author zhangchong
 *
 */
@Data
public class WeChatSession {

	private String openid;
	private String session_key;
	private String unionid;
	private Integer errcode;
	private String errmsg;

}
