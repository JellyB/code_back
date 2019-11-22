package com.huatu.tiku.teacher.service;

import com.huatu.ztk.paper.bean.Paper;

/**
 * Created by huangqp on 2018\7\6 0006.
 */
public interface SyncPaperService {
    /**
     * 试卷从mongo迁移到mysql中
     *
     * @param id
     */
    void syncPaper(Integer id);

    /**
     * 试卷从mongo迁移到mysql中
     * 策略：只迁移试卷基础数据和活动基础数据
     * 实体试卷和活动卷的id均为paperId
     *
     * @param id
     */
    void syncPaperSingle(Integer id);

    /**
     * 从mongo中查询试卷信息
     *
     * @param id
     * @return
     */
    Paper findPaperById(Integer id);

    /**
     * 查询试卷详细信息（试卷名+模块名+试题信息）
     *
     * @param paperId
     * @return
     */
    Object findPaperDetail(Integer paperId);

    /**
     * 根据实体卷ID创建活动信息
     * @param paperId
     */
     void createActivityByPaperId(Long paperId) ;

    /**
     * 将一张试题卷同步为活动卷(针对事业单位同步试卷)
     */
    void syncPaperEntityToPaperActivity() ;
}
