package com.huatu.ztk.paper.controller.v3;

import com.huatu.ztk.paper.controller.BaseController;
import com.huatu.ztk.user.service.UserSessionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping(value = "/v3/truePaperVersion", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class TruePaperVersionControllerV3 extends BaseController {

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(value = "getRootVersion",method = RequestMethod.GET)
    public Object getRootVersion(
            @RequestHeader(required = false) String token,
            @RequestParam(defaultValue = "-1") int subject
    ){
        
        if (subject < 0) {
            subject = userSessionService.getSubject(token);
        }
        ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
        String version = valueOperations.get("new:true:paper:root:version:" + subject);
        HashMap<String, String> map = new HashMap<>();
        map.put("version",StringUtils.isBlank(version) ? "-1" : version);
        return map;
    }
}
