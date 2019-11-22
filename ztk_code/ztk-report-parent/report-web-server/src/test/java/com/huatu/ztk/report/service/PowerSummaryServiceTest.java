package com.huatu.ztk.report.service;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.report.BaseTest;
import com.huatu.ztk.report.bean.PowerSummary;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created by shaojieyue
 * Created time 2016-07-27 19:22
 */
public class PowerSummaryServiceTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(PowerSummaryServiceTest.class);

    @Autowired
    private PowerSummaryService powerSummaryService;

    @Test
    public void findTest(){
        final PowerSummary powerSummary = powerSummaryService.find(13041518, 1, -9);
        System.out.println(JsonUtil.toJson(powerSummary));

        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
