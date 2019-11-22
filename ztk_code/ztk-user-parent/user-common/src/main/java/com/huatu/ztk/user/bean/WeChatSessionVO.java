package com.huatu.ztk.user.bean;

import lombok.Data;

/**
 * 微信小程序授权返回vo
 * 
 * @author zhangchong
 *
 */
@Data
public class WeChatSessionVO {

	private String openid;
	private String unionid;
	private Integer errcode;
	private String errmsg;

}
