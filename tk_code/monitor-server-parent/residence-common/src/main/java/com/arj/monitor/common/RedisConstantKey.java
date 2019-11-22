package com.arj.monitor.common;

/**
 * @author zhouwei
 * @Description: TODO
 * @create 2018-12-11 16:59:21
 **/
public class RedisConstantKey {
    /**
     * 异常机器key zset
     */
    public static final String EXCEPTION_SERVER_INFO = "exception_server_info";
    /**
     * 所有机器key list
     */
    public static final String ALL_SERVER_INFO = "all_normal_server_info";
    public static final String EXCEPTION_SERVER_COUNT = "exception:server:count:";


    public static final String TOKEN_USER_TYPE = "userType";
    public static final String TOKEN_USER_ID = "userId";
    public static final String TOKEN_TOKEN = "token";
    public static final String TOKEN_EXPIRE_TIME = "expireTime";

  public static String  getExceptionServerCountStringKey(long serverInfoId,long minute){
        return EXCEPTION_SERVER_COUNT+serverInfoId+":"+minute;
    }
}
