package com.huatu.tiku.match.service.v1.meta;

import com.huatu.tiku.match.bean.entity.MatchQuestionMeta;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.question.bean.QuestionMeta;
import service.BaseServiceHelper;

import java.util.List;
import java.util.Map;

/**
 * 模考大赛试题统计信息处理
 * Created by huangqingpeng on 2018/10/16.
 */
public interface MatchQuestionMetaService extends BaseServiceHelper<MatchQuestionMeta> {

    /**
     * 用户答题情况生成知识树统计信息（报告中使用）
     * @param questions
     * @param corrects
     * @param times
     * @return
     */
    List<QuestionPointTree> questionPointSummaryWithTotalNumber(List<Integer> questions, int[] corrects, int[] times);

    /**
     * 统计某个单题卡的答题数据到缓存中(用户交卷答题卡状态置为完成之后调用)
     * 持久化到表的操作不在这里（同步定时实现）
     * @param answerCard
     */
    void handlerQuestionMeta(AnswerCard answerCard);

    /**
     * 获得试题的统计数据
     * @param questionId
     * @return
     */
    QuestionMeta getQuestionMeta(int questionId);

    /**
     * 获取试卷下的试题统计数据
     * @param paperId
     * @return
     */
    Map<Integer, QuestionMeta> getQuestionMetaByPaperId(int paperId);


    void reCountQuestionMeta();
}
