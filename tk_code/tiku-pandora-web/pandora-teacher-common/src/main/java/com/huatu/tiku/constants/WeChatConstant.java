package com.huatu.tiku.constants;

/**
 * 微信常量
 * 
 * @author zhangchong
 *
 */
public class WeChatConstant {

	public final static String APPID = "wxea712b1f7f859bdd";
	public final static String SECRET = "297a03646773c31fa9e8ecefa25966f0";
	// 获取accessToken地址
	public final static String GETACCESSTOKENURL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
			+ APPID + "&secret=" + SECRET;

	// 获取小程序码地址
	public final static String GETWXACODEUNLIMITURL = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=${ACCESS_TOKEN}";

}
