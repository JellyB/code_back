package com.huatu.ztk.knowledge.service;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.ModuleConstants;
import com.huatu.ztk.knowledge.BaseTest;
import com.huatu.ztk.knowledge.api.PointSummaryDubboService;
import com.huatu.ztk.knowledge.bean.PointSummary;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by shaojieyue
 * Created time 2016-07-27 14:00
 */
public class PointSummaryDubboServiceTest extends BaseTest{
    private static final Logger logger = LoggerFactory.getLogger(PointSummaryDubboServiceTest.class);

    @Autowired
    private PointSummaryDubboService pointSummaryDubboService;
    final int uid = 11873660;
    final int subject = 1;

    @Test
    public void aaTest() {
        List<Integer> moduleIds = ModuleConstants.getModulesBySubject(subject).stream().map(m -> m.getId()).collect(Collectors.toList());

        for (Integer moduleId : moduleIds) {
            PointSummary pointSummary = pointSummaryDubboService.find(uid, subject, moduleId);
            System.out.println(JsonUtil.toJson(pointSummary));
        }

    }
}
