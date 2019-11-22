package com.huatu.tiku.teacher.service;

/**
 * 微信相关接口
 * 
 * @author zhangchong
 *
 */
public interface WeChatService {

	/**
	 * 获取accessToken
	 * 
	 * @return
	 */
	String getAccessToken();
	
	/**
	 * 获取小程序二维码地址
	 * @param scene 参数拼接字符串
	 * @return
	 */
	String getQrCode(String scene);
}
