package com.huatu.ztk.course.controller;

import com.google.common.collect.Maps;
import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.course.common.NetSchoolConfig;
import com.huatu.ztk.course.service.LogisticsService;
import com.huatu.ztk.user.service.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * 物流
 * Created by linkang on 11/30/16.
 */

@RestController
@RequestMapping(value = "v1/logistics")
public class LogisticsController {

    @Autowired
    private LogisticsService logisticsService;

    @Autowired
    private UserSessionService userSessionService;

    /**
     * 物流列表
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object logistics(@RequestHeader(required = false) String token) throws Exception {
        userSessionService.assertSession(token);
        String username = userSessionService.getUname(token);
        int catgory = userSessionService.getCatgory(token);

        final HashMap<String, Object> parameterMap = Maps.newHashMap();
        parameterMap.put("username", username);
        parameterMap.put("categoryid", catgory == CatgoryType.GONG_WU_YUAN ?
                NetSchoolConfig.CATEGORY_GWY : NetSchoolConfig.CATEGORY_SHIYE);

        return logisticsService.getList(parameterMap);
    }
}
