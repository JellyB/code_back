package com.huatu.tiku.essay.vo.resp.correct;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;
import com.huatu.tiku.essay.vo.resp.EssayQuestionAnswerSimpleVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by x6 on 2017/12/8.
 */

/**
 * Created by x6 on 2017/12/1.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public  class ResponseExtendVO {

    private CorrectTimesSimpleVO intelligence;

    private CorrectTimesSimpleVO manual;

    /* 答题卡id */
    private Long answerCardId;

    /**
     * 另外一种批改模式的答题卡
     */
    private Long otherAnswerCardId;

    /**
     * 批改类型
     *
     * @see CorrectModeEnum
     */
    private Integer correctMode;

    /**
     * 最近一次人工批改的答题卡状态
     */
    private Integer manualRecentStatus;

    /**
     * 最近修改的答题卡状态
     */
    private Integer lastType;

    /**
     * 最近一次批改状态
     */
    private Integer recentStatus;
}
