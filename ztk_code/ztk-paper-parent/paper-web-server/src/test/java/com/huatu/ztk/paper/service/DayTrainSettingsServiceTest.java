package com.huatu.ztk.paper.service;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.paper.bean.DayTrainSettings;
import com.self.generator.core.WaitException;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;

/**
 * Created by shaojieyue
 * Created time 2016-05-20 17:49
 */
public class DayTrainSettingsServiceTest extends BaseTest{
    private static final Logger logger = LoggerFactory.getLogger(DayTrainSettingsServiceTest.class);

    @Autowired
    private DayTrainSettingsService dayTrainSettingsService;
    long userId = 12346;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;


    @Test
    public void test(){
        redisTemplate.opsForList().leftPushAll("test",Lists.<String>newArrayList("1","2","3","4","5"));
    }


    @Test
    public void creatTest() throws WaitException {
        final DayTrainSettings a = dayTrainSettingsService.create(userId,1);
        Assert.assertNotNull(a);
        final DayTrainSettings b = dayTrainSettingsService.create(userId,1);
        Assert.assertEquals(a.getId(),b.getId());

    }

    @Test
    public void findByUserIdTest() throws WaitException {
        DayTrainSettings dayTrainSettings = dayTrainSettingsService.findByUserId(userId,1);
        Assert.assertNotNull(dayTrainSettings);
        dayTrainSettings = dayTrainSettingsService.findByUserId(12,1);
        Assert.assertNull(dayTrainSettings);
    }

    @Test
    public void updateTest() throws WaitException {
        final DayTrainSettings dayTrainSettings = dayTrainSettingsService.findByUserId(userId,1);
        dayTrainSettings.setSelects(Lists.<Integer>newArrayList(642, 392, 435, 482, 754,642, 392, 435,123,234));
        dayTrainSettings.setNumber(8);
        DayTrainSettings update = null;
        try {
            update = dayTrainSettingsService.update(dayTrainSettings, userId,1);
        } catch (BizException e) {
            e.printStackTrace();
        }
        final ArrayList<Integer> list = Lists.newArrayList(642, 392, 435, 482, 754);
        final boolean equalCollection = CollectionUtils.isEqualCollection(list, update.getSelects());

        Assert.assertEquals(true,equalCollection);
        Assert.assertEquals(update.getNumber(),8);

        dayTrainSettings.setNumber(13);
        try {
            update = dayTrainSettingsService.update(dayTrainSettings, userId,1);
        } catch (BizException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(update.getNumber(),10);

        try {
            dayTrainSettings.setUserId(11111);
            update = dayTrainSettingsService.update(dayTrainSettings, userId,1);
        } catch (BizException e) {
            Assert.assertEquals(CommonErrors.PERMISSION_DENIED.getCode(),e.getErrorResult().getCode());
        }




    }
}
