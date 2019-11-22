package com.arj.monitor.common;

/**
 * @author zhouwei
 * @Description: TODO
 * @create 2018-10-19 上午11:51
 **/
public class CommonConfig {
    //token 过期时间 单位：s 7776000=3个月,此处一定要定义为long，不然×1000存在越界
    public static final long USER_TOKEN_TIME_OUT = 30;

   // public static final String WEBHOOK_TOKEN = "https://oapi.dingtalk.com/robot/send?access_token=04be97cf7cc98704ea635b3e7e199d7a9b89aa408a64ab51cd1c8dc1dfb2b5db";
    public static final String WEBHOOK_TOKEN = "https://oapi.dingtalk.com/robot/send?access_token=29bfd5d194505100685ab87fc805e94da860141f42f3db45e925a15b1139c2a4";


}
