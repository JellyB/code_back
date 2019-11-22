package com.huatu.ztk.user.controller;

import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.user.service.AdvertMessageService;
import com.huatu.ztk.user.service.UserService;
import com.huatu.ztk.user.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;

/**
 * @Create zhouwei
 */

@RestController
@RequestMapping(value = "/v4/users/bc", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UserAdvertControllerV4 {

    private static final Logger logger = LoggerFactory.getLogger(UserAdvertControllerV4.class);


    @Autowired
    private UserService userService;

    @Autowired
    private AdvertMessageService advertMessageService;

    @Autowired
    private UserSessionService userSessionService;

    @Resource(name = "redisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 公告
     * 补充：app2018年升级之后,已经不使用此广告功能
     *
     * @return
     */
    @RequestMapping(value = "/notice", method = RequestMethod.GET)
    public Object launch(@RequestHeader int terminal,
                         @RequestHeader String cv,
                         @RequestParam(defaultValue = CatgoryType.GONG_WU_YUAN + "") int category,
                         @RequestHeader(required = false) String token,
                         @RequestHeader(defaultValue = "2") int appType) throws BizException {
        return new ArrayList<>();

//        userSessionService.assertSession(token);
//
//        //ios审核需要使用
//        if (userService.isIosAudit(category,terminal,cv)) {
//            return new ArrayList<>();
//        }
//        final int sessionCategory = userSessionService.getCatgory(token);
//        category = sessionCategory > 0 ? sessionCategory : category;
//        long uid = userSessionService.getUid(token);
//
//        return advertMessageService.findNoticeList(category,uid, appType);
    }
}
