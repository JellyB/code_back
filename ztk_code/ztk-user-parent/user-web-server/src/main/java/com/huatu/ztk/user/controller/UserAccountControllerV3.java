package com.huatu.ztk.user.controller;

import com.huatu.common.Result;
import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.user.bean.UserSession;
import com.huatu.ztk.user.common.AppType;
import com.huatu.ztk.user.common.UserErrors;
import com.huatu.ztk.user.galaxy.report.UserGetCaptchaContext;
import com.huatu.ztk.user.service.SensorsUserService;
import com.huatu.ztk.user.service.UserService;
import com.huatu.ztk.user.service.WechatService;
import com.huatu.ztk.user.utils.SensorsUtils;
import com.huatu.ztk.user.utils.WechatAESUtils;

import lombok.Getter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/8/1
 * @描述
 */
@RestController
@RequestMapping(value = "/v3/users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UserAccountControllerV3 {

    // logger
    private static final Logger logger = LoggerFactory.getLogger(UserAccountControllerV1.class);

    @Autowired
    private UserService userService;

    @Autowired
    private SensorsUserService sensorsUserService;
    
    @Autowired
    private WechatService wechatService;

    /**
     * 更新注册接口,添加deviceToken字段
     * 用户登录接口
     * 支持密码和验证码登录
     * 密码和验证码二选一,以验证码为优先
     *
     * @param account  账户
     * @param password 账户密码
     * @param captcha  验证码
     * @param from     登录来源
     * @param catgory  科目
     * @param cv       版本号
     * @param terminal 终端类型,登陆送课用到
     * @param request  用于获取注册ip
     * @return
     */
    @RequestMapping(value = "login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object login(@RequestParam String account, @RequestParam(required = false) String password,
                        @RequestParam(required = false) String captcha, @RequestParam(required = false) String from,
                        @RequestParam(defaultValue = CatgoryType.GONG_WU_YUAN + "") int catgory, @RequestParam String deviceToken,
                        @RequestHeader String cv, @RequestHeader int terminal, @RequestParam(required = false) String anonymousId,
                        @RequestHeader(defaultValue = "1") int appType, HttpServletRequest request) throws BizException {
        UserGetCaptchaContext.get().setCv(cv);
        UserGetCaptchaContext.get().setTerminal(terminal);
        UserSession session = null;
        String loginWay = "密码";
        account = account.trim();
        if (account != null) {
            Pattern p = Pattern.compile("(^\\s*)|(^\t)|(^\r)|(^\n)|(\\s*$)|(\t$)|(\r$)|(\n$)");
            Matcher m = p.matcher(account);
            account = m.replaceAll("");
        }
        if (StringUtils.isBlank(from)) {
            from = String.valueOf(terminal);
        }
        final long timeMillis = System.currentTimeMillis();

        if (StringUtils.isNoneBlank(captcha)) {// 通过验证码动态登录
            loginWay = "验证码";
            //logger.info(">>>> 开始用户登录验证码登录-,开始：{},account = {}",System.currentTimeMillis(),account);

            session = userService.loginFromCaptcha(account, captcha, userService.getRegip(request), terminal, catgory,
                    from, deviceToken);
            //logger.info(">>>> 用户登录验证码登录-,耗时,{},account = {}",System.currentTimeMillis() - timeMillis,account);
        } else if (StringUtils.isNoneEmpty(password)) {
            session = userService.login(account, password, terminal, catgory, deviceToken);
        }
        if (session == null) {
            return UserErrors.LOGIN_FAIL;
        }
        session.setAudit(userService.isIosAudit(catgory, terminal, cv));

        try {
            final long millis = System.currentTimeMillis();
			logger.info("appType is:{},has online:{}", appType, appType == AppType.ONLINE);
           // if (appType == AppType.ONLINE) {
                if (!StringUtils.isEmpty(session.getUcId())) {
                    sensorsUserService.loginAnalytics(session, (Boolean) SensorsUtils.getMessage().get("loginFirst"),
                            loginWay, terminal, (Long) SensorsUtils.getMessage().get("createTime"), anonymousId, from,
                            (Boolean) SensorsUtils.getMessage().get("fromUc"), "0");
                    SensorsUtils.removeMessage();
                    logger.info(">>>> 用户数据上报-{},耗时,{}", account, System.currentTimeMillis() - millis);

                }
          //  }
            // logger.info(">>>> 用户数据上报-{},耗时,{}",account,System.currentTimeMillis() - millis);
        } catch (Exception e) {
            logger.error("sa track error:{}", e);
        }
        return session;
    }
    
    /**
     * 根据微信code获取openid并存储sessionkey
     * @return
     * @throws BizException 
     */
    @RequestMapping(value = "code2Session", method = RequestMethod.GET)
	public Object code2Session(@RequestParam String code) throws BizException {
		return wechatService.code2Session(code);
		
	}
    
    
    
    /**
     * 为小程序授权手机号登录使用
     *
     * @param appId appId 微信开放id
     * @param encryptedData  微信加密后的用户信息
     * @param iv 加密算法的初始向量
     * @param catgory  科目
     * @param cv       版本号
     * @param terminal 终端类型,
     * @param request  用于获取注册ip
     * @return
     */
	@RequestMapping(value = "loginForWechat", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Object loginForWechat(@RequestParam String openId, @RequestParam String encryptedData, @RequestParam String iv,
			@RequestParam(defaultValue = CatgoryType.GONG_WU_YUAN + "") int catgory, @RequestHeader int terminal,
			HttpServletRequest request) throws BizException {
		UserSession session = userService.loginForWechat(openId, encryptedData, iv, userService.getRegip(request), terminal,
				catgory);
		if (session == null) {
			return UserErrors.LOGIN_FAIL;
		}
		return session;
	}
    

}
