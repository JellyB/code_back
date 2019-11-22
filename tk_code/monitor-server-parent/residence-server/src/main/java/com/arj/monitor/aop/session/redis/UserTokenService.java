package com.arj.monitor.aop.session.redis;

/**
 * @author zhouwei
 */

//@Slf4j
@Deprecated
//@Service
public class UserTokenService {
//
//    //token 过期时间 单位：s 7776000=3个月,此处一定要定义为long，不然×1000存在越界
//    public static final long USER_TOKEN_TIME_OUT = 7776000;
//
//
//    @Resource(name = "sessionRedisTemplate")
//    private RedisTemplate<String, String> sessionRedisTemplate;
//
//
//
//    /**
//     * 用户登录
//     *
//     * @return
//     */
//    public UserSessionDep login(String username, String password) throws BizException {
//        User user = new User();
//        user.setId(1L);
//        user.setNickname("周威");
//        user.setUsername(username);
//
//
//        UserSessionDep userSessionDep = new UserSessionDep();
//        userSessionDep.setId(user.getId());
//        userSessionDep.setNickname(StringUtils.trimToEmpty(user.getNickname()));
//        userSessionDep.setUsername(user.getUsername());
//         String token = generateToken();
//        userSessionDep.setToken(token);
//        //设置过期时间 session失效时间比redis删除key的时间早一分钟
//        userSessionDep.setExpireTime(System.currentTimeMillis() + (USER_TOKEN_TIME_OUT * 1000) - 60000);
//        saveSessionToRedis(userSessionDep);
//
//
//        return userSessionDep;
//    }
//
//
//
//
//    /**
//     * 保持session到redis里面
//     *
//     * @param userSessionDep
//     */
//    private void saveSessionToRedis(UserSessionDep userSessionDep) {
//        log.info("token:", userSessionDep.getToken());
//        Map<String, String> sessionInfo = new HashMap();
//
//
//        //用户信息id
//        sessionInfo.put(UserRedisSessionKeys.ID, userSessionDep.getId() + "");
//        //设置登录时间
//        sessionInfo.put(UserRedisSessionKeys.EXPIRE_TIME, userSessionDep.getExpireTime() + "");
//        sessionInfo.put(UserRedisSessionKeys.TELEPHONE, StringUtils.trimToEmpty(userSessionDep.getTelephone()));
//        sessionInfo.put(UserRedisSessionKeys.NICKNAME, StringUtils.trimToEmpty(userSessionDep.getNickname()));
//        sessionInfo.put(UserRedisSessionKeys.USERNAME, userSessionDep.getUsername());
//        final HashOperations<String, Object, Object> hashOperations = sessionRedisTemplate.opsForHash();
//        try {
//            hashOperations.putAll(userSessionDep.getToken(), sessionInfo);
//        } catch (Exception e) {
//           e.printStackTrace();
//        }
//        sessionRedisTemplate.expire(userSessionDep.getToken(), USER_TOKEN_TIME_OUT, TimeUnit.SECONDS);
//    }
//
//    /**
//     * 生成token
//     *
//     * @return
//     */
//    private String generateToken() {
//        final String token = UUID.randomUUID().toString().replaceAll("-", "");
//        return token;
//    }
//
//
//    /**
//     * 退出登录操作
//     *
//     * @param token
//     */
//    public void logout(String token) throws BizException {
//
//        /**
//         * 设置过期时间,此处把过期时间设置为当前之前的时间,
//         * 来达到token过期的目的
//         * 这里不采用直接把key删除的方式来过期,目的是方式还有程序通过token访问
//         * session数据,如果直接删除,就会报错
//         */
//        sessionRedisTemplate.opsForHash().put(token, UserRedisSessionKeys.EXPIRE_TIME, String.valueOf(System.currentTimeMillis() - 10000));
//        //重新设置过期时间
//        sessionRedisTemplate.expire(token, 100, TimeUnit.SECONDS);
//
//    }
}
