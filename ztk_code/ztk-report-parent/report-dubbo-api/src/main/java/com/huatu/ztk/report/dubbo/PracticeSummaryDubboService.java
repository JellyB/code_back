package com.huatu.ztk.report.dubbo;

import com.huatu.ztk.report.bean.PracticeSummary;

/**
 * 练习统计dubbo 接口
 * Created by shaojieyue
 * Created time 2016-09-19 16:34
 */
public interface PracticeSummaryDubboService {

    /**
     * 通过用户id查询我的统计
     * @param uid 用户id
     * @param subject 科目
     * @return
     */
    PracticeSummary findByUid(long uid, int subject);
}
