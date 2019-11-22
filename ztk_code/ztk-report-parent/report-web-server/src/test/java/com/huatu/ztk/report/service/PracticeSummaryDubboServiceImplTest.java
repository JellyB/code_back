package com.huatu.ztk.report.service;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.report.BaseTest;
import com.huatu.ztk.report.bean.PracticeSummary;
import com.huatu.ztk.report.dubbo.PracticeSummaryDubboService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by shaojieyue
 * Created time 2016-09-19 16:52
 */
public class PracticeSummaryDubboServiceImplTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(PracticeSummaryDubboServiceImplTest.class);

    @Autowired
    private PracticeSummaryDubboService practiceSummaryDubboService;

    @Test
    public void findByUidTest(){
        long uid = 12252065;
        int subject = 1;
        int area = 1933;
        final PracticeSummary byUid = practiceSummaryDubboService.findByUid(uid, subject);
        System.out.println(JsonUtil.toJson(byUid));
    }
}
