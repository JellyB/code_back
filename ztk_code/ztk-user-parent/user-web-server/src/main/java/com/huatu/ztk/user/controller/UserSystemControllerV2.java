package com.huatu.ztk.user.controller;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.huatu.ztk.commons.*;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.commons.exception.SuccessResponse;
import com.huatu.ztk.user.bean.*;
import com.huatu.ztk.user.galaxy.MqService;
import com.huatu.ztk.user.galaxy.report.SendEvent;
import com.huatu.ztk.user.galaxy.report.UserGetCaptchaContext;
import com.huatu.ztk.user.service.*;
import com.huatu.ztk.user.util.UserTokenUtil;
import com.huatu.ztk.user.utils.IpUtils;
import com.huatu.ztk.user.utils.UploadFileUtil;
import com.rits.cloning.Cloner;

import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.huatu.ztk.user.galaxy.report.UserGetCaptchaContext.UserGetCaptchaEvent.GET_CAPTCHA;


@RestController
@RequestMapping(value = "/v2/users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UserSystemControllerV2 {
    private static final Logger logger = LoggerFactory.getLogger(UserSystemControllerV2.class);

    @Autowired
    private UserServerConfig userServerConfig;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private UserConfigService userConfigService;

    @Autowired
    private UserService userService;

    @Autowired
    private UploadFileUtil uploadFileUtil;

    @Autowired
    private MqService mqService;


    @Autowired
    private AppVersionService appVersionService;

    @Autowired
    private UserSystemControllerUtil userSystemControllerUtil;

    @Autowired
    private SendEvent sendEvent;

    @Autowired
    private RedisTemplate redisTemplate;
    
    private Cloner cloner = new Cloner();

    Cache<String, CheckBean> checkInfoCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

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
                         @RequestParam(defaultValue = "-1") int errorQcount,
                         @RequestHeader(required = false) String token,
                         @RequestHeader(defaultValue = UserSessionService.DEFAULT_TERMINAL + "") int terminal
    ) throws BizException {
        if (StringUtils.isEmpty(token)) {
            UserConfig userConfig = new UserConfig();
            if (category > 0) {
                userConfig.setCategory(category);
            } else {
                userConfig.setCategory(SubjectType.GWY_XINGCE);
            }
            userConfig.setSubject(subject > 0 ? subject : SubjectType.GWY_XINGCE);
            userConfig.setArea(area);
            userConfig.setQcount(errorQcount);
            return userConfig;
        }
        userSessionService.assertSession(token);
        //logger.info("config token={}", token);
        long uid = userSessionService.getUid(token);//用户id
        String ucid = userSessionService.getUcId(token);//ucId
        String uname = userSessionService.getUname(token);
        int finalCategory = UserTokenUtil.getHeaderSubject(token, userSessionService::getCatgory, catgory, categoryId, category);
        UserConfig userConfig = userConfigService.update(area, qcount, subject, uid, finalCategory, errorQcount, terminal, ucid, uname);//UserConfig

        userConfigService.addLastConfigId(userConfig);
        return userConfig;
    }

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
                           @RequestParam(defaultValue = "-1") int categoryId,
                           @RequestHeader(defaultValue = "-1") int category,
                           @RequestHeader(required = false) String token,
                           @RequestHeader String cv,
                           @RequestHeader(defaultValue = "") String system,
                           @RequestHeader(defaultValue = "") String device) throws BizException, IOException {

        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        final int catgory = UserTokenUtil.getHeaderSubject(token, userSessionService::getCatgory, categoryId, category);

        feedback.setUid(userId);//设置用户id
        feedback.setUname(userSessionService.getNick(token));

        /**
         * 如果用户没有填写联系方式，设置为用户注册手机号
         */
        if (StringUtils.isEmpty(feedback.getContacts())) {
            feedback.setContacts(userSessionService.getMobileNo(token));
        }
        feedbackService.insert(feedback, cv, system, device, catgory);
        return SuccessMessage.create("您的意见已经提交成功");
    }


//    @RequestMapping(value = "uploadImages",method = RequestMethod.POST)
//    public String uploadImages(@RequestParam("file") MultipartFile[] files) throws IOException {
//        // 上传图片
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < files.length; i++) {
//            sb.append(uploadFileUtil.ftpUploadTickling(files[i]));
//            if(i < files.length-1){
//                sb.append(",");
//            }
//        }
//        return sb.toString();
//    }

    /**
     * 上传图片
     *
     * @param file
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "uploadImage", method = RequestMethod.POST)
    public Object uploadImage(MultipartFile file) throws IOException {
        //  return uploadFileUtil.ftpUploadTickling(file);
        return new SuccessResponse(uploadFileUtil.ftpUploadTickling(file));
    }


    /**
     * 上传日志
     *
     * @param log
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "uploadLog", method = RequestMethod.POST)
    public Object uploadLogs(MultipartFile log) throws IOException {
        return new SuccessResponse(uploadFileUtil.logUploadTickling(log));
    }


    /**
     * 获取意见反馈
     *
     * @param type
     * @param size
     * @param page
     * @return
     */
    @RequestMapping(value = "/feedback", method = RequestMethod.GET)
    public FeedbackDto getFeedback(@RequestParam Integer type,
                                   @RequestParam Integer processed,
                                   @RequestParam Integer size,
                                   @RequestParam Integer page,
                                   @RequestParam Integer isSolve,
                                   @RequestParam String content,
                                   @RequestParam Long id,
                                   @RequestParam long start,
                                   @RequestParam long end) throws BizException {

        FeedbackDto feedback = feedbackService.getFeedback(type, processed, size, page, isSolve, content, id, start, end);
        return feedback;

    }

    /**
     * 删除意见反馈
     *
     * @param id
     */
    @RequestMapping(value = "/delFeedback", method = RequestMethod.DELETE)
    public void delFeedback(Integer id, String modifier) {
        logger.info("需要删除的ID:{}", id);
        feedbackService.delFeedback(id, modifier);
    }

    /**
     * 解决意见反馈
     *
     * @param solve
     * @param id
     */
    @RequestMapping(value = "/solveFeedback", method = RequestMethod.GET)
    public void solveFeedback(Integer solve, Integer id, String modifier) {
        feedbackService.setSolve(id, solve, modifier);
    }

    /**
     * 用户的区域信息
     *
     * @return
     */
    @RequestMapping(value = "/areas", method = RequestMethod.GET)
    public Object areas(@RequestParam(defaultValue = "1") int depth,
                        @RequestHeader(required = false) String token,
                        @RequestHeader int terminal,
			@RequestParam int catgory) throws BizException {

		Map result = new HashMap();
		List<Area> areas = AreaConstants.getAreas(depth);
		if (terminal == TerminalType.WEI_XIN_APPLET && catgory == CatgoryType.GONG_WU_YUAN) {
			List<Area> areasClone = cloner.deepClone(areas);
			areasClone.stream().forEach(area -> {
				if (area.getId() == AreaConstants.QUAN_GUO_ID) {
					area.setName("国考");
				}
			});
			result.put("areas", areasClone);
		}else {
			result.put("areas", areas);
		}
		if (StringUtils.isEmpty(token)) {
			result.put("userArea", AreaConstants.QUAN_GUO_ID);
		} else {
			userSessionService.assertSession(token);
			// 用户id
			long userId = userSessionService.getUid(token);
			UserConfig userConfig = userConfigService.findByUidAndCatgory(userId, catgory);
			result.put("userArea", userConfig != null ? userConfig.getArea() : AreaConstants.QUAN_GUO_ID);
		}

		return result;
	}

    /**
     * 发送短信验证码
     *
     * @param mobile 手机号
     */
    @RequestMapping(value = "/captcha/{mobile}", method = RequestMethod.GET)
    public Object get(@PathVariable String mobile, HttpServletRequest request,
                      @RequestHeader(required = false, defaultValue = "-1") String cv,
                      @RequestParam(required = false, defaultValue = "-1") String type,
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

            if (!"M".equals(type)) {

                if (StringUtils.isBlank(agent)) {
                    agent = request.getHeader("user-agent");
                }
                final UserAgent userAgent = UserAgent.parseUserAgentString(agent);
                if (userAgent.getOperatingSystem().getDeviceType() == DeviceType.COMPUTER || !isLegalAgent(agent)) {
                    logger.info("filter mobile={},agent={},forwardedFor={}", mobile, agent, forwardedFor);

                    return SuccessMessage.create("发送验证码成功");
                }

            }
            clientIp = Optional.ofNullable(IpUtils.getIpFromRequest(request)).orElse("unknow");
            logger.info("client={} send sms,mobile={}", clientIp, mobile);
            logger.info("forwardedFor={},mobile={}, agent={},realip={}", forwardedFor, mobile, agent, real);
        } catch (Exception e) {
            logger.error("ex", e);
        }

        userService.sendCaptcha(mobile, clientIp, false);
        sendEvent.send(UserGetCaptchaContext.get());
        return SuccessMessage.create("发送验证码成功");
    }

    /**
     * 为找回密码发送短信
     *
     * @throws BizException
     */
    @RequestMapping(value = "findPasswordCaptcha/{mobile}", method = RequestMethod.GET)
    public Object getFindPasswordCaptcha(@PathVariable String mobile, HttpServletRequest request,
                                         @RequestHeader(required = false, defaultValue = "-1") String cv,
                                         @RequestParam(required = false, defaultValue = "-1") String type,
                                         @RequestHeader(required = false, defaultValue = UserSessionService.DEFAULT_TERMINAL + "") int terminal) throws BizException {
        {
            UserGetCaptchaContext.get().setCv(cv);
            UserGetCaptchaContext.get().setPhone(mobile);
            UserGetCaptchaContext.get().setTerminal(terminal);
            UserGetCaptchaContext.get().setEventName(GET_CAPTCHA);
        }
        String clientIp = null;
        try {
            final String real = request.getHeader("X-Real-IP");
            final String forwardedFor = request.getHeader("X-Forwarded-For");
            String agent = request.getHeader("User-Agent");
            if (StringUtils.isBlank(agent)) {
                agent = request.getHeader("user-agent");
            }
            final UserAgent userAgent = UserAgent.parseUserAgentString(agent);
            if (!"M".equalsIgnoreCase(type)) {
                if (userAgent.getOperatingSystem().getDeviceType() == DeviceType.COMPUTER
                        || !isLegalAgent(agent)) {
                    logger.info("filter mobile={},agent={},forwardedFor={}", mobile, agent, forwardedFor);

                    return SuccessMessage.create("发送验证码成功");
                }
            }

            clientIp = Optional.ofNullable(IpUtils.getIpFromRequest(request)).orElse("unknow");
            logger.info("client={} send sms,mobile={}", clientIp, mobile);
            logger.info("forwardedFor={},mobile={}, agent={},realip={}", forwardedFor, mobile, agent, real);
        } catch (Exception e) {
            logger.error("ex", e);
        }
        if (StringUtils.isBlank(mobile)) {

            return ErrorResult.create(10001, "手机号不存在");
        }
        UserDto byMobile = userService.findByMobile(mobile);
        if (null == byMobile) {
            return ErrorResult.create(10001, "手机号不存在");
        }
        userService.sendCaptcha(mobile, clientIp, false);

        sendEvent.send(UserGetCaptchaContext.get());
        return SuccessMessage.create("发送验证码成功");
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
     * 意见反馈回复
     *
     * @param id
     * @param content
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/reply", method = RequestMethod.POST)
    public int reply(@RequestParam int id,
                     @RequestParam String content,
                     @RequestParam String title,
                     @RequestParam String modifier) throws BizException {

        return feedbackService.reply(id, content, title, modifier);
    }


    /**
     * 意见反馈回复
     *
     * @param id
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/reply", method = RequestMethod.GET)
    public List<UserMessage> reply(@RequestParam int id) throws BizException {
        return feedbackService.getReply(id);
    }

    /**
     * 检测接口
     */
    @RequestMapping(value = "check", method = RequestMethod.GET)
    public Object checkStatus(@RequestParam(defaultValue = "-1") int source,
                              @RequestHeader String cv,
                              @RequestHeader int terminal,
                              @RequestHeader(defaultValue = "") String system,
                              @RequestHeader(defaultValue = "") String device,
                              @RequestHeader(required = false) String token) throws BizException {

        String checkKey = new StringBuffer().append("check:cache").append(":").append(terminal).append(":").append(cv).toString();
        logger.info("checkInfoCache key:{}", checkKey);
        CheckBean checkBean = checkInfoCache.getIfPresent(checkKey);
        if (null == checkBean) {
            //默认用户ID=-1,版本更新跟用户ID无关
            AppVersionBean version = appVersionService.findNewVersion(UserService.DEFAULT_USER_ID, terminal, cv);
            checkBean = CheckBean.builder()
                    .appVersionBean(version)
                    .audit(false)
                    .commentStatus(0)
                    .aboutEmail("fkzhuantiku@163.com")
                    .aboutPhone("400-8989-766")
                    .coursePhone("400-8989-766")
                    .seckillUrl(userServerConfig.getSeckillUrl())
                    .essayCorrectFree(userService.correctFree())//申论批改是否免费
                    /**
                     * 答题开关，不同客户端分开控制（2018-03-27）
                     */
                    .photoAnswer(userService.photoAnswerV2(terminal, cv))//是否支持拍照答题（0支持  1不支持）
                    .voiceAnswer(userService.voiceAnswerV2(terminal, cv))//是否支持语音答题（0支持  1不支持）
                    .photoAnswerType(userService.photoAnswerType(terminal))//拍照识别对接第三方（0  汉王  1优图）
                    .photoAnswerMsg((null == userService.getPhotoAnswerMsg()) ? "" : userService.getPhotoAnswerMsg())
                    .build();

            checkInfoCache.put(checkKey, checkBean);
            //收集用户设备信息,主要存储设备-操作系统信息,不必实时上报
            userSystemControllerUtil.storeDeviceInfo(device, system, userSessionService.getMobileNo(token));
            logger.info("storeDeviceInfo不实时上报");
        }

        //判断ios审核状态
        long userId = userSessionService.getUid(token);
        if (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD) {
            Boolean isAudit = judgeIosIsAudit(userId, terminal, cv);
            checkBean.setAudit(isAudit);
            logger.info("checkBean isAudit:{}", isAudit);
        }

        //判断审核版本白名单
        boolean audit = appVersionService.isAudit(userId);
        if (audit) {
            checkBean.getAppVersionBean().setUpdate(true);
        }
        mqService.send(token + "," + terminal);
        //是否是广告白名单
        int fur = isWhiteUser(userId);
        checkBean.setFur(fur);
        if (fur == 1) {
            checkBean.getAppVersionBean().setIsPatch(true);
            checkBean.getAppVersionBean().setPatchUrl("http://tiku.huatu.com/cdn/pandora/img/huatuv2v127.patch");
            checkBean.getAppVersionBean().setPatchMd5("59acdc7298b2ddd92e7f640dcbe7ccc8");
        }
        if((terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD) && cv.equals("7.2.121")){
            checkBean.getAppVersionBean().setUpdate(true);
        }
        return checkBean;
    }


    /**
     * 课程服务获取当前 ios 是否为审核版本
     *
     * @param cv
     * @return
     */
    @RequestMapping(value = "check/audit", method = RequestMethod.GET)
    public Object checkIosIsAudit(@RequestParam(value = "cv") String cv) {
        return userService.isIosAudit(cv);
    }

    /**
     * 根据用户判断ios审核状态
     *
     * @param userId
     * @param terminal
     * @param cv
     */
    public Boolean judgeIosIsAudit(Long userId, int terminal, String cv) {
        Boolean isAudit = false;
        final ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String auditListRedis = valueOperations.get("auditList");
        logger.info("userId={},auditListRedis={}", userId, auditListRedis);
        if (StringUtils.isNotBlank(auditListRedis) && auditListRedis.contains(userId + "")) {
            isAudit = true;
            logger.info("auditList,isAudit=true");
        } //有白名单一定是内购状态
        else {
            //因为isIosAudit方法中 categroy实质上未使用到,所以可以不传递,这里给默认公务员,兼容旧接口
            isAudit = userService.isIosAudit(CatgoryType.GONG_WU_YUAN, terminal, cv);
            logger.info("!auditList,isAudit={}", isAudit);
        }
        return isAudit;
    }


    public int isWhiteUser(Long userId) {
        final ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String userIds = valueOperations.get("picList");
        if (StringUtils.isNotEmpty(userIds) && userIds.contains(userId + "")) {
            return 1;
        }
        return 0;
    }

    @RequestMapping(value = "/feedback/reply/content", method = RequestMethod.POST)
    public Object getFeedBackContent(@RequestBody List<Integer> feedBackIds) throws BizException {
        if (CollectionUtils.isEmpty(feedBackIds)) {
            return Lists.newArrayList();
        }
        logger.info("意见反馈参数是:{}", JsonUtil.toJson(feedBackIds));
        List<FeedBackContentDto> batchUserReply = feedbackService.getBatchUserReply(feedBackIds);
        return batchUserReply;
    }


}
