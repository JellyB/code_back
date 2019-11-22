package com.huatu.tiku.match.service.v1.meta;

import com.huatu.tiku.match.dto.enroll.EnrollDTO;

/**
 * 异步处理统计信息入库
 * Created by huangqingpeng on 2019/1/9.
 */
public interface MetaHandlerService {

    /**
     * 报名信息写入统计表
     *
     * @param enrollDTO
     */
    void saveEnrollInfo(EnrollDTO enrollDTO);

    /**
     * 答题卡ID写入统计表
     *
     * @param practiceId-
     */
    void savePracticeId(long practiceId);

    /**
     * 分数写入统计表
     *
     * @param practiceId
     */
    void saveScore(long practiceId);

}
