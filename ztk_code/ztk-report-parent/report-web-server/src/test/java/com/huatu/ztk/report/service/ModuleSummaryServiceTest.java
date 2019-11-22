package com.huatu.ztk.report.service;

import com.huatu.ztk.report.BaseTest;
import com.huatu.ztk.report.bean.ModuleSummary;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-07-27 15:53
 */
public class ModuleSummaryServiceTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(ModuleSummaryServiceTest.class);

    @Autowired
    private ModuleSummaryService moduleSummaryService;

    @Test
    public void aaTest(){
        final List<ModuleSummary> moduleSummaries = moduleSummaryService.find(13041518, 1);
    }
}
