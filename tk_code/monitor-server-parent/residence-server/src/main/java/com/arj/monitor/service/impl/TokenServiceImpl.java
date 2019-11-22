package com.arj.monitor.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.arj.monitor.common.CommonConfig;
import com.arj.monitor.common.CommonResult;
import com.arj.monitor.common.RedisConstantKey;
import com.arj.monitor.entity.User;
import com.arj.monitor.entity.UserSession;
import com.arj.monitor.exception.BizException;
import com.arj.monitor.service.TokenService;
import com.arj.monitor.util.TokenUtil;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zhouwei
 * @create 2018-10-17 下午12:41
 **/
@Slf4j
@Service
public class TokenServiceImpl implements TokenService{

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 判断用户登陆状态是否过期
     * @param expireTime
     * @return
     */
    public boolean isExpire(long expireTime){
        return expireTime<System.currentTimeMillis();
    }

    @Override
    public UserSession assertSession(UserSession userSession) throws BizException {
        if(userSession == null || isExpire(userSession.getExpireTime())){
            throw new BizException(CommonResult.SESSION_EXPIRE);
        }
        return userSession;
    }
    @Override
    public UserSession assertAdminSession(UserSession userSession) throws BizException {
        if(userSession == null || isExpire(userSession.getExpireTime()) ||userSession.getUserType()!=1){
            throw new BizException(CommonResult.SESSION_EXPIRE);
        }
        return userSession;
    }

    /**
     * 用户登录新的userSession信息
     * 存入cache和redis中
     * @param user
     * @return
     */
    @Override
    public UserSession newUserSession(User user) {
        UserSession userSession = new UserSession();
        userSession.setUserId(user.getId());
        String token = TokenUtil.generateToken();
        userSession.setToken(token);
        userSession.setUserType(0);
        userSession.setExpireTime(System.currentTimeMillis() + (CommonConfig.USER_TOKEN_TIME_OUT * 1000));
        putUserSession2RedisIfAbsent(userSession);
        return userSession;
    }


    /**
     * 用户session保存到redis中
     * @param userSession
     */
    @Override
    public void putUserSession2RedisIfAbsent(UserSession userSession){
        HashMap<String, String> sessionInfo = Maps.newHashMap();
        try {
            final HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
            if(redisTemplate.hasKey(userSession.getToken())){
                updateSessionExpireTime(userSession.getToken());
            }else{
                sessionInfo.put(RedisConstantKey.TOKEN_USER_ID, String.valueOf(userSession.getUserId().intValue()));
                sessionInfo.put(RedisConstantKey.TOKEN_TOKEN, userSession.getToken());
                sessionInfo.put(RedisConstantKey.TOKEN_USER_TYPE, String.valueOf(userSession.getUserType()));
                sessionInfo.put(RedisConstantKey.TOKEN_EXPIRE_TIME, String.valueOf(userSession.getExpireTime().longValue()));
                hashOperations.putAll(userSession.getToken(), sessionInfo);
            }
        } catch (Exception e) {
            log.error("ex,sessionInfo={}", JSONObject.toJSONString(sessionInfo));
        }
        redisTemplate.expire(userSession.getToken(), CommonConfig.USER_TOKEN_TIME_OUT, TimeUnit.SECONDS);
    }


    /**
     * 从redis中获取用户session信息
     * @param token
     * @return
     */
    @Override
    public UserSession getUserSessionFromRedis(String token) {
        if(StringUtils.isBlank(token)){
            return null;
        }
        HashOperations hashOperations = redisTemplate.opsForHash();
        Map<String,String> serializeSession = hashOperations.entries(token);
        if(MapUtils.isNotEmpty(serializeSession)){

            String userId = serializeSession.get(RedisConstantKey.TOKEN_USER_ID);
            String userToken = serializeSession.get(RedisConstantKey.TOKEN_TOKEN);
            String expireTime = serializeSession.get(RedisConstantKey.TOKEN_EXPIRE_TIME);
            String userType = serializeSession.get(RedisConstantKey.TOKEN_USER_TYPE);

            UserSession userSession = new UserSession();
            userSession.setToken(userToken);
            userType =  StringUtils.isBlank(userType) ? "0" : userType;
            userSession.setUserType(Integer.parseInt(userType));
            userSession.setUserId(Long.valueOf(userId));
            userSession.setExpireTime(StringUtils.isEmpty(expireTime) ? -1:Long.valueOf(expireTime));
            return userSession;
        }
        return null;
    }

    /**
     * 重新更新redis缓存时间
     * @param token
     */
    @Override
    public void updateSessionExpireTime(String token){
        redisTemplate.expire(token, CommonConfig.USER_TOKEN_TIME_OUT, TimeUnit.SECONDS);
    }
}
