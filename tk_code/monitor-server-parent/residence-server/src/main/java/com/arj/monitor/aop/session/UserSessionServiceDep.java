package com.arj.monitor.aop.session;

/**
 * @author zhouwei
 * @Description: UserSessionServiceDep
 * @create 2018-10-15 上午11:39
 **/
@Deprecated
public class UserSessionServiceDep {
//
//    @Autowired
//    private SessionRedisTemplate sessionRedisTemplate;
//
//
//    /**
//     * 查询session key 对应的value
//     * @param token token
//     * @param key session 属性
//     * @return
//     */
//    public final String getSessionValue(String token,String key){
//        String value = null;
//        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(key)) {
//            return value;
//        }
//        value = sessionRedisTemplate.hget(token, key);
//        return value;
//    }
//
//
//    /**
//     * 判断用户登陆状态是否过期
//     * @param expireTime
//     * @return
//     */
//    public boolean isExpire(long expireTime){
//        return expireTime<System.currentTimeMillis();
//    }
//
//    /**
//     * 获取完整的userSession
//     * @param token
//     * @return
//     */
//    public UserSessionDep getUserSession(String token){
//        if(StringUtils.isBlank(token)){
//            return null;
//        }
//        Map<String,String> serializeSession = sessionRedisTemplate.hgetAll(token);
//        if(MapUtils.isNotEmpty(serializeSession)){
//
//            String id = serializeSession.get(UserRedisSessionKeys.ID);
//            String expireTime = serializeSession.get(UserRedisSessionKeys.EXPIRE_TIME);
//
//            UserSessionDep userSessionDep = UserSessionDep.builder()
//                    .token(token)
//                    .id(StringUtils.isEmpty(id) ? -1:Long.valueOf(id))
//                    .expireTime(StringUtils.isEmpty(expireTime) ? -1:Long.valueOf(expireTime))
//                    .telephone(serializeSession.get(UserRedisSessionKeys.TELEPHONE))
//                    .nickname(serializeSession.get(UserRedisSessionKeys.NICKNAME))
//                    .username(serializeSession.get(UserRedisSessionKeys.USERNAME))
//                    .oldToken(serializeSession.get(UserRedisSessionKeys.OLD_TOKEN))
//                    .newDeviceLoginTime(serializeSession.get(UserRedisSessionKeys.NEW_DEVICE_LOGIN_TIME)).build();
//            return userSessionDep;
//        }
//        return null;
//    }
//
//    /**
//     * 断定session是否有效
//     * @param userSessionDep
//     * @throws BizException
//     */
//    public UserSessionDep assertSession(UserSessionDep userSessionDep) throws BizException {
//        if(userSessionDep == null || isExpire(userSessionDep.getExpireTime())){
//            throw new BizException(CommonResult.SESSION_EXPIRE);
//        }
//        return userSessionDep;
//    }

}
