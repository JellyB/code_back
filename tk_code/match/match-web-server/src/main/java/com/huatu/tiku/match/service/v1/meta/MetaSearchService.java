package com.huatu.tiku.match.service.v1.meta;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.question.bean.Question;

/**
 * 统计信息查询并构建返回对象
 * Created by huangqingpeng on 2019/1/9.
 */
public interface MetaSearchService {

    /**
     * 查询报告需要的统计数据填充
     *
     * @param answerCard
     */
    void handlerStandAnswerCard(StandardCard answerCard,String cv,int terminal) throws BizException;

    /**
     * 试题统计信息添加--整体统计信息（不是用户统计信息）
     *
     * @param question
     */
    void handlerQuestionMeta(Question question);



//    void handlerTotalAnswerCard();
}
