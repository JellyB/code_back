package com.huatu.ztk.pc.service;

import com.huatu.ztk.pc.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by linkang on 2017/10/13 下午2:29
 */
public class ShareRewardServiceTest extends BaseTest{
    @Autowired
    private ShareRewardService shareRewardService;


    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    public void sendShareMsg() throws Exception {
        shareRewardService.sendShareMsg(1234L, "heheh", "shareId124");
    }

}