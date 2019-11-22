package com.huatu.ztk.paper.service;

import com.huatu.ztk.paper.api.PracticeDubboService;
import com.huatu.ztk.paper.bean.PracticePaper;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by shaojieyue
 * Created time 2016-07-05 16:53
 */
public class PracticeDubboServiceTest extends BaseTest{
    private static final Logger logger = LoggerFactory.getLogger(PracticeDubboServiceTest.class);

    @Autowired
    private PracticeDubboService practiceDubboService;

    @Test
    public void createTest(){
        int point = 435;
        int qcount = 20;
        int subject = 1;
        for (int i = 0; i < 100; i++) {
            final PracticePaper practicePaper = practiceDubboService.create(point, qcount,12252065, subject);
            Assert.assertEquals(practicePaper.getQcount(),qcount);
        }
    }

    @Test
    public void createWeixinPaperTest() throws Exception{
        long uid = 7828231;
        int subject = 1;
        int pointId = 392;
        int qcount = 5;

        final PracticePaper practicePaper = practiceDubboService.createWeixinPaper(uid, subject, pointId, qcount);
        Assert.assertNotNull(practicePaper);
        Assert.assertEquals(practicePaper.getQcount(),qcount);
        logger.info("createWeixinPaper method test success");
    }

}
