package com.huatu.tiku.essay.vo.admin.answer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户试卷答题卡后台封装类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminPaperAnswerSimpleVO {
    /**
     * 试卷答题卡ID
     */
    private long paperAnswerCardId;

    /**
     * 试卷Id
     */
    private long paperBaseId;

    /**
     * 试卷答题卡信息
     */
    private List<AdminQuestionAnswerSimpleVO>  questionAnswerCards;

    /**
     * 试卷批注ID
     */
    private long totalId;

    /**
     * EssayLabelBizStatusEnum
     */
    private int bizStatus;
}
