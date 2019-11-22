package com.huatu.tiku.springboot.users.common;

/**
 * Created by shaojieyue
 * Created time 2016-05-31 11:18
 */
public class UserRedisSessionKeys {
    public static final String ssoId = "ssoId";//ssoId
    public static final String mobile = "mobile";
    public static final String nick = "nick";//用户昵称
    public static final String uname = "uname";//username具有唯一性和ucent做关联
    public static final String id = "id";
    public static final String subject = "subject";
    public static final String area = "area";
    public static final String email = "email";
    public static final String expireTime = "expireTime";
    public static final String oldToken = "oldToken";
    public static final String newDiveceLoginTime = "nloginTime";//新设备登录时间
    /**
     * @see {@link com.huatu.tiku.common.consts.CatgoryType}
     */
    public static final String catgory = "catgory";//知识点类目
    public static final String qcount = "qcount";//做题数量
    public static final String ucId = "ucId";//ucId


}
