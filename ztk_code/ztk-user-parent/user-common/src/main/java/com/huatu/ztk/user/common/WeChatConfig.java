package com.huatu.ztk.user.common;

/**
 * 微信小程序相关常量
 * @author zhangchong
 *
 */
public class WeChatConfig {
	
    /**
     * 根据code获取OpenID以及session_key
     */
    public static final String AUTH_RUL = "https://api.weixin.qq.com/sns/jscode2session?appid=wxea712b1f7f859bdd&secret=297a03646773c31fa9e8ecefa25966f0&grant_type=authorization_code&js_code=${code}";
    
    

    

}
