package com.huatu.tiku.match.service.v1.paper;

import com.huatu.ztk.paper.bean.AnswerCard;

import java.util.List;

/**
 * Created by lijun on 2019/1/15
 */
public interface AnswerCardDBService {

    /**
     * 根据ID查询答题卡新
     */
    AnswerCard findById(Long practiceId);

    /**
     * 根据ID查询答题卡新
     */
    List<AnswerCard> findById(List<Long> idList);

    /**
     * 保存答题卡信息
     */
    void save(AnswerCard answerCard);

    /**
     * 保存答题卡信息 并刷新至实体库
     */
    void saveToDB(AnswerCard answerCard);
}
