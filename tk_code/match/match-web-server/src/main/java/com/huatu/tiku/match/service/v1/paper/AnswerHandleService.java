package com.huatu.tiku.match.service.v1.paper;

import com.huatu.tiku.match.bo.paper.AnswerResultBo;
import com.huatu.tiku.match.dto.paper.AnswerDTO;
import com.huatu.ztk.paper.bean.AnswerCard;

import java.util.List;
import java.util.function.Function;

/**
 * Created by lijun on 2019/1/3
 */
public interface AnswerHandleService {

    /**
     * 判断一个答案是否正确
     *
     * @param questionId 试题ID
     * @param answer     试题答案
     * @return 正确
     */
    boolean handleQuestionAnswer(int questionId, String answer);

    /**
     * 处理用户提交的答案
     *
     * @param answerList 用户提交的答案
     */
    List<AnswerResultBo> handleQuestionAnswer(List<AnswerDTO> answerList);

    /**
     * 存储 更新答题信息 至用户答题卡
     *
     * @param answerCard         原始答题卡
     * @param answerResultBoList 用户答题信息
     * @param getSource          计算分数
     */
    void saveAnswerInfoToAnswerCard(AnswerCard answerCard, List<AnswerResultBo> answerResultBoList, Function<AnswerCard, Double> getSource);

}
