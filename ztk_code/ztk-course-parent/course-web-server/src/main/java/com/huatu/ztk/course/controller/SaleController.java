package com.huatu.ztk.course.controller;

import com.google.common.collect.Maps;
import com.huatu.ztk.course.service.SaleService;
import com.huatu.ztk.user.service.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * 支付
 * Created by linkang on 11/30/16.
 */
@RestController
@RequestMapping(value = "v1/sales")
public class SaleController {

    @Autowired
    private SaleService saleService;

    @Autowired
    private UserSessionService userSessionService;

    /**
     * 支付详情接口
     * @param courseId 课程id
     * @param token
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "detail", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object saleDetail(@RequestParam int courseId,
                             @RequestHeader(required = false) String token,
                             @RequestHeader String cv,
                             @RequestHeader int terminal) throws Exception {
        userSessionService.assertSession(token);
        String uname = userSessionService.getUname(token);
        int catgory = userSessionService.getCatgory(token);

        final HashMap<String, Object> parameterMap = Maps.newHashMap();
        parameterMap.put("username", uname);
        parameterMap.put("rid", courseId);
        return saleService.findDetail(parameterMap,cv,terminal,catgory);
    }
}
