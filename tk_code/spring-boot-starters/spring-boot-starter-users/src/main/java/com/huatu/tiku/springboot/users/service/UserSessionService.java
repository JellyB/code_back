package com.huatu.tiku.springboot.users.service;

import com.huatu.common.CommonResult;
import com.huatu.common.exception.BizException;
import com.huatu.common.exception.UnauthorizedException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.springboot.users.common.UserRedisSessionKeys;
import com.huatu.tiku.springboot.users.support.SessionRedisTemplate;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * 用户session工具包
 * Created by shaojieyue
 * Created time 2016-04-23 21:57
 */
public class UserSessionService {
    public static final int DEFAUL_QCOUNT = 10;

    @Autowired
    private SessionRedisTemplate sessionRedisTemplate;

    /**
     * 医护人员token
     */
    public static final String USER_TOKEN_KEY = "utoken_%s";

    //新设备登录消息提示
    public static final String TIP_MESSAGE = "您的账号于%s在其它客户端登录，请重新登录。\n如非本人操作，则密码可能已泄露，请及时修改密码。";


    /**
     * 查询session key 对应的value
     *
     * @param token token
     * @param key   session 属性
     * @return
     */
    private final String getSessionValue(String token, String key) {
        String value = null;
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(key)) {
            return value;
        }
        value = sessionRedisTemplate.hget(token, key);
        return value;
    }

    /**
     * 获取用户id
     *
     * @param token
     * @return
     */
    public long getUid(String token) {
        long userId = -1;
        if (isNotBlank(token)) {
            final String uidStr = getSessionValue(token, UserRedisSessionKeys.id);
            if (StringUtils.isEmpty(uidStr)) {//id存在
                userId = Long.valueOf(uidStr);
            }
        }
        return userId;
    }

    /**
     * 通过用户id查询其token
     *
     * @param userId
     * @return
     */
    public String getTokenById(long userId) {
        String key = String.format(USER_TOKEN_KEY, userId);
        final String token = sessionRedisTemplate.get(key);
        return token;
    }

    /**
     * 查询用户手机号
     *
     * @param token
     * @return
     */
    @Deprecated
    public final String getMobileNo(String token) {
        return getSessionValue(token, UserRedisSessionKeys.mobile);
    }

    /**
     * 查询用户名称
     *
     * @param token
     * @return
     */
    @Deprecated
    public final String getNick(String token) {
        return getSessionValue(token, UserRedisSessionKeys.nick);
    }

    /**
     * 查询用户username
     *
     * @param token
     * @return
     */
    @Deprecated
    public final String getUname(String token) {
        return getSessionValue(token, UserRedisSessionKeys.uname);
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
        if (isNotBlank(subjectStr)) {
            subject = Integer.valueOf(subjectStr);
        }
        return subject;
    }

    /**
     * 针对事业单位ABC类科目转换
     */
    public int convertChildSubjectToParentSubject(int subject) {
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
    @Deprecated
    public int getCatgory(String token) {
        final String sessionValue = getSessionValue(token, UserRedisSessionKeys.catgory);
        int pointCatgory = -1;
        if (StringUtils.isNotEmpty(sessionValue)) {//id存在
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
    @Deprecated
    public int getQcount(String token) {
        final String sessionValue = getSessionValue(token, UserRedisSessionKeys.qcount);
        int qcount = 10;
        if (StringUtils.isNotEmpty(sessionValue)) {//id存在
            qcount = Integer.valueOf(sessionValue);
        }

        return qcount;
    }

    /**
     * 查询区域id
     *
     * @param token token
     * @return
     */
    @Deprecated
    public int getArea(String token) {
        final String areaStr = getSessionValue(token, UserRedisSessionKeys.area);
        int area = -1;
        if (StringUtils.isNotEmpty(areaStr)) {//id存在
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
    @Deprecated
    public String getEmail(String token) {
        final String emailStr = getSessionValue(token, UserRedisSessionKeys.email);

        if (StringUtils.isEmpty(emailStr)) {//id存在
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
    @Deprecated
    public long getExpireTime(String token) {
        final String expireTimeStr = getSessionValue(token, UserRedisSessionKeys.expireTime);
        long loginTime = -1;
        if (StringUtils.isEmpty(expireTimeStr)) {//id存在
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
    @Deprecated
    private final boolean isExpire(String token) {
        if (StringUtils.isEmpty(token)) {
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
    @Deprecated
    public void assertSession(String token) throws BizException {
        final String oldToken = getSessionValue(token, UserRedisSessionKeys.oldToken);
        if ("1".equals(oldToken)) {//有新设备登录，当前设备已经被踢掉
            final String newDiveceLoginTime = getSessionValue(token, UserRedisSessionKeys.newDiveceLoginTime);
            //fastdateformat维护了自己的缓存，不会重复生成实例
            final String time = DateFormatUtils.format(Long.parseLong(newDiveceLoginTime), "MM月dd日HH:mm");
            final String tipMessage = String.format(TIP_MESSAGE, time);
            //设置过期一个月,让自动过期
            sessionRedisTemplate.expire(token, 30, TimeUnit.DAYS);
            throw new UnauthorizedException(CommonResult.LOGIN_ON_OTHER_DEVICE, tipMessage);
        }

        if (isExpire(token)) {//session过期
            //拋出普通的session過期異常
            throw new BizException(CommonResult.SESSION_EXPIRE);
        }
    }


    /**
     * =============================== new method =============================================
     */

    /**
     * 判断用户登陆状态是否过期
     *
     * @param expireTime
     * @return
     */
    public boolean isExpire(long expireTime) {
        return expireTime < System.currentTimeMillis();
    }

    /**
     * 获取完整的usersession
     *
     * @param token
     * @return
     */
    public UserSession getUserSession(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        Map<String, String> serializeSession = sessionRedisTemplate.hgetAll(token);
        if (MapUtils.isNotEmpty(serializeSession)) {

            String ssoId = serializeSession.get(UserRedisSessionKeys.ssoId);
            String id = serializeSession.get(UserRedisSessionKeys.id);
            String subject = serializeSession.get(UserRedisSessionKeys.subject);
            String area = serializeSession.get(UserRedisSessionKeys.area);
            String expireTime = serializeSession.get(UserRedisSessionKeys.expireTime);
            String category = serializeSession.get(UserRedisSessionKeys.catgory);
            String qcount = serializeSession.get(UserRedisSessionKeys.qcount);

            UserSession userSession = UserSession.builder()
                    .token(token)
                    .ssoId(StringUtils.isEmpty(ssoId) ? -1 : Integer.valueOf(ssoId))
                    .id(StringUtils.isEmpty(id) ? -1 : Integer.valueOf(id))
                    .subject(StringUtils.isEmpty(subject) ? -1 : Integer.valueOf(subject))
                    .area(StringUtils.isEmpty(area) ? -1 : Integer.valueOf(area))
                    .expireTime(StringUtils.isEmpty(expireTime) ? -1 : Long.valueOf(expireTime))
                    .category(StringUtils.isEmpty(category) ? -1 : Integer.valueOf(category))
                    .qcount(StringUtils.isEmpty(qcount) ? DEFAUL_QCOUNT : Integer.valueOf(qcount))
                    .mobile(serializeSession.get(UserRedisSessionKeys.mobile))
                    .email(serializeSession.get(UserRedisSessionKeys.email))
                    .nick(serializeSession.get(UserRedisSessionKeys.nick))
                    .uname(serializeSession.get(UserRedisSessionKeys.uname))
                    .oldToken(serializeSession.get(UserRedisSessionKeys.oldToken))
                    .newDiveceLoginTime(serializeSession.get(UserRedisSessionKeys.newDiveceLoginTime))
                    .ucId(serializeSession.get(UserRedisSessionKeys.ucId))
                    .build();
            return userSession;
        }
        return null;
    }

    /**
     * 断定session是否有效
     *
     * @param userSession
     * @throws BizException
     */
    public UserSession assertSession(UserSession userSession) throws BizException {
        if (userSession == null) {
            throw new UnauthorizedException(CommonResult.SESSION_EXPIRE);
        }
        //有新设备登录，当前设备已经被踢掉
        if ("1".equals(userSession.getOldToken())) {
            //fastdateformat维护了自己的缓存，不会重复生成实例
            final String time = DateFormatUtils.format(Long.parseLong(userSession.getNewDiveceLoginTime()), "MM月dd日HH:mm");
            final String tipMessage = String.format(TIP_MESSAGE, time);
            //设置过期一个月,让自动过期
            sessionRedisTemplate.expire(userSession.getToken(), 30, TimeUnit.DAYS);
            throw new UnauthorizedException(CommonResult.LOGIN_ON_OTHER_DEVICE, tipMessage);
        }

        if (isExpire(userSession.getExpireTime())) {//session过期
            //拋出普通的session過期異常
            throw new UnauthorizedException(CommonResult.SESSION_EXPIRE);
        }
        return userSession;
    }

}
