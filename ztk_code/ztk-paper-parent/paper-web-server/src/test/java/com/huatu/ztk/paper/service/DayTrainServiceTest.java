package com.huatu.ztk.paper.service;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.DayTrain;
import com.self.generator.core.WaitException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by renwenlong on 2016/10/11.
 */
public class DayTrainServiceTest  extends BaseTest{
    private static final Logger logger = LoggerFactory.getLogger(DayTrainService.class);

    @Autowired
    private DayTrainService dayTrainService;

    final int userId = 10476537;
    @Test
    public void testCreate() throws WaitException, BizException {
        DayTrain dayTrain = dayTrainService.create(userId,1);
        //该测试账号设置的练习次数是5
        Assert.assertNotNull(dayTrain);
        Assert.assertEquals(dayTrain.getAllCount(),5);
        Assert.assertEquals(dayTrain.getPoints().size(),5);
        logger.info("dayTrain={}", JsonUtil.toJson(dayTrain));//验证出5个3级知识点分散在不同的顶级知识点中

    }
}