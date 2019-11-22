package com.huatu.ztk.report.bean;

import com.huatu.ztk.knowledge.api.PointSummaryDubboService;
import com.huatu.ztk.knowledge.bean.PointSummary;
import com.huatu.ztk.report.BaseTest;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-06-16 14:26
 */
public class PointSummaryDubboServiceImplTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(PointSummaryDubboServiceImplTest.class);

    @Autowired
    private PointSummaryDubboService pointSummaryDubboService;

    @Test
    public void findUserPointSummaryTest(){
        long uid = 12252065;
        int subject = 1;
        final List<PointSummary> userPointSummary = pointSummaryDubboService.findUserPointSummary(uid, subject);
        logger.info("userPointSummary.size={}",userPointSummary.size());
        Assert.assertTrue(userPointSummary.size()>0);
    }
}
