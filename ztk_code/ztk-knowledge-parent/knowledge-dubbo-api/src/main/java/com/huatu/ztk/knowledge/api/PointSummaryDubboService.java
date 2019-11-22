package com.huatu.ztk.knowledge.api;


import com.huatu.ztk.knowledge.bean.PointSummary;

import java.util.List;

/**
 * 知识点汇总dubbo服务
 * Created by shaojieyue
 * Created time 2016-06-16 14:12
 */
public interface PointSummaryDubboService {

    /**
     * 查询用户指定科目下的知识点汇总
     * @param uid 用户id
     * @param subject 考试科目
     * @return
     */
    public List<PointSummary> findUserPointSummary(long uid, int subject);

    /**
     * 查找用户知识点汇总
     * 如果该知识点还不存在汇总,则创建一个
     * @param uid
     * @param subject
     * @param point
     * @return
     */
    public PointSummary find(long uid, int subject, int point);
}
