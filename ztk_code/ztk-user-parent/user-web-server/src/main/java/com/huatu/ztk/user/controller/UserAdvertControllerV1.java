package com.huatu.ztk.user.controller;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.collect.Maps;
import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.user.bean.Activity;
import com.huatu.ztk.user.bean.Advert;
import com.huatu.ztk.user.common.UserRedisKeys;
import com.huatu.ztk.user.service.*;
import com.huatu.ztk.user.util.UserTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 广告控制层
 * Created by shaojieyue
 * Created time 2016-06-27 10:01
 */
@Slf4j
@RestController
@RequestMapping(value = "/v1/users/bc",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UserAdvertControllerV1 {
    private static final Logger logger = LoggerFactory.getLogger(UserAdvertControllerV1.class);

    //服务资源地址
    public static final String SERVER_SESOURCES = System.getProperty("server_resources");

    @Autowired
    private ActivityService activityService;

    @Autowired
    private UserSessionService userSessionService;

    @Resource(name = "redisTemplate")
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private AdvertService advertService;

    @Autowired
    private UserService userService;
    @Autowired
    private NewAdvertService service;

    /**
     * 手机启动页广告列表
     * @return
     */
    @RequestMapping(value = "/launch")
    public Object launch(){
        //TODO 此处目前只是方便测试,后期要调整
        List<Advert> advertList = new ArrayList<>();
//        for (int i = 0; i < RandomUtils.nextInt(1, 7); i++) {
//            Advert advert = Advert.builder()
//                    .image("http://fb.fbcontent.cn/api/ape-images/1558a460f6405c8.png")
//                    .link("https://www.sogou.com/")
//                    .build();
//            advertList.add(advert);
//            if (RandomUtils.nextInt(1, 3) == 1) {
//                advert.setLink(null);
//            }
//        }
        Map data = new HashMap();
        //随机模拟指定的
        if (RandomUtils.nextInt(1, 3) ==1) {
            data.put("special",RandomUtils.nextInt(0,advertList.size()));
        }else {
            data.put("special",-1);
        }

        data.put("adverts",advertList);
        return data;
    }

    /**
     * 手机首页广告列表
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/list")
    public Object list(@RequestHeader int terminal, @RequestHeader String cv){

        if (userService.isIosAudit(CatgoryType.GONG_WU_YUAN,terminal,cv)) {
            return new ArrayList<>();
        }

        final List<Advert> adverts = advertService.queryMobileAdverts();

        return adverts;
    }

    /**
     * 广告详情页
     * @param id 广告id
     * @return
     */
    @RequestMapping(value = "{id}/detail",method = RequestMethod.GET,produces = MediaType.TEXT_HTML_VALUE+ ";charset=UTF-8")
    public Object detal(@PathVariable long id){
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(SERVER_SESOURCES+"/webapp/pages/advert_detail.mustache");

        StringWriter stringWriter = new StringWriter();
        Advert advert = advertService.findById(id);
        Map data = Maps.newHashMap();
        data.put("advert",advert);
        if (advert != null) {//广告有可能为null
            data.put("onlineTime", DateFormatUtils.format(advert.getCreateTime(),"yyyy-MM-dd HH:mm:ss"));
        }

        try {
            mustache.execute(stringWriter, data).flush();
        } catch (IOException e) {
            logger.error("render html fail.",e);
        }

        return stringWriter.getBuffer().toString();
    }




    /**
     * 活动列表接口
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "actlist", method = RequestMethod.GET)
    public Object activity(@RequestHeader(required = false) String token) throws BizException {
        userSessionService.assertSession(token);
        final List<Activity> activities = activityService.queryMobileActivitys();

        long userId = userSessionService.getUid(token);

        //把用户看活动的时间缓存
        String key = String.format(UserRedisKeys.USER_ACT_READ,userId);
        redisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()));

        return activities;
    }


    /**
     * 获得活动红点个数
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "actCount", method = RequestMethod.GET)
    public Object activity2(@RequestHeader(required = false) String token) throws BizException {
        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);

        Map data = Maps.newHashMap();
        data.put("unreadActCount", activityService.getUnReadActCount(userId));
        return data;
    }

    /**
     * 活动点击量增加接口
     * @param aid 活动id
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "pvadd", method = RequestMethod.PUT)
    public Object activityCountAdd(@RequestParam long aid) throws BizException {
        activityService.pvadd(aid);
        return SuccessMessage.create("操作成功");
    }

    /**
     * 好评送课
     * @param token
     * @param terminal
     * @param cv
     * @throws BizException
     */
    @RequestMapping(value = "course", method = RequestMethod.POST)
    public Object sendFreeCourse(@RequestHeader(required = false) String token,
                                 @RequestHeader int terminal,
                                 @RequestParam(defaultValue = "-1") int categoryId,
                                 @RequestHeader(defaultValue = "-1") int category,
                                 @RequestHeader String cv) throws BizException{
        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);
        String username = userSessionService.getUname(token);
        int catgory = UserTokenUtil.getHeaderSubject(token,userSessionService::getCatgory,categoryId,category);

        String courseName = activityService.sendCommentCourse(userId, username, terminal, cv, catgory);
        Map data = new HashMap<>();
        data.put("courseName", courseName);
        return data;
    }
}
