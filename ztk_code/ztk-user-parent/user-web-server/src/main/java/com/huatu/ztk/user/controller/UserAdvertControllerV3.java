package com.huatu.ztk.user.controller;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.huatu.ztk.commons.CatgoryType;
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


@RestController
@RequestMapping(value = "/v3/users/bc", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UserAdvertControllerV3 {

    private static final Logger logger = LoggerFactory.getLogger(UserAdvertControllerV3.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AdvertMessageService advertMessageService;

    @Autowired
    private UserSessionService userSessionService;
    @Resource(name = "redisTemplate")
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private AdvertMessageDao advertMessageDao;

    //知识点的缓存
    Cache<Integer, List<Message>> LOGO_CACHE =
            CacheBuilder.newBuilder()
                    .maximumSize(100)
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .build();

    /**
     * 查询首页弹出广告图列表
     *
     * @return
     */
    @RequestMapping(value = "/popup", method = RequestMethod.GET)
    public Object popup(@RequestHeader int terminal,
                        @RequestHeader String cv,
                        @RequestHeader String token,
                        @RequestParam(defaultValue = "-1") int categoryId,
                        @RequestHeader(defaultValue = "-1") int category,
                        @RequestHeader(defaultValue = "2") int appType) throws BizException {
        userSessionService.assertSession(token);
        final int catgory = UserTokenUtil.getHeaderSubject(token, userSessionService::getCatgory, categoryId, category);
        long uid = userSessionService.getUid(token);
        //ios审核需要使用
        //修改：无论是否是审核状态,都开启轮播图
       /* if (userService.isIosAudit(catgory, terminal, cv)) {
            return new ArrayList<>();
        }*/
        List<Message> result = advertMessageService.findNewPopupList(catgory, uid, appType);
        //如果是IOS 6.0 新版本，轮播图模考大赛应该是estimatePaper/home  替换
        if (cv != null && cv.contains("6.0") && (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD)) {
            MessageUtil.dealMessage(result);

        }
        return result;
    }

    /**
     * 公告
     * 补充：app2018年升级之后,已经不使用此广告功能
     *
     * @return
     */
    @RequestMapping(value = "/notice", method = RequestMethod.GET)
    public Object launch(@RequestHeader int terminal,
                         @RequestHeader String cv,
                         @RequestParam(defaultValue = CatgoryType.GONG_WU_YUAN + "") int catgory,
                         @RequestHeader(required = false) String token,
                         @RequestHeader(defaultValue = "2") int appType) throws BizException {
        return Lists.newArrayList();
//        userSessionService.assertSession(token);
//        //ios审核需要使用
//        if (userService.isIosAudit(catgory, terminal, cv)) {
//            return new ArrayList<>();
//        }
//
//        long uid = userSessionService.getUid(token);
//
//        return advertMessageService.findNoticeList(-1, uid, appType);
    }

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
                       @RequestHeader(defaultValue = "2") int appType,
                       @RequestParam(defaultValue = "-1") int categoryId,
                       @RequestHeader(defaultValue = "-1") int category) {

        final int finalCategory = UserTokenUtil.getHeaderSubject(token, userSessionService::getCatgory, categoryId, category);

        //修改：无论是否是审核状态,都开启轮播图

       /* if (!userService.isIosAuditAd(category, terminal, cv)) {
            return Collections.EMPTY_LIST;
        }*/
        long uid = userSessionService.getUid(token);
        List<Message> result = advertMessageService.findBannerListV3(finalCategory, terminal, cv, uid, appType);
        /*    轮播图白名单  */
        MessageUtil.filterMessage(redisTemplate, uid, advertMessageDao, advertMessageService, result);
        return result;
    }


}
