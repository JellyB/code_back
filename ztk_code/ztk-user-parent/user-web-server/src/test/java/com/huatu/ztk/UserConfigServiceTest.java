package com.huatu.ztk;

import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.user.bean.UserConfig;
import com.huatu.ztk.user.service.UserConfigService;
import com.huatu.ztk.user.service.UserSessionService;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

/**
 * Author: xuhuiqiang
 * Time: 2016-12-21  14:11 .
 */
public class UserConfigServiceTest extends BaseTest{
    private static final Logger logger = LoggerFactory.getLogger(UserConfigServiceTest.class);

    @Autowired
    private UserConfigService userConfigService;
    @Autowired
    private UserSessionService userSessionService;
    @Resource(name = "sessionRedisTemplate")
    private RedisTemplate<String,String> sessionRedisTemplate;

    @Test
    public void getDefaultUserConfigTest(){
        int catgory = CatgoryType.GONG_WU_YUAN;
        UserConfig userConfig = userConfigService.getDefaultUserConfig(catgory);
        Assert.assertNotNull(userConfig);
        Assert.assertEquals(userConfig.getArea(),-9);
        Assert.assertEquals(userConfig.getCategory(), CatgoryType.GONG_WU_YUAN);
        Assert.assertEquals(userConfig.getQcount(),10);
    }

    @Test
    public void findByUidTest(){
        long uid = 13191006;
        UserConfig userConfig = userConfigService.findByUidAndCatgory(uid,1);
        Assert.assertNotNull(userConfig);
        Assert.assertEquals(userConfig.getArea(),-9);
        Assert.assertEquals(userConfig.getSubject(), 1);
        Assert.assertEquals(userConfig.getQcount(),20);
    }

    @Test
    public void saveTest(){
        long uid = 13191006;
        int area = -9;
        int qcount = 20;
        int category = 1;
        int subject = 1;
        UserConfig userConfig = UserConfig.builder()
                .area(area)
                .category(category)
                .qcount(qcount)
                .subject(subject)
                .uid(uid)
                .build();
        System.out.println("保存前："+userConfig);
        userConfigService.save(userConfig);

        userConfig = userConfigService.findByUidAndCatgory(uid,1);
        System.out.println("保存后："+userConfig);
        Assert.assertNotNull(userConfig);
        Assert.assertEquals(area,userConfig.getArea());
        Assert.assertEquals(qcount,userConfig.getQcount());
        Assert.assertEquals(category,userConfig.getCategory());
        Assert.assertEquals(subject,userConfig.getSubject());
    }
}
