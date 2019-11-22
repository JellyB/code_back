package com.huatu.ztk.user.controller;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Maps;
import com.huatu.ztk.user.util.UserTokenUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.redisqueue.core.RedisQueueTempalte;
import com.huatu.ztk.user.bean.NicknameUpdateMessage;
import com.huatu.ztk.user.bean.RewardMessage;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.bean.UserSession;
import com.huatu.ztk.user.bean.UserSign;
import com.huatu.ztk.user.common.UserErrors;
import com.huatu.ztk.user.galaxy.report.UserGetCaptchaContext;
import com.huatu.ztk.user.service.AvatarService;
import com.huatu.ztk.user.service.SensorsUserService;
import com.huatu.ztk.user.service.UcenterService;
import com.huatu.ztk.user.service.UserRewardService;
import com.huatu.ztk.user.service.UserService;
import com.huatu.ztk.user.service.UserSessionService;
import com.huatu.ztk.user.utils.SensorsUtils;

import static com.huatu.ztk.commons.RewardConstants.ACTION_ATTENDANCE;

/**
 * 用户账户有关的
 * Created by shaojieyue
 * Created time 2016-04-23 20:57
 */

@RestController
@RequestMapping(value = "/v1/users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UserAccountControllerV1 {
    //logger
    private static final Logger logger = LoggerFactory.getLogger(UserAccountControllerV1.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private UcenterService ucenterService;

    @Autowired
    private AvatarService avatarService;

    @Autowired
    private UserRewardService userRewardService;

    @Autowired
    private RedisQueueTempalte redisQueueTempalte;

    @Autowired
	private SensorsUserService sensorsService;

    @RequestMapping(value = "me", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object queryMe(@RequestHeader(required = false) String token) throws BizException {
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        UserDto userDto = userService.findById(userId);
        return userDto;
    }

    /**
     * 用户登录接口
     * 支持密码和验证码登录
     * 密码和验证码二选一,以验证码为优先
     *
     * @param account  账户
     * @param password 账户密码
     * @param captcha  验证码
     * @param from  登录来源
     * @param catgory 科目
     * @param cv 版本号
     * @param terminal 终端类型,登陆送课用到
     * @param request 用于获取注册ip
     * @param anonymousId 用于神策上报判断是否pc登录
     * @return
     */
    @RequestMapping(value = "login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object login(@RequestParam String account,
                        @RequestParam(required = false) String password,
                        @RequestParam(required = false) String captcha,
                        @RequestParam(required = false) String from,
                        //@RequestParam(defaultValue = CatgoryType.GONG_WU_YUAN+ "") int catgory
                        /*
                            这个默认值很奇怪，后边还判断了catgory//-1：表示不指定考试类型
                            先设置默认值为-1，不传的时候取上次的配置
                        */
                        @RequestParam (defaultValue = "-1")int catgory,
                        @RequestHeader String cv,
                        @RequestHeader int terminal,
                        @RequestParam(required = false) String anonymousId,
                        @RequestParam(required = false, defaultValue = "0") String source, // 来源 只区分中1石油和0其他
                        HttpServletRequest request) throws BizException {
        UserGetCaptchaContext.get().setCv(cv);
        UserGetCaptchaContext.get().setTerminal(terminal);
        UserSession session = null;
        String loginWay = "密码";
        account = account.trim();
        if (account!=null) {
            Pattern p = Pattern.compile("(^\\s*)|(^\t)|(^\r)|(^\n)|(\\s*$)|(\t$)|(\r$)|(\n$)");
            Matcher m = p.matcher(account);
            account = m.replaceAll("");
        }
        if (StringUtils.isBlank(from)){
            from = String.valueOf(terminal);
        }
        if (StringUtils.isNoneBlank(captcha)) {//通过验证码动态登录
        	loginWay = "验证码";
            session = userService.loginFromCaptcha(account, captcha, userService.getRegip(request), terminal, catgory,from,"");
        } else if (StringUtils.isNoneEmpty(password)){
            session = userService.login(account, password, terminal, catgory,"");
        }
        if (session == null) {
            return UserErrors.LOGIN_FAIL;
        }
        session.setAudit(userService.isIosAudit(catgory, terminal, cv));
		try {
			// 神策上报
			if (!StringUtils.isEmpty(session.getUcId())) {
				sensorsService.loginAnalytics(session, (Boolean) SensorsUtils.getMessage().get("loginFirst"), loginWay,
						terminal, (Long) SensorsUtils.getMessage().get("createTime"),anonymousId,from,(Boolean) SensorsUtils.getMessage().get("fromUc"),source);
				SensorsUtils.removeMessage();
			}
		} catch (Exception e) {
			logger.error("sa track error:{}", e);
		}
        return session;
    }

    /**
     * 退出操作
     *
     * @return
     */
    @RequestMapping(value = "logout", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public SuccessMessage logout(
            @RequestHeader(defaultValue = "1") int terminal,
            @RequestHeader(required = false) String token
    ) throws BizException {
        //退出登录
        userService.logout(token,terminal);
        return SuccessMessage.create("账户退出成功");
    }

    /**
     * 注册接口，不带密码的
     *
     * @param mobile 注册手机号
     * @param captcha 验证码
     * @param catgory 科目
     * @param from 来源
     * @param request 用于获取注册ip
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "register", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object register(@RequestParam String mobile,
                           @RequestParam String captcha,
                           @RequestParam(required = false) String from,
                           @RequestParam(defaultValue = CatgoryType.GONG_WU_YUAN + "") int catgory,
                           @RequestHeader int terminal,
                           @RequestHeader(required = false,defaultValue = "-1") String cv,
                           HttpServletRequest request) throws BizException {
        UserGetCaptchaContext.get().setCv(cv);
        UserGetCaptchaContext.get().setTerminal(terminal);
        int realFrom;
        if (StringUtils.isBlank(from)){
            realFrom = terminal;
        }else {
            realFrom = Integer.valueOf(from);
        }
        final UserSession session = userService.register(mobile, captcha, null, userService.getRegip(request), catgory,realFrom,"", terminal);
        return session;
    }

    /**
     * 完善个人信息接口
     *
     * @param password
     * @param nick
     * @return
     */
    @RequestMapping(value = "complete", method = RequestMethod.PUT)
    public Object complete(
            @RequestHeader(defaultValue = UserSessionService.DEFAULT_TERMINAL + "") int terminal,
            @RequestHeader(required = false) String token,
            @RequestParam String password,
            @RequestParam(defaultValue = "-1") int categoryId,
            @RequestHeader(defaultValue = "-1") int category,
            @RequestParam String nick) throws BizException {
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        int catgory = UserTokenUtil.getHeaderSubject(token,userSessionService::getCatgory,categoryId,category);
        final UserSession userSession = userService.complete(userId, password, nick, catgory,terminal);
        return userSession;
    }

    /**
     * 重置密码
     *
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "resetpwd", method = RequestMethod.PUT)
    public Object resetpwd(@RequestParam String mobile,
                           @RequestParam String password,
                           @RequestParam String captcha,
                           @RequestHeader(required = false,defaultValue = "-1") String cv,
                           @RequestHeader(required = false,defaultValue = UserSessionService.DEFAULT_TERMINAL + "") int terminal) throws BizException {
        UserGetCaptchaContext.get().setCv(cv);
        UserGetCaptchaContext.get().setTerminal(terminal);
        password = StringUtils.trimToNull(password);
        userService.resetpwd(mobile, captcha, password);
        return SuccessMessage.create("密码重置成功,请重新登录");
    }

    /**
     * 修改密码接口
     *
     * @param token
     * @param oldpwd
     * @param newpwd
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "modifypwd", method = RequestMethod.PUT)
    public Object modifypwd(@RequestHeader(required = false) String token,
                            @RequestParam String oldpwd,
                            @RequestParam String newpwd) throws BizException {
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        //修改密码
        userService.modifypwd(userId, oldpwd, newpwd);

        return SuccessMessage.create("密码修改成功,请重新登录");
    }

    /**
     * 选择考试的科目
     *
     * @param token
     * @param area    区域
     * @return
     */
    @RequestMapping(value = "subject", method = RequestMethod.PUT)
    public Object selectSubject(
            @RequestHeader(defaultValue = UserSessionService.DEFAULT_TERMINAL + "") int terminal,
            @RequestHeader(required = false) String token,
            @RequestHeader(defaultValue = "-1") int category,
            @RequestParam(defaultValue = "-1") int categoryId,
            @RequestParam int area) throws BizException {
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        int catgory = UserTokenUtil.getHeaderSubject(token,userSessionService::getCatgory,categoryId,category);
        return userService.updateSubject(userId,area,catgory,terminal);
    }

    /**
     * 修改昵称
     *
     * @param token
     * @param nickname 昵称
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "nick", method = RequestMethod.PUT)
    public Object modifyNickname(
            @RequestHeader(defaultValue = UserSessionService.DEFAULT_TERMINAL + "") int terminal,
            @RequestHeader(required = false) String token,
            @RequestHeader(defaultValue = "-1") int category,
            @RequestParam(defaultValue = "-1") int categoryId,
            @RequestParam String nickname) throws BizException {
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        String username = userSessionService.getUname(token);
        String nickOld = userSessionService.getNick(token);
        int catgory = UserTokenUtil.getHeaderSubject(token,userSessionService::getCatgory,categoryId,category);

        //修改昵称
        UserSession userSession = userService.modifyNickname(userId, nickname, catgory,terminal);
        // add by hanchao,2017-11-01
        // 同步昵称到网校
        redisQueueTempalte.convertAndSend("queue.netschool_user_nick_update", NicknameUpdateMessage
                .builder()
                .username(username)
                .nickname(nickname)
                .nicknameOld(nickOld)
                .avatar("")
                .build());
        return userSession;
    }

    /**
     * 测试接口，获取测试token
     * @param pwd
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "gettoken", method = RequestMethod.GET)
    public Object gettoken(@RequestParam String pwd,@RequestParam long uid) throws BizException {
        String token = null;
        if (pwd.equals("123ztk")) {
            token = userSessionService.getTokenById(uid,3);
        }
        Map data = new HashMap();
        data.put("token", token);
        return data;
    }

    /**
     * PHP 换取token接口
     */
    @RequestMapping(value = "{userId}/phpToken",method = RequestMethod.GET)
    public Object getTokenForPhp(
            @RequestHeader("secret") String secret,
            @RequestHeader("terminal") int terminal,
            @PathVariable("userId") long userId) throws BizException {
        if (UserSessionService.isPCTerminal(terminal)){
            throw new BizException(ErrorResult.create(5000000,"设备类型错误"));
        }
        String md5Secret = UserSystemControllerV1.MD5(userId + "");
        if (!md5Secret.equals(secret)){
            return "default";
        }
        return userSessionService.getTokenById(userId, terminal);
    }

    /**
     * 内部 换取token接口
     */
    @RequestMapping(value = "{userId}/token",method = RequestMethod.GET)
    public Object getTokenForInner(@RequestHeader("secret") String secret, @PathVariable("userId") long userId) throws BizException {
        HashMap<String, String> map = new HashMap<>();
        if (secret.equals("123ztk")) {
            String appToken = userSessionService.getTokenById(userId,1);
            String pcToken = userSessionService.getTokenById(userId,3);
            map.put("appToken",appToken);
            map.put("pcToken",pcToken);
        }
        return map;
    }


    /**
     * 测试接口，删除ucenter和本地的测试用户
     * @param phone
     * @return
     */
    //@RequestMapping(value = "deluser",method = RequestMethod.GET)
    public Object delUser(@RequestParam String phone) {
        ucenterService.delUser(phone);
        return SuccessMessage.create("ok.");
    }


    /**
     * 上传头像
     * @param token
     * @return
     * @throws BizException
     * @throws IOException
     */
    @RequestMapping(value = "avatar", method = RequestMethod.POST,
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object uploadAvatar(MultipartHttpServletRequest request, @RequestHeader(required = false) String token) throws BizException, IOException, FileUploadException {
        userSessionService.assertSession(token);
        //获取用户id
        long userId = userSessionService.getUid(token);
        String username = userSessionService.getUname(token);
        String nickOld = userSessionService.getNick(token);
        if (!ServletFileUpload.isMultipartContent(request)) {
            throw new BizException(UserErrors.UPLOAD_AVATAR_FAIL);
        }
        final Collection<List<MultipartFile>> values = request.getMultiFileMap().values();

        List<MultipartFile> images = Lists.newArrayList();
        values.forEach(files->{
            images.addAll(files);
        });
        String url= avatarService.uploadAvatar(new BufferedInputStream(images.get(0).getInputStream()),userId);
        logger.info("上传头像:{}", url);

        // 同步头像到网校
        NicknameUpdateMessage nicknameUpdateMessage = NicknameUpdateMessage
                .builder()
                .username(username)
                .nickname(nickOld)
                .nicknameOld(nickOld)
                .avatar(url)
                .build();
        redisQueueTempalte.convertAndSend("queue.netschool_user_nick_update", nicknameUpdateMessage);

        logger.info("更新用户头像message:{}", nicknameUpdateMessage);
        Map map = new HashMap<>();
        map.put("url", url);
        return map;
    }


    /**
     * 签到
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "sign", method = RequestMethod.POST)
    public RewardMessage sign(@RequestHeader(required = false, defaultValue = "") String token) throws BizException {
        if(StringUtils.isBlank(token)){
            return RewardMessage.builder().uname(StringUtils.EMPTY).uid(0).action(ACTION_ATTENDANCE).bizId(StringUtils.EMPTY).experience(0).timestamp(System.currentTimeMillis()).gold(0).build();
        }
    	long start1 = System.currentTimeMillis();
        userSessionService.assertSession(token);
    	long start2 = System.currentTimeMillis();
    	logger.info("zc assertSession spend:{}",start2-start1);
        //用户id
        long userId = userSessionService.getUid(token);
        String uname = userSessionService.getUname(token);
        long start3 = System.currentTimeMillis();
        logger.info("zc get user info spend:{}",start3-start2);

        return userRewardService.sign(userId, uname, new Date());
    }

    /**
     * 查询今天的签到记录
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "sign", method = RequestMethod.GET)
    public UserSign getSign(@RequestHeader(required = false, defaultValue = "") String token) throws BizException {
        if(StringUtils.isBlank(token)){
            return new UserSign(0, new Date(), 0, 0);
        }
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);

        return userRewardService.findTodaySign(userId);
    }


    @RequestMapping(value = "userId", method = RequestMethod.GET)
    public long getUIdByUserName(@RequestParam(required = false) String userName) throws BizException {

        return userRewardService.getUIdByUserName(userName);
    }

    @RequestMapping(value = "userIdBatch", method = RequestMethod.POST)
    public List<JSONObject> getUIdByUsernameBatch(@RequestBody List<String> userNames) throws BizException{
        return userRewardService.getSimpleUserInfoBatch(userNames);
    }

    /**
     * 通过 userId 批量获取用户信息
     * @param userId
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "userNameById", method = RequestMethod.GET)
    public Map<String, Object> getUserInfoById(@RequestParam(value = "userId") Integer userId) throws BizException{
        Map<String, Object> result = Maps.newHashMap();
        try{
            String userName =  userRewardService.getUserNameById(userId);
            result.put("userName", userName);
            result.put("userId", userId);
            return result;
        }catch (Exception e){
            logger.error("getUserInfoById:{}", e.getMessage());
            return result;
        }
    }


    /**
     * 根据用户id，批量查询用户信息
     *
     * @param userMap
     * @return
     */
    @RequestMapping(value = "batch", method = RequestMethod.POST)
    public List<UserDto> getUserBatchByUserIds(@RequestBody HashMap<String, String> userMap) {
        logger.info("根据用户id，批量查询用户信息。userIds：{}", userMap);
        String userIds = userMap.get("userIds");
        if (StringUtils.isEmpty(userIds)) {
            return Lists.newArrayList();
        }
        return userService.getUserBatchByUserIds(userIds);
    }


    /**
     * 根据用户id，批量查询用户信息
     *
     * @param
     * @return
     */
    @RequestMapping(value = "batchUserInfo", method = RequestMethod.POST)
    public List<UserDto> getUserBatch(@RequestBody List<String> userIds) {
        logger.info("根据用户id，批量查询用户信息。userIds：{}", userIds);
        if (CollectionUtils.isEmpty(userIds)) {
            return Lists.newArrayList();
        }
        String collect = userIds.stream().collect(Collectors.joining(","));
        return userService.getUserBatchByUserIds(collect);
    }

    /**
     * 保存一条用户信息到 ucenter 库
     * @param userMap
     * @return
     */
    @RequestMapping(value = "saveUcUser", method = RequestMethod.POST)
    public Object saveUser(@RequestBody HashMap<String, String> userMap) {
        return userService.saveUserSimple(userMap);
    }
    
    /**
     * 同步uc和本地错误用户名
     * @return
     */
    //@RequestMapping(value = "syncUcInfo", method = RequestMethod.GET)
    public Object syncUcInfo() {
        return userService.syncUcInfo();
    }
    
    /**
     * 根据username过期token信息
     * @param uname
     * @return
     */
    //@RequestMapping(value = "delSessionByUname", method = RequestMethod.GET)
    public Object delSessionByUname(String uname) {
    	
        return userService.delSessionByUname(uname);
    }
    
    /**
     * 删除uc库中memebr表数据
     * @param uname
     * @return
     */
   // @RequestMapping(value = "delUCMemberByUname", method = RequestMethod.GET)
    public Object delUCMemberByUname(String uname) {
    	
        return userService.delUCMemberByUname(uname);
    }

    /**
     * 根据用户名/手机号查询
     *
     * @param params 用户名/手机号
     * @return 用户列表
     */
    @RequestMapping(value = "batchUserInfoByUsernameOrMobile", method = RequestMethod.POST)
    public Object batchUserInfoByUsernameOrMobile(@RequestBody List<String> params) {

        return userService.getUserBatchByByUsernameOrMobile(params);
    }
    
    /**
     * 为php提供检测用户是否为压测数据
     * @return
     */
	@RequestMapping(value = "checkUserIsMock", method = RequestMethod.GET)
	public Object checkUserIsMock(String uName) {
		return userService.checkUserIsMock(uName);
	}
	
	@RequestMapping(value = "loginForTest", method = RequestMethod.GET)
	public Object loginForTest() {
		return userService.loginForTest();
	}
}
