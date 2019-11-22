package com.huatu.ztk.paper.controller;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.paper.common.MatchRedisKeys;
import com.huatu.ztk.user.service.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * @author jbzm
 * @date Create on 2018/3/2 17:43
 */
@RestController
@RequestMapping("redis")
public class FindRedisController {
    static final String matchWhiteKey = "match_white_key_2_0_1_8";
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserSessionService userSessionService;

    @RequestMapping(value = "findMatchWhite")
    @ResponseBody
    public Object findMatchWhite(@RequestHeader(required = false) String token) throws BizException {
        userSessionService.assertSession(token);
        //用户id
        final long uid = userSessionService.getUid(token);
        Set allMatchWhite = redisTemplate.opsForSet().members(matchWhiteKey);
        if (token == null) {
            return String.valueOf(allMatchWhite);
        }
        Boolean member = redisTemplate.opsForSet().isMember(matchWhiteKey, String.valueOf(uid));
        return member;
    }
}
