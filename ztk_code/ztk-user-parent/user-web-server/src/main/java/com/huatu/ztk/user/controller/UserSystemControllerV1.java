package com.huatu.ztk.user.controller;

import static com.huatu.ztk.user.galaxy.report.UserGetCaptchaContext.UserGetCaptchaEvent.GET_CAPTCHA;
import static com.huatu.ztk.user.service.UserService.REGISTER_USER_PHP;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.huatu.ztk.user.util.UserTokenUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;
import com.huatu.ztk.commons.AreaConstants;
import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.user.bean.AppVersionBean;
import com.huatu.ztk.user.bean.CheckBean;
import com.huatu.ztk.user.bean.Feedback;
import com.huatu.ztk.user.bean.UserConfig;
import com.huatu.ztk.user.bean.UserSession;
import com.huatu.ztk.user.common.UserErrors;
import com.huatu.ztk.user.common.VersionRedisKey;
import com.huatu.ztk.user.dubbo.VersionDubboService;
import com.huatu.ztk.user.galaxy.MqService;
import com.huatu.ztk.user.galaxy.report.SendEvent;
import com.huatu.ztk.user.galaxy.report.UserGetCaptchaContext;
import com.huatu.ztk.user.service.ActivityService;
import com.huatu.ztk.user.service.AppVersionService;
import com.huatu.ztk.user.service.FeedbackService;
import com.huatu.ztk.user.service.RegisterFreeCourseDetailConfig;
import com.huatu.ztk.user.service.ThirdTaskComponent;
import com.huatu.ztk.user.service.UserConfigService;
import com.huatu.ztk.user.service.UserMessageService;
import com.huatu.ztk.user.service.UserServerConfig;
import com.huatu.ztk.user.service.UserService;
import com.huatu.ztk.user.service.UserSessionService;
import com.huatu.ztk.user.utils.IpUtils;

import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.UserAgent;

/**
 * Created by shaojieyue
 * Created time 2016-06-06 17:26
 */

@RestController
@RequestMapping(value = "/v1/users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Scope("singleton")
public class UserSystemControllerV1 {
    private static final Logger logger = LoggerFactory.getLogger(UserSystemControllerV1.class);
    @Autowired
    private UserServerConfig userServerConfig;
    @Autowired
    private MqService mqService;

    /**
     * 默认反馈类型，其他
     */
    public static final int DEFAULT_FEEDBACK_TYPE = 1;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private UserService userService;

    @Resource(name = "redisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private UserMessageService userMessageService;

    @Autowired
    private VersionDubboService versionDubboService;

    @Autowired
    private UserConfigService userConfigService;

    @Autowired
    private AppVersionService appVersionService;

    @Autowired
    private UserSystemControllerUtil userSystemControllerUtil;

    @Autowired
    private SendEvent sendEvent;

    @Autowired
    private RegisterFreeCourseDetailConfig registerFreeCourseDetailConfig;

    @Autowired
    private ThirdTaskComponent thirdTaskComponent;

    /**
     * 意见反馈
     *
     * @param feedback
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/feedback", method = RequestMethod.POST)
    public Object feedback(@RequestBody Feedback feedback,
                           @RequestHeader(required = false) String token,
                           @RequestHeader String cv,
                           @RequestParam(defaultValue = "-1") int categoryId,
                           @RequestHeader(defaultValue = "-1") int category,
                           @RequestHeader(defaultValue = "") String system,
                           @RequestHeader(defaultValue = "") String device) throws BizException {
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        final int catgory = UserTokenUtil.getHeaderSubject(token, userSessionService::getCatgory, categoryId, category);

        feedback.setUid(userId);//设置用户id
        feedback.setType(DEFAULT_FEEDBACK_TYPE);

        feedbackService.insert(feedback, cv, system, device, catgory);
        return SuccessMessage.create("您的意见已经提交成功");
    }

    /**
     * 发送短信验证码
     *
     * @param mobile 手机号
     */
    @RequestMapping(value = "/captcha/{mobile}", method = RequestMethod.GET)
    public Object get(@PathVariable String mobile, HttpServletRequest request,
                      @RequestHeader(required = false, defaultValue = "-1") String cv,
                      @RequestHeader(required = false, defaultValue = UserSessionService.DEFAULT_TERMINAL + "") int terminal) throws BizException {
        String clientIp = null;
        try {
            {
                UserGetCaptchaContext.get().setCv(cv);
                UserGetCaptchaContext.get().setPhone(mobile);
                UserGetCaptchaContext.get().setTerminal(terminal);
                UserGetCaptchaContext.get().setEventName(GET_CAPTCHA);
            }
            final String real = request.getHeader("X-Real-IP");
            final String forwardedFor = request.getHeader("X-Forwarded-For");
            String agent = request.getHeader("User-Agent");
            if (StringUtils.isBlank(agent)) {
                agent = request.getHeader("user-agent");
            }
            final UserAgent userAgent = UserAgent.parseUserAgentString(agent);
            if (userAgent.getOperatingSystem().getDeviceType() == DeviceType.COMPUTER
                    || !isLegalAgent(agent)) {
                logger.debug("mobile={},agent={},forwardedFor={}", mobile, agent, forwardedFor);

                return SuccessMessage.create("发送验证码成功");
            }

            clientIp = Optional.ofNullable(IpUtils.getIpFromRequest(request)).orElse("unknow");
            logger.debug("client={} send sms,mobile={}", clientIp, mobile);
            logger.debug("forwardedFor={},mobile={}, agent={},realip={}", forwardedFor, mobile, agent, real);
        } catch (Exception e) {
            logger.error("ex", e);
        }

        userService.sendCaptcha(mobile, clientIp, true);

        sendEvent.send(UserGetCaptchaContext.get());
        return SuccessMessage.create("发送验证码成功");
    }

    /**
     * PHP 发送短信调用接口
     *
     * @throws BizException
     */
    @RequestMapping(value = "/captchaForPHP/{mobile}", method = RequestMethod.GET)
    public Object get(
            @PathVariable("mobile") String mobile,
            @RequestParam("key") String key,
            @RequestHeader(required = false, defaultValue = "-1") String cv,
            @RequestParam("secret") String secret,
            @RequestHeader(required = false, defaultValue = UserSessionService.DEFAULT_TERMINAL + "") int terminal,
            HttpServletRequest request) throws BizException {
        {
            UserGetCaptchaContext.get().setCv(cv);
            UserGetCaptchaContext.get().setPhone(mobile);
            UserGetCaptchaContext.get().setTerminal(terminal);
            UserGetCaptchaContext.get().setEventName(GET_CAPTCHA);
        }
        String md5Secret = MD5(key);
        logger.info("短信MD5验证,key = {},secret = {},md5Secret = {}", key, secret, md5Secret);
        if (StringUtils.isNoneBlank(md5Secret) && md5Secret.equals(secret)) {
            String clientIp = Optional.ofNullable(IpUtils.getIpFromRequest(request)).orElse("unknow");
            userService.sendCaptcha(mobile, clientIp, false);
        }
        sendEvent.send(UserGetCaptchaContext.get());
        return SuccessMessage.create("发送验证码成功");
    }

    /**
     * PHP 验证码登录
     */
    @RequestMapping(value = "captchaLoginForPhp", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object captchaLoginForPhp(
            @RequestParam String account,
            @RequestParam(required = false) String captcha,
            @RequestParam(defaultValue = REGISTER_USER_PHP) String from,
            @RequestParam(defaultValue = CatgoryType.GONG_WU_YUAN + "") int catgory,
            @RequestHeader(defaultValue = "3") int terminal,
            HttpServletRequest request
    ) throws BizException {
        UserSession userSession = userService.loginFromCaptcha(account, captcha, userService.getRegip(request), terminal, catgory, from, "");
        if (userSession == null) {
            return UserErrors.LOGIN_FAIL;
        }
        return userSession;
    }


    /**
     * 简单判断agent是否合法
     *
     * @param agent
     * @return
     */
    private boolean isLegalAgent(String agent) {
        //安卓,ios,pc理论上都带agent,不带agent视为非法请求
        if (StringUtils.isBlank(agent)) {
            return false;
        }
        return agent.contains("okhttp") || agent.contains("netschool") || agent.contains("_android");
    }


    /**
     * 检测用户状态信息
     * 比如:新消息,新活动
     *
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "red", method = RequestMethod.GET)
    public Object msgMark(@RequestHeader(required = false) String token,
                          @RequestParam(defaultValue = "-1") int categoryId,
                          @RequestHeader(defaultValue = "-1") int category) throws BizException {
        userSessionService.assertSession(token);

        //用户id
        long userId = userSessionService.getUid(token);
        int catgory = UserTokenUtil.getHeaderSubject(token, userSessionService::getCatgory, categoryId, category);
        //最新活动个数
        long unreadActCount = activityService.getUnReadActCount(userId);
        //未读消息个数
        int unreadMsgCount = userMessageService.getUnReadMsgCount(userId, catgory);

        Map data = new HashMap();
        data.put("unreadMsgCount", unreadMsgCount);
        data.put("unreadActCount", unreadActCount);
        return data;
    }

    /**
     * 区域信息
     *
     * @return
     */
    @RequestMapping(value = "/areas", method = RequestMethod.GET)
    public Object areas(@RequestParam(defaultValue = "1") int depth) {
        return AreaConstants.getAreas(depth);
    }

    /**
     * 用户基本配置
     *
     * @param area    考试区域,可以精确到市
     * @param qcount  用户自定义抽题数量
     * @param subject 用户抽题类目
     * @param token   用户token
     * @return
     */
    @RequestMapping(value = "/config", method = RequestMethod.PUT)
    public Object config(@RequestParam(defaultValue = "-1") int area,
                         @RequestParam(defaultValue = "-1") int qcount,
                         @RequestParam(defaultValue = "-1") int subject,
                         @RequestParam(defaultValue = "-1") int categoryId,
                         @RequestParam(defaultValue = "-1") int catgory,
                         @RequestHeader(defaultValue = "-1") int category,
                         @RequestHeader(required = false) String token,
                         @RequestHeader(defaultValue = UserSessionService.DEFAULT_TERMINAL + "") int terminal
    ) throws BizException {
        userSessionService.assertSession(token);
        logger.info("config token={}", token);
        UserSession userSession = userSessionService.getUserSession(token);
        final int finalCategory = UserTokenUtil.getHeaderSubject(token, (i) -> userSession.getCatgory(), catgory, categoryId, category);
        UserConfig userConfig = userConfigService.update(area, qcount, subject, userSession.getId(), finalCategory, -1, terminal, userSession.getUcId(), userSession.getUname());//UserConfig
        return userConfig;
    }


    /**
     * 设置版本信息
     *
     * @param android_version
     * @param ios_version
     * @param full
     * @param bulk
     * @param message
     * @param mod
     */
    @RequestMapping(value = "/setVersion", method = RequestMethod.PUT)
    public void setVersion(@RequestParam(required = false) String android_version,
                           @RequestParam(required = false) String ios_version,
                           @RequestParam(required = false) String full,
                           @RequestParam(required = false) String bulk,
                           @RequestParam(required = false) String message,
                           @RequestParam(required = false) String mod) {
        final ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();

        if (StringUtils.isNoneBlank(android_version)) {
            valueOperations.set(VersionRedisKey.ANDROID_LATEST_VERSION_KEY, android_version);
        }

        if (StringUtils.isNoneBlank(ios_version)) {
            valueOperations.set(VersionRedisKey.IOS_LATEST_VERSION_KEY, ios_version);
        }

        if (StringUtils.isNoneBlank(full)) {
            valueOperations.set(VersionRedisKey.ANDROID_FULL_URL_KEY, full);
        }

        if (StringUtils.isNoneBlank(bulk)) {
            valueOperations.set(VersionRedisKey.ANDROID_BULK_URL_KEY, bulk);
        }

        if (StringUtils.isNoneBlank(message)) {
            message = message.replaceAll("\\|", "\n");
            valueOperations.set(VersionRedisKey.VERSION_MESSAGE_KEY, message);
        }

        if (StringUtils.isNoneBlank(mod)) {
            valueOperations.set(VersionRedisKey.MOD_VALUE_KEY, mod);
        }

    }

    /**
     * 对外提供使用验证码的接口
     *
     * @param mobile  手机号
     * @param captcha 验证码
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "captcha", method = RequestMethod.GET)
    public Object userCaptcha(@RequestParam String mobile,
                              @RequestParam String captcha,
                              @RequestHeader(required = false, defaultValue = "-1") String cv,
                              @RequestHeader(required = false, defaultValue = UserSessionService.DEFAULT_TERMINAL + "") int terminal) throws BizException {
        UserGetCaptchaContext.get().setCv(cv);
        // UserGetCaptchaContext.get().setTerminal(terminal);
        userService.userCaptcha(mobile, captcha);
        return SuccessMessage.create("验证通过");
    }


    /**
     * 检测接口
     */
    @RequestMapping(value = "check", method = RequestMethod.GET)
    public Object checkStatus(@RequestParam(defaultValue = "-1") int source,
                              @RequestHeader String cv,
                              @RequestHeader int terminal,
                              @RequestParam(defaultValue = "-1") int categoryId,
                              @RequestHeader(defaultValue = "-1") int category,
                              @RequestHeader(defaultValue = "") String system,
                              @RequestHeader(defaultValue = "") String device,
                              @RequestHeader(required = false) String token) throws BizException {
        //logger.info("source is :{}", source);
        //用户id
        long userId = userSessionService.getUid(token);
        final int finalCategory = UserTokenUtil.getHeaderSubject(token, userSessionService::getCatgory, categoryId, category);
        //   logger.info("version check cv={},terminal={},device={}", cv, terminal, device);
        //AppVersionBean version = versionDubboService.checkVersion(terminal, cv, userId, category);
        AppVersionBean version = appVersionService.findNewVersion(userId, terminal, cv);

        logger.info("版本号:{},终端:{},考试类别:{}", cv, terminal, finalCategory);
        //事业单位没有送课
//        int status = (category == CatgoryType.GONG_WU_YUAN) ? activityService.getFreeCourseStatus(userId, cv) : 0;
        boolean isAudit = false;
        final ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String auditListRedis = valueOperations.get("auditList");
        //logger.info("userId={},auditListRedis={}", userId, auditListRedis);
        if (StringUtils.isNotBlank(auditListRedis) && auditListRedis.contains(userId + "")) {
            isAudit = true;
          //  logger.info("auditList,isAudit=true");
        } //有白名单一定是内购状态

        else {
            isAudit = userService.isIosAudit(finalCategory, terminal, cv);
           // logger.info("!auditList,isAudit={}", isAudit);
        }
        CheckBean checkBean = CheckBean.builder()
                .appVersionBean(version)
                .commentStatus(0)
                .audit(isAudit)
                .aboutEmail("fkzhuantiku@163.com")
                .aboutPhone("400-8989-766")
                .coursePhone("400-8989-766")
                //.seckillUrl("http://sk.test.htexam.net")
                .seckillUrl(userServerConfig.getSeckillUrl())
                .essayCorrectFree(userService.correctFree())//申论批改是否免费
//                .photoAnswer(userService.photoAnswer())//是否支持拍照答题
//                .voiceAnswer(userService.voiceAnswer())//是否支持语音答题
                /**
                 * 答题开关，不同客户端分开控制（2018-03-27）
                 */
                .photoAnswer(userService.photoAnswerV2(terminal, cv))//是否支持拍照答题（0支持  1不支持）
                .voiceAnswer(userService.voiceAnswerV2(terminal, cv))//是否支持语音答题（0支持  1不支持）
                .photoAnswerType(userService.photoAnswerType(terminal))//拍照识别对接第三方（0  汉王  1优图）
                .photoAnswerMsg((null == userService.getPhotoAnswerMsg()) ? "" : userService.getPhotoAnswerMsg())
                .build();
        mqService.send(token + "," + terminal);
        //收集用户设备信息
        userSystemControllerUtil.storeDeviceInfo(device, system, userSessionService.getMobileNo(token));
        return checkBean;
    }

    /**
     * 检测是否开启注册送课接口（7.1+）
     *
     * @param cv
     * @param terminal
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "checkRegisterGiveCourseStatus", method = RequestMethod.GET)
    public Object checkRegisterGiveCourseStatus(
            @RequestHeader String cv,
            @RequestHeader int terminal,
            @RequestHeader(required = false) String token) throws BizException {
        String title = registerFreeCourseDetailConfig.getRegTitle();
        return ImmutableMap.builder().put("status", registerFreeCourseDetailConfig.getOpenRegisterFreeCourse())
                .put("title", title).build();

    }

    /**
     * pc获取注册送课信息
     *
     * @param cv
     * @param terminal
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "getRegisterGiveCourseForPc", method = RequestMethod.GET)
    public Object getRegisterGiveCourseForPc(
            @RequestHeader String cv,
            @RequestHeader int terminal,
            @RequestHeader() String token) throws BizException {
        if (terminal == TerminalType.PC) {
            String uname = userSessionService.getUname(token);
            return userService.getRegisterGiveCourseForPc(uname);
        }
        return null;

    }

    /**
     * 用户注册上报位置信息
     *
     * @param cv
     * @param terminal
     * @param token
     * @param city         市名称
     * @param device       设备名称
     * @param district     地区
     * @param positionName
     * @param street       街道名称
     * @param system       系统版本
     * @param number       门牌号
     * @return
     */
    @RequestMapping(value = "/report/position", method = RequestMethod.POST)
    public Object reportPosition(@RequestHeader String cv, @RequestHeader int terminal, @RequestHeader String token,
                                 String city, @RequestHeader String device, String district, String positionName, String province, String street,
                                 @RequestHeader String system, String number) {
        String uName = userSessionService.getUname(token);
        String phone = userSessionService.getMobileNo(token);
        logger.info("用户:{}注册上报位置信息city:{},device:{},district:{},positionName:{},privice:{},street:{},systemVersion:{},number:{}",
                uName, city, device, district, positionName, province, street, system, number);
        thirdTaskComponent.reportRegisterPosition(phone, uName, cv, terminal, province, city, district, street, positionName,
                device, system);
        return SuccessMessage.create("上报成功");

    }
    
	/**
	 * 检测微信小程序是否开启引流
	 * 
	 * @return
	 */
	@RequestMapping(value = "/wechat/check", method = RequestMethod.GET)
	public Object wechatCheck() {
		return SuccessMessage.create(userServerConfig.getWechatCheckStatus());

	}
    

    public static final String ANDROID_NEW_VERSION = "6.0";
    public static final String IOS_NEW_VERSION = "6.0.0";

    private boolean isNewVersion(int terminal, String cv) {
        boolean iosNewVersion = (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD)
                && cv.compareTo(IOS_NEW_VERSION) >= 0;

        boolean androidNewVersion = (terminal == TerminalType.ANDROID || terminal == TerminalType.ANDROID_IPAD)
                && cv.compareTo(ANDROID_NEW_VERSION) >= 0;

        return iosNewVersion || androidNewVersion;
    }


    public static String MD5(String key) {
        char hexDigits[] = {
                'A', 'B', 'C', 'D', 'E', 'F', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'H', 'U'
        };
        try {
            byte[] btInput = key.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }
}
