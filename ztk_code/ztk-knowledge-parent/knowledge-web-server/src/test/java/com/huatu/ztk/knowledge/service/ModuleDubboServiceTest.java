package com.huatu.ztk.knowledge.service;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.BaseTest;
import com.huatu.ztk.knowledge.api.ModuleDubboService;
import com.huatu.ztk.knowledge.bean.Module;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by shaojieyue
 * Created time 2016-05-19 13:48
 */

public class ModuleDubboServiceTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(ModuleDubboServiceTest.class);

    @Autowired
    private ModuleDubboService moduleDubboService;

    @Test
    public void findByPointIdTest(){
        Module module = moduleDubboService.findByPointId(123);
        logger.info(JsonUtil.toJson(module));
        Assert.assertNull(module);
        module = moduleDubboService.findByPointId(642);
        logger.info(JsonUtil.toJson(module));
        Assert.assertEquals(642,module.getCategory());
        module = moduleDubboService.findByPointId(393);
        logger.info(JsonUtil.toJson(module));
        Assert.assertEquals(392,module.getCategory());
        module = moduleDubboService.findByPointId(395);
        logger.info(JsonUtil.toJson(module));
        Assert.assertEquals(392,module.getCategory());
        module = moduleDubboService.findByPointId(445);
        logger.info(JsonUtil.toJson(module));
        Assert.assertEquals(435,module.getCategory());

    }

}
