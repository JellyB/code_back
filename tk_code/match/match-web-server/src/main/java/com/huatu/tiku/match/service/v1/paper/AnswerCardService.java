package com.huatu.tiku.match.service.v1.paper;

import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.match.bo.paper.AnswerCardSimpleBo;
import com.huatu.tiku.match.bo.paper.AnswerResultBo;
import com.huatu.tiku.match.bo.paper.QuestionSimpleBo;
import com.huatu.tiku.match.dto.paper.AnswerDTO;
import com.huatu.ztk.paper.bean.AnswerCard;

import java.util.List;

/**
 * 用户答题卡信息处理
 * Created by lijun on 2018/10/19
 */
public interface AnswerCardService {

    /**
     * 为用户创建一张答题卡 答题卡
     */
    AnswerCardSimpleBo createAnswerCard(UserSession userSession, Integer paperId, int terminal);

    /**
     * 根据答题卡ID查询答题卡信息-不包含统计信息
     *
     * @param answerCardId 答题卡ID 同 practiceId
     * @return 答题卡信息
     */
    AnswerCard findAnswerCard(Long answerCardId);

    /**
     * 获取答题卡对应的试题ID List
     *
     * @param answerCardId 答题卡ID 同 practiceId
     * @return 试题ID
     */
    List<Integer> getAnswerCardQuestionIdList(Long answerCardId);

    /**
     * 获取错题解析
     *
     * @param answerCardId 答题卡ID
     * @return 试题解析信息
     */
    List<QuestionSimpleBo> getWrongQuestionAnalysis(Long answerCardId);

    /**
     * 获取所有试题解析
     *
     * @param answerCardId 答题卡ID
     * @return 试题解析信息
     */
    List<QuestionSimpleBo> getAllQuestionAnalysis(Long answerCardId);

    /**
     * 用户试题答案保存
     */
    List<AnswerResultBo> save(Integer userId, Long practiceId, List<AnswerDTO> answerList);

    /**
     * 用户试题答案提交
     */
    void submit(Integer userId, Long practiceId, List<AnswerDTO> answerList);

    /**
     * 用户试题答案提交(快速提交-异步消息队列消费)
     */
    void submit2Queue(Integer userId, Long practiceId, List<AnswerDTO> answerList);
}
