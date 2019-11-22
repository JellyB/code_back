package com.huatu.ztk.user.controller;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.huatu.tiku.common.AdvertEnum;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.user.bean.Message;
import com.huatu.ztk.user.dao.AdvertMessageDao;
import com.huatu.ztk.user.service.AdvertMessageService;
import com.huatu.ztk.user.service.UserService;
import com.huatu.ztk.user.service.UserSessionService;
import com.huatu.ztk.user.util.UserTokenUtil;
import com.huatu.ztk.user.utils.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * 广告控制层
 * Created by shaojieyue
 * Created time 2016-06-27 10:01
 */

@RestController
@RequestMapping(value = "/v2/users/bc", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UserAdvertControllerV2 {
    private static final Logger logger = LoggerFactory.getLogger(UserAdvertControllerV2.class);

    @Autowired
    private UserService userService;

    @Resource(name = "redisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private AdvertMessageService advertMessageService;

    @Autowired
    private UserSessionService userSessionService;
    @Autowired
    private AdvertMessageDao advertMessageDao;

    /**
     * 查询首页广告轮播图列表 v2
     *
     * @param terminal 终端类型
     * @param cv       版本号
     * @return
     */
    @RequestMapping(value = "/list")
    public Object list(@RequestHeader int terminal,
                       @RequestHeader String cv,
                       @RequestHeader String token,
                       @RequestParam(defaultValue = "-1") int categoryId,
                       @RequestHeader(defaultValue = "-1") int category,
                       @RequestHeader(defaultValue = "2") int appType) {

        final int finalCategory = UserTokenUtil.getHeaderSubject(token, userSessionService::getCatgory, categoryId, category);

        //ios审核需要使用 是否开启轮播图
        if (!userService.isIosAuditAd(finalCategory, terminal, cv)) {
            return Collections.EMPTY_LIST;
        }
        long uid = userSessionService.getUid(token);
        List<Message> result = advertMessageService.findBannerList(finalCategory, terminal, cv, uid, appType);
        MessageUtil.filterEssayForOldVersion(result);
        /*    轮播图白名单  */
        /*    轮播图白名单  */
        MessageUtil.filterMessage(redisTemplate, uid, advertMessageDao, advertMessageService, result);
        return result;
    }

    /**
     * 查询启动广告图列表
     *
     * @return
     */
    @RequestMapping(value = "/launch", method = RequestMethod.GET)
    public Object launch(@RequestHeader int terminal,
                         @RequestHeader String cv,
                         @RequestParam(defaultValue = "-1") int catgory,
                         @RequestParam(defaultValue = "-1") int categoryId,
                         @RequestHeader(defaultValue = "-1") int category,
                         @RequestHeader(defaultValue = "2") int appType,
                         @RequestHeader(required = false) String token) {

        //return new ArrayList<>();
        final int finalCategory = UserTokenUtil.getHeaderSubject(token, userSessionService::getCatgory, catgory, categoryId, category);
        //ios审核需要使用
        if (userService.isIosAudit(category, terminal, cv)) {
            return new ArrayList<>();
        }
        //修改：无论是否是审核状态,都开启轮播图
       /* if (userService.isIosAudit(catgory, terminal, cv)) {
            return new ArrayList<>();
        }*/
        long uid = userSessionService.getUid(token);
        //logger.info("appType是:{}", appType);
        List<Message> result = advertMessageService.findLaunchList(finalCategory, uid, AdvertEnum.AppType.HTZX.getCode());
        //如果是IOS 6.0 新版本，轮播图模考大赛应该是estimatePaper/home  替换
        if (cv != null && cv.contains("6.0") && (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD)) {
            MessageUtil.dealMessage(result);
        }
        return result;
    }


    /**
     * 查询首页弹出广告图列表
     *
     * @return
     */
    @RequestMapping(value = "/popup", method = RequestMethod.GET)
    public Object popup(@RequestHeader int terminal,
                        @RequestHeader String cv,
                        @RequestHeader(defaultValue = "2") int appType,
                        @RequestHeader String token,
                        @RequestParam(defaultValue = "-1") int categoryId,
                        @RequestHeader(defaultValue = "-1") int category) throws BizException {
        String downgradeValue = redisTemplate.opsForValue().get("_u:notice:downgrade");
        if ("on".equals(downgradeValue)) {
            return new ArrayList<>();
        }

        userSessionService.assertSession(token);
        final int catgory = UserTokenUtil.getHeaderSubject(token, userSessionService::getCatgory, categoryId, category);
        //ios审核需要使用
        if (userService.isIosAudit(catgory, terminal, cv)) {
            return new ArrayList<>();
        }
        List<Message> result = advertMessageService.findPopupList(catgory, AdvertEnum.AppType.HTZX.getCode());
        return result;
    }
}
