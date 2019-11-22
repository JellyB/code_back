package com.huatu.ztk.report.dubbo;

import com.huatu.ztk.report.bean.ModuleSummary;

import java.util.List;

/**
 *
 * 模块能力汇总
 * Created by shaojieyue
 * Created time 2016-09-19 21:30
 */
public interface ModuleSummaryDubboService {

    /**
     * 查询个人能力汇总
     * @param uid
     * @param subject
     * @return
     */
    public List<ModuleSummary> find(long uid, int subject);
}
