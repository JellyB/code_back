package com.huatu.tiku.essay.service;

import com.huatu.tiku.essay.vo.resp.EssayPaperReportQuestionVO;
import com.huatu.tiku.essay.vo.resp.ManualCorrectReportVo;

import java.util.LinkedList;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/30
 * @描述
 */
public interface EssayCommonReportService {

    ManualCorrectReportVo addPaperRemarkList(Long answerId, Integer answerType, String correctRemark);

    ManualCorrectReportVo addQuestionRemarkList(Long answerId, Integer answerType, String correctRemark);

    ManualCorrectReportVo addFeedBack(long answerCardId, int answerCardType);

    /**
     * 套卷每个试题的考试情况
     *
     * @param paperAnswerCardId
     * @param paperBaseId
     * @return
     */
    LinkedList<EssayPaperReportQuestionVO> getPaperQuestionList(Long paperAnswerCardId, Long paperBaseId);

}
