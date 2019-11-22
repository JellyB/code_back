package com.huatu.ztk.user.service;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.user.bean.UserSession;
import com.huatu.ztk.user.common.UserErrors;
import com.huatu.ztk.user.common.UserRedisSessionKeys;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * 用户session工具包
 * Created by shaojieyue
 * Created time 2016-04-23 21:57
 */
@Service
public class UserSessionService {

    @Resource(name = "sessionRedisTemplate")
    private RedisTemplate<String, String> sessionRedisTemplate;
    @Value("${spring.profiles}")
    private String env;

    /**
     * App token
     */
    public static final String USER_TOKEN_KEY = "utoken_%s";

    /**
     * PC/M token (terminal 3/7)
     */
    public static final String USER_TOKEN_KEY_TERMINAL = "t_utoken_%s";

    public static final int DEFAULT_TERMINAL = 1;

    //新设备登录消息提示
    public static final String TIP_MESSAGE = "您的账号于%s在其它客户端登录，请重新登录。\n如非本人操作，则密码可能已泄露，请及时修改密码。";

    /**
     * 查询用户手机号s
     *
     * @param token
     * @return
     */
    public final String getMobileNo(String token) {
        return getSessionValue(token, UserRedisSessionKeys.mobile);
    }

    /**
     * 查询session key 对应的value
     *
     * @param token token
     * @param key   session 属性
     * @return
     */
    private final String getSessionValue(String token, String key) {
        String value = null;
        if (isBlank(token) || isBlank(key)) {
            return value;
        }
        final HashOperations<String, String, String> hashOperations = sessionRedisTemplate.opsForHash();
        value = hashOperations.get(token, key);
        return value;
    }


    /**
     * 查询用户名称
     *
     * @param token
     * @return
     */
    public final String getNick(String token) {
        return getSessionValue(token, UserRedisSessionKeys.nick);
    }

    /**
     * 查询UcId
     *
     * @param token
     * @return
     */
    public final String getUcId(String token) {
        return getSessionValue(token, UserRedisSessionKeys.ucId);
    }

    /**
     * 查询用户username
     *
     * @param token
     * @return
     */
    public final String getUname(String token) {
        return getSessionValue(token, UserRedisSessionKeys.uname);
    }

    /**
     * 获取用户id
     *
     * @param token
     * @return
     */
    public long getUid(String token) {
        final String uidStr = getSessionValue(token, UserRedisSessionKeys.id);
        long userId = -1;
        if (isNotBlank(uidStr)) {//id存在
            userId = Long.valueOf(uidStr);
        }
        return userId;
    }

    /**
     * 通过用户id查询其token
     *
     * @param userId
     * @return
     */
    public String getTokenById(long userId, int terminal) {
        String key = getTokenKeyByUserId(userId, terminal);
        final String token = sessionRedisTemplate.opsForValue().get(key);
        return token;
    }

    public String getTokenById(long userId) {
        Optional<String> optional = getAllTokenKeyByUserId(userId).stream()
                .map(tokenKey -> sessionRedisTemplate.opsForValue().get(tokenKey))
                .filter(StringUtils::isNotBlank)
                .findAny();
        return optional.get();
    }

    /**
     * 获取用户科目id
     *
     * @param token
     * @return
     */
    public int getSubject(String token) {
        int subject = getRealSubject(token);
        return subject;
    }


    /**
     * 事业单位ABCD科目,真题演练/精准估分/专项模考需要转化为其父科目（职测）
     *
     * @return
     */
    public int getNewSubject(String token) {
        int newSubject = getRealSubject(token);
        newSubject = convertChildSubjectToParentSubject(newSubject);
        System.out.print("最终科目ID是:{}" + newSubject);
        return newSubject;
    }

    /**
     * 从用户token中获取科目ID
     *
     * @return
     */
    public int getRealSubject(String token) {
        final String subjectStr = getSessionValue(token, UserRedisSessionKeys.subject);
        int subject = -1;
        if (isNotBlank(subjectStr)) {//id存在
            subject = Integer.valueOf(subjectStr);
        }
        return subject;
    }


    /**
     * 针对事业单位ABC类科目转换
     */
    public int convertChildSubjectToParentSubject(int subject) {
        System.out.print("env=" + env);
        //测试环境
        if (env.equals("dev") || env.equals("test")) {
            if (subject == 200100054 || subject == 200100055 || subject == 200100056 || subject == 200100057) {
                return 3;
            }
        }
        //线上环境
        if (subject == 200100054 || subject == 200100055 || subject == 200100056 || subject == 200100057) {
            return 4;
        }
        return subject;
    }

    /**
     * 获取当前用户所属的知识点类目
     *
     * @param token
     * @return
     */
    public int getCatgory(String token) {
        final String sessionValue = getSessionValue(token, UserRedisSessionKeys.catgory);
        int pointCatgory = -1;
        if (isNotBlank(sessionValue)) {//id存在
            pointCatgory = Integer.valueOf(sessionValue);
        }
        return pointCatgory;
    }

    /**
     * 获取用户抽题数量配置
     *
     * @param token
     * @return
     */
    public int getQcount(String token) {
        final String sessionValue = getSessionValue(token, UserRedisSessionKeys.qcount);
        int qcount = 10;
        if (isNotBlank(sessionValue)) {//id存在
            qcount = Integer.valueOf(sessionValue);
        }

        return qcount;
    }

    /**
     * 获取用户错题练习数量
     *
     * @param token
     * @return
     */
    public int getErrorQcount(String token) {
        final String sessionValue = getSessionValue(token, UserRedisSessionKeys.errorQcount);
        int errorQcount = 10;
        if (isNotBlank(sessionValue)) {//id存在
            errorQcount = Integer.valueOf(sessionValue);
        }

        return errorQcount;
    }

    /**
     * 查询区域id
     *
     * @param token token
     * @return
     */
    public int getArea(String token) {
        final String areaStr = getSessionValue(token, UserRedisSessionKeys.area);
        int area = -1;
        if (isNotBlank(areaStr)) {//id存在
            area = Integer.valueOf(areaStr);
        }
        return area;
    }

    /**
     * 查询email
     *
     * @param token token
     * @return
     */
    public String getEmail(String token) {
        final String emailStr = getSessionValue(token, UserRedisSessionKeys.email);

        if (isBlank(emailStr)) {//id存在
            return null;
        }
        return emailStr;
    }

    /**
     * 获取过期时间
     *
     * @param token
     * @return
     */
    public long getExpireTime(String token) {
        final String expireTimeStr = getSessionValue(token, UserRedisSessionKeys.expireTime);
        long loginTime = -1;
        if (isNotBlank(expireTimeStr)) {//id存在
            loginTime = Long.valueOf(expireTimeStr);
        }
        return loginTime;
    }

    /**
     * 判断用户登录状态是否过期
     *
     * @param token 用户token
     * @return true：过期 false：没有过期
     */
    private final boolean isExpire(String token) {
        if (isBlank(token)) {
            return true;
        }
        //过期时间小于当前时间，说明已经过期
        return getExpireTime(token) < System.currentTimeMillis();
    }

    /**
     * 断定session有效
     *
     * @param token 用户的token
     * @throws BizException 当用户session无效时抛出异常
     */
    public void assertSession(String token) throws BizException {
        final String oldToken = getSessionValue(token, UserRedisSessionKeys.oldToken);
        if ("1".equals(oldToken)) {//有新设备登录，当前设备已经被踢掉
            final String newDiveceLoginTime = getSessionValue(token, UserRedisSessionKeys.newDiveceLoginTime);
            //7月1日15:20
            final String time = DateFormatUtils.format(Long.parseLong(newDiveceLoginTime), "MM月dd日HH:mm");
            final String tipMessage = String.format(TIP_MESSAGE, time);
            //设置过期一个月,让自动过期
            sessionRedisTemplate.expire(token, 30, TimeUnit.DAYS);
            throw new BizException(UserErrors.LOGIN_ON_OTHER_DEVICE, tipMessage);
        }
        if (isExpire(token)) {//session过期
            //拋出普通的session過期異常
            throw new BizException(UserErrors.SESSION_EXPIRE);
        }
    }


    private static boolean isBlank(String str) {
        return str == null || "".equals(str);
    }

    private static final boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 是否为PC 的设备类型
     *
     * @param terminal 设备类型
     */
    public static final boolean isPCTerminal(int terminal) {
        //3 - v.huatu.com
        //7 - M 站
        //21 - 小程序
        return terminal == 3 || terminal == 7 || terminal == 21;
    }

    /**
     * 获取 用户 token 对应的 key
     */
    public static final String getTokenKeyByUserId(long userId, int terminal) {
        if (isPCTerminal(terminal)) {
            return String.format(USER_TOKEN_KEY_TERMINAL, userId);
        }
        return String.format(USER_TOKEN_KEY, userId);
    }

    /**
     * 获取所有类型的 的 token 对应key
     */
    public static final List<String> getAllTokenKeyByUserId(long userId) {
        return Lists.newArrayList(1, 3).stream()
                .map(terminalType -> getTokenKeyByUserId(userId, terminalType))
                .collect(Collectors.toList());
    }

    public UserSession getUserSession(String token) {
        UserSession userSession = new UserSession();
        if (StringUtils.isBlank(token)) {
            return userSession;
        }
        final HashOperations<String, String, String> hashOperations = sessionRedisTemplate.opsForHash();
        Map<String, String> entries = hashOperations.entries(token);
        BiFunction<Map<String, String>, String, Long> parseLong = ((map, key) -> null == map.get(key) ? -1L : Long.parseLong(map.get(key)));
        BiFunction<Map<String, String>, String, Integer> parseInt = ((map, key) -> null == map.get(key) ? -1 : Integer.parseInt(map.get(key)));
        BiFunction<Map<String, String>, String, String> parseString = ((map, key) -> null == map.get(key) ? "" : map.get(key));
        BiFunction<Map<String, String>, String, Boolean> parseBoolean = ((map, key) -> null == map.get(key) ? false : Boolean.parseBoolean(map.get(key)));
        userSession.setToken(token);
        userSession.setId(parseLong.apply(entries, "id"));
        userSession.setStatus(parseInt.apply(entries, "status"));
        userSession.setSubject(parseInt.apply(entries, "subject"));
        userSession.setArea(parseInt.apply(entries, "area"));
        userSession.setQcount(parseInt.apply(entries, "qcount"));
        userSession.setErrorQcount(parseInt.apply(entries, "errorQcount"));
        userSession.setCatgory(parseInt.apply(entries, "catgory"));
        userSession.setEmail(parseString.apply(entries, "email"));
        userSession.setMobile(parseString.apply(entries, "mobile"));
        userSession.setNick(parseString.apply(entries, "nick"));
        userSession.setSignature(parseString.apply(entries, "signature"));
        userSession.setUname(parseString.apply(entries, "uname"));
        userSession.setExpireTime(parseLong.apply(entries, "expireTime"));
        userSession.setSubjectName(parseString.apply(entries, "subjectName"));
        userSession.setAreaName(parseString.apply(entries, "areaName"));
        userSession.setUcId(parseString.apply(entries, "ucId"));
        userSession.setAudit(parseBoolean.apply(entries, "audit"));
        userSession.setFirstLogin(parseBoolean.apply(entries, "firstLogin"));
        userSession.setAvatar(parseString.apply(entries, "avatar"));
        userSession.setRegFrom(parseString.apply(entries, "regFrom"));
        return userSession;
    }
}
