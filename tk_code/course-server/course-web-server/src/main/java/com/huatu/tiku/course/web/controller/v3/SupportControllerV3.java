package com.huatu.tiku.course.web.controller.v3;

import com.google.common.collect.Maps;
import com.huatu.tiku.course.netschool.api.v3.SupportServiceV3;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.springboot.users.support.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author hanchao
 * @date 2017/9/14 15:39
 */
@RestController
@RequestMapping("/v3/support")
public class SupportControllerV3 {

    @Autowired
    private SupportServiceV3 supportServiceV3;

    /**
     * 课程签到接口
     * @param lessionid
     * @param netclassid
     * @param userSession
     * @return
     */
    @PostMapping("/course/checkin")
    public Object checkin(@RequestParam int lessionid,
                          @RequestParam int netclassid,
                          @Token UserSession userSession) {
        Map<String,Object> params = Maps.newHashMap();
        params.put("action","checkin");
        params.put("lessionid",lessionid);
        params.put("netclassid",netclassid);
        params.put("username",userSession.getUname());
        return ResponseUtil.build(supportServiceV3.checkInOut(params));
    }

    /**
     * 课程签退接口
     * @param lessionid
     * @param userSession
     * @return
     */
    @PostMapping("/course/checkout")
    public Object checkout(@RequestParam int lessionid,
                          @RequestParam int netclassid,
                          @Token UserSession userSession) {
        Map<String,Object> params = Maps.newHashMap();
        params.put("action","checkout ");
        params.put("netclassid",netclassid);
        params.put("lessionid",lessionid);
        params.put("username",userSession.getUname());
        return ResponseUtil.build(supportServiceV3.checkInOut(params));
    }
}
