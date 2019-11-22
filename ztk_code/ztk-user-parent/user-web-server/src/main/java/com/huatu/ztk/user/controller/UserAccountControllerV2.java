package com.huatu.ztk.user.controller;


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.huatu.common.consts.TerminalType;
import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.bean.UserSession;
import com.huatu.ztk.user.common.AppType;
import com.huatu.ztk.user.common.CourseType;
import com.huatu.ztk.user.galaxy.report.UserGetCaptchaContext;
import com.huatu.ztk.user.service.ActivityService;
import com.huatu.ztk.user.service.SensorsUserService;
import com.huatu.ztk.user.service.UserService;
import com.huatu.ztk.user.service.UserSessionService;
import com.huatu.ztk.user.util.UserTokenUtil;
import com.huatu.ztk.user.utils.Crypt3Des;
import com.huatu.ztk.user.utils.DES3Utils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linkang on 9/30/16.
 */

@RestController
@RequestMapping(value = "/v2/users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UserAccountControllerV2 {
    private static final Logger logger = LoggerFactory.getLogger(UserAccountControllerV2.class);

    @Autowired
    private UserService userService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private SensorsUserService sensorsService;

    /**
     * 带密码的注册接口
     *
     * @param mobile   注册手机号
     * @param captcha  验证码
     * @param password 明文密码
     * @param from     来源
     * @param catgory  科目，默认为公考
     * @param terminal 终端类型
     * @param request  用于获取注册ip
     *                 appType 1 类是appType,1 华图在线,2 教师app
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object register(@RequestParam String mobile,
                           @RequestParam String captcha,
                           @RequestParam String password,
                           @RequestParam(required = false) String from,
                           @RequestParam(defaultValue = CatgoryType.GONG_WU_YUAN + "") int catgory,
                           @RequestHeader int terminal,
                           @RequestParam(required = false, defaultValue = "0") String source,//来源 1为中石油 否则为其他
                           @RequestHeader(required = false, defaultValue = "-1") String cv,
                           @RequestHeader(defaultValue = "1") int appType,
                           HttpServletRequest request) throws BizException {

        UserGetCaptchaContext.get().setCv(cv);
        UserGetCaptchaContext.get().setTerminal(terminal);
        String regIp = userService.getRegip(request);
        int realFrom;
        if (StringUtils.isBlank(from)) {
            realFrom = terminal;
        } else {
            realFrom = Integer.valueOf(from);
        }
        final UserSession session = userService.register(mobile, captcha, password, regIp, catgory, realFrom, "", terminal);

        if ("weixin_active".equals(from)) {
            activityService.sendCourse(session.getUname(), terminal, CourseType.LOGIN, catgory);
        }
       // if (appType == AppType.ONLINE) {
            //神策埋点
            if (!StringUtils.isEmpty(session.getUcId()) && terminal == TerminalType.PC) {
                sensorsService.registerAnalytics(mobile, terminal, from, session.getUcId(), source);
            }
        //}
        return session;
    }

    /**
     * PHP的注册接口 不需要验证码
     */
    @RequestMapping(value = "registerForPHP", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object registerForPHP(@RequestParam String mobile,
                                 @RequestParam String password,
                                 @RequestParam(required = false) String from,
                                 @RequestParam(defaultValue = CatgoryType.GONG_WU_YUAN + "") int catgory,
                                 @RequestHeader int terminal,
                                 HttpServletRequest request) throws BizException {
        String regIp = userService.getRegip(request);
        if (StringUtils.isBlank(from)) {
            from = String.valueOf(terminal);
        }
        final UserSession session = userService.register(mobile, password, regIp, catgory, from, terminal, "");
        return session;
    }

    /**
     * 给教育提供同步用户信息接口
     *
     * @param mobile
     * @param password
     * @param from
     * @param terminal
     * @param request
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "userInfoAsync", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object registerForPHP(@RequestParam String mobile,
                                 @RequestParam String password,
                                 @RequestParam(required = false) String from,
                                 @RequestHeader int terminal,
                                 HttpServletRequest request) throws BizException {
        String regIp = userService.getRegip(request);
        if (StringUtils.isBlank(from)) {
            from = String.valueOf(terminal);
        }
        return userService.userInfoAsync(mobile, password, regIp, from, terminal, "");
    }

    /**
     * 更换手机号
     *
     * @param token
     * @param mobile
     * @param captcha
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "mobile", method = RequestMethod.PUT)
    public UserSession bind(
            @RequestHeader(defaultValue = UserSessionService.DEFAULT_TERMINAL + "") int terminal,
            @RequestHeader(required = false) String token,
            @RequestParam String mobile,
            @RequestHeader(required = false, defaultValue = "-1") String cv,
            @RequestHeader(defaultValue = "-1") int category,
            @RequestParam(defaultValue = "-1") int categoryId,
            @RequestParam String captcha
    ) throws BizException {
        UserGetCaptchaContext.get().setCv(cv);
        UserGetCaptchaContext.get().setTerminal(terminal);
        userSessionService.assertSession(token);

        long uid = userSessionService.getUid(token);
        String uname = userSessionService.getUname(token);
        int catgory = UserTokenUtil.getHeaderSubject(token, userSessionService::getCatgory, categoryId, category);
        return userService.updateMobile(uid, uname, mobile, catgory, captcha, terminal);
    }

    /**
     * 校验验证码
     *
     * @return
     */
    @RequestMapping(value = "validateCaptcha", method = RequestMethod.POST)
    public Object validateCaptcha(@RequestParam String mobile,
                                  @RequestParam String captcha,
                                  @RequestHeader(required = false, defaultValue = "-1") String cv,
                                  @RequestHeader(required = false, defaultValue = UserSessionService.DEFAULT_TERMINAL + "") int terminal) {
        {
            UserGetCaptchaContext.get().setCv(cv);
            UserGetCaptchaContext.get().setTerminal(terminal);
        }
        //验证码对应的key
        Object o = userService.validateCapcha(mobile, captcha);
        HashMap<String, Object> map = new HashMap<>();
        map.put("flag", !(captcha == null || !captcha.equals(o.toString())));
        return map;
    }

    /**
     * 为PHP 提供通过手机号码获取用户信息接口
     *
     * @param mobile 手机号码
     * @param secret 验证信息
     * @return
     */
    @RequestMapping(value = "getUserInfoByMobileForPHP/{mobile}", method = RequestMethod.GET)
    public Object getUserInfoByMobile(
            @PathVariable("mobile") String mobile,
            @RequestHeader("secret") String secret
    ) {
        String md5Secret = UserSystemControllerV1.MD5(mobile);
        if (md5Secret.equals(secret)) {
            UserDto userDto = userService.getUserInfoByMobile(mobile);
            return userDto;
        }
        return new UserDto();
    }

    /**
     * 为PHP 提供修改密码的接口
     *
     * @return
     */
    @RequestMapping(value = "updateUserPasswordForPHP", method = RequestMethod.POST)
    public Object updateUserPasswordForPHP(
            @RequestHeader("secret") String secret,
            @RequestParam String mobile,
            @RequestParam String newpwd
    ) throws BizException {
        String md5Secret = UserSystemControllerV1.MD5(mobile);
        if (md5Secret.equals(secret)) {
            UserDto userDto = userService.getUserInfoByMobile(mobile);
            if (null == userDto) {
                throw new BizException(ErrorResult.create(5000000, "用户信息不存在"));
            }
            //修改密码
            userService.modifyPassword(userDto, newpwd);
        } else {
            throw new BizException(ErrorResult.create(5000000, "校验失败"));
        }
        return SuccessMessage.create("密码修改成功,请重新登录");
    }

    /**
     * 为PHP 提供批量注册用户信息接口，传入信息{mobile,password}
     * 默认的from 为 PC：2
     *
     * @return
     */
    @RequestMapping(value = "registerAllForPHP", method = RequestMethod.POST)
    public Object registerAllForPHP(
            @RequestBody List<UserDto> registerList,
            HttpServletRequest request
    ) {
        logger.info("批量注册数据 : num : {},data : {}", registerList.size(), JSON.toJSON(registerList));
        if (null == registerList || registerList.size() == 0) {
            return new HashMap<Boolean, Object>();
        }
        String regIp = userService.getRegip(request);
        return userService.registerForPHP(registerList, regIp, "");
    }

    /**
     * 为PHP 提供查询用户信息接口
     *
     * @return
     */
    @RequestMapping(value = "getUserInfoForPHP/{account}", method = RequestMethod.GET)
    public Object getUserInfoForPHP(
            @PathVariable("account") String account
    ) throws BizException {
        HashMap userInfoForPhp = userService.getUserInfoForPhp(account);
        return userInfoForPhp;
    }

    /**
     * PHP 通过手机号查询 用户名信息
     */
    @RequestMapping(value = "getUserNameByPhoneForPHP", method = RequestMethod.POST)
    public Object getUserNameForPHP(@RequestBody List<String> phoneList) {
        if (CollectionUtils.isEmpty(phoneList)) {
            return StringUtils.EMPTY;
        }
        Map<String, List<String>> userNameByPhoneForPHP = userService.getUserNameByPhoneForPHP(phoneList);
        return userNameByPhoneForPHP;
    }

    /**
     * 为小程序提供通过手机号获取token 小程序不传type
     * 教育学习平台需要传type app
     *
     * @return
     */
    @RequestMapping(value = "token", method = RequestMethod.GET)
    public Object getToken(@RequestParam String phone,
                           @RequestHeader(defaultValue = "") String appId,
                           @RequestHeader int terminal,
                           @RequestHeader(defaultValue = "weixin", required = false) String type,
                           HttpServletRequest request) throws BizException {
        String mobile = null;
        if (type.equals("weixin")) {
            mobile = DES3Utils.decrypt(phone);
        } else {
            terminal = TerminalType.WEI_XIN_APPLET;
            mobile = Crypt3Des.decryptMode(phone);
            logger.info("获取token接口,terminal是:{},手机号是:{}", terminal, mobile);
        }
        if (StringUtils.isBlank(mobile) || mobile.length() != 11 || !NumberUtils.isDigits(mobile)) {
            System.out.println(phone + "||" + mobile);
            throw new BizException(ErrorResult.create(1100123, "账号信息非法"));
        }
        HashMap<Object, Object> result = Maps.newHashMap();
        UserSession userSession = userService.loginVirtual(mobile, terminal);
        if (null == userSession) {
//            throw new BizException(ErrorResult.create(10000999,"用户手机号未在在线平台注册过账号"));
            String regIp = userService.getRegip(request);
            UserSession register = userService.register(mobile, mobile, regIp, 1, terminal + "", terminal, appId);

            result.put("token", register.getToken());
            result.put("id", register.getId());
            result.put("uname", register.getUname());
            result.put("ucId", register.getUcId());
            result.put("subject", register.getSubject());
            result.put("subjectName", register.getSubjectName());
            result.put("expireTime", register.getExpireTime());
            result.put("mobile", register.getMobile());
        } else {
            result.put("token", userSession.getToken());
            result.put("id", userSession.getId());
            result.put("uname", userSession.getUname());
            result.put("ucId", userSession.getUcId());
            result.put("subject", userSession.getSubject());
            result.put("subjectName", userSession.getSubjectName());
            result.put("expireTime", userSession.getExpireTime());
            result.put("mobile", userSession.getMobile());
        }
        return result;

    }
}
