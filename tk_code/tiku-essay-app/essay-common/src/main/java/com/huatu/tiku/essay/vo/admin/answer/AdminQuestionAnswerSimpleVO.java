package com.huatu.tiku.essay.vo.admin.answer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 试题答题卡相关逻辑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminQuestionAnswerSimpleVO {

    /**
     * 试题答题卡ID
     */
    private long questionAnswerCardId;

    /**
     * 试题ID
     */
    private long questionBaseId;

    /**
     * 试题详细ID
     */
    private long questionDetailId;

    /**
     * 试题批注ID
     */
    private long totalId;

    /**
     * EssayLabelBizStatusEnum
     */
    private int bizStatus;
}
