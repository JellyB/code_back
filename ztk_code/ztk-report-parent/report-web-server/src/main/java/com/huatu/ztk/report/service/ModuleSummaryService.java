package com.huatu.ztk.report.service;

import com.huatu.ztk.commons.Module;
import com.huatu.ztk.commons.ModuleConstants;
import com.huatu.ztk.knowledge.api.PointSummaryDubboService;
import com.huatu.ztk.knowledge.bean.PointSummary;
import com.huatu.ztk.report.bean.ModuleSummary;
import com.huatu.ztk.report.dubbo.ModuleSummaryDubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户模块预测
 * Created by shaojieyue
 * Created time 2016-06-21 10:58
 */

@Service
public class ModuleSummaryService {
    private static final Logger logger = LoggerFactory.getLogger(ModuleSummaryService.class);

    @Autowired
    private ModuleSummaryDubboService moduleSummaryDubboService;

    public List<ModuleSummary> find(long uid, int subject) {
        return moduleSummaryDubboService.find(uid, subject);
    }


}
