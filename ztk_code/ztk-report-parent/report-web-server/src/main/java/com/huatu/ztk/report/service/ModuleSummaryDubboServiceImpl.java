package com.huatu.ztk.report.service;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.Module;
import com.huatu.ztk.knowledge.api.ModuleDubboService;
import com.huatu.ztk.knowledge.api.PointSummaryDubboService;
import com.huatu.ztk.knowledge.bean.PointSummary;
import com.huatu.ztk.report.bean.ModuleSummary;
import com.huatu.ztk.report.dubbo.ModuleSummaryDubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-09-19 21:31
 */
public class ModuleSummaryDubboServiceImpl implements ModuleSummaryDubboService {
    private static final Logger logger = LoggerFactory.getLogger(ModuleSummaryDubboServiceImpl.class);

    @Autowired
    private PointSummaryDubboService pointSummaryDubboService;

    @Autowired
    private ModuleDubboService moduleDubboService;

    /**
     * 查询个人能力汇总
     *
     * @param uid
     * @param subject
     * @return
     */
    @Override
    public List<ModuleSummary> find(long uid, int subject) {
        List<ModuleSummary> moduleSummaries = new ArrayList<>();

        //遍历科目下所有模块
        for (Module module : moduleDubboService.findSubjectModules(subject)) {
            //查询用户知识点汇总信息（知识点的一级节点就是模块）

            final PointSummary pointSummary = pointSummaryDubboService.find(uid, subject, module.getId());

            int score = 0;
            if (pointSummary.getAcount()>0) {//>0 则计算预测分
                //预测分=（作对题数/做的总题量）×100
                score = new BigDecimal(pointSummary.getRcount())
                        .divide(new BigDecimal(pointSummary.getAcount()), 2, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100)).intValue();
            }
            final ModuleSummary moduleSummary = ModuleSummary.builder().moduleId(pointSummary.getPointId())
                    .moduleName(module.getName())
                    .score(score)//预测分
                    .subject(subject)
                    .acount(pointSummary.getAcount())
                    .rcount(pointSummary.getRcount())
                    .wrong(pointSummary.getWcount())
                    .uid(uid)
                    .build();
            moduleSummaries.add(moduleSummary);
        }

        return moduleSummaries;
    }
}
