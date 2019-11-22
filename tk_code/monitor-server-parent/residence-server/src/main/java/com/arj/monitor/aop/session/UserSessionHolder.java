package com.arj.monitor.aop.session;


import com.arj.monitor.common.CommonConfig;
import com.arj.monitor.entity.UserSession;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author zhouwei
 * @Description: 本地维护
 * @create 2018-10-15 上午12:15
 **/
@Slf4j
public class UserSessionHolder {
    /**
     * key 是token
     *  */
  public static final Cache<String,UserSession> userSessionCache= CacheBuilder.newBuilder()
            //最大缓存数目
            .maximumSize(2000)
            //缓存USER_TOKEN_TIME_OUT秒后过期
            .expireAfterAccess(CommonConfig.USER_TOKEN_TIME_OUT, TimeUnit.SECONDS)
            .build();
}
