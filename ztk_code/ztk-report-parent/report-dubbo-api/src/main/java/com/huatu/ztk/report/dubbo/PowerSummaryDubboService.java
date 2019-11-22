package com.huatu.ztk.report.dubbo;

import com.huatu.ztk.report.bean.PowerSummary;

/**
 * 用户能力dubbo 接口
 * Created by shaojieyue
 * Created time 2016-09-19 21:27
 */
public interface PowerSummaryDubboService {

    /**
     * 查询用户能力汇总
     * @param userId 用户id
     * @param subject 科目
     * @param area 区域
     * @return
     */
    public PowerSummary find(long userId, int subject, int area);
}
