package com.huatu.ztk.paper.service.v4;

import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.paper.enums.ScoreSortEnum;

/**
 * Created by huangqingpeng on 2019/3/7.
 */
public interface ScoreSortService {

    /**
     * 分数存储
     * @param answerCard
     * @param scoreSubmitSort
     */
    void addScoreSort(StandardCard answerCard, ScoreSortEnum scoreSubmitSort);

    /**
     * 重新做排序规则
     * @param answerCard        答题卡ID
     * @param scoreSubmitSort   排序方式
     */
    void reSort(AnswerCard answerCard, ScoreSortEnum scoreSubmitSort);
}
