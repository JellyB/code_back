package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by huangqp on 2017\12\1 0001.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayPaperQuestionVO {
    //试卷信息
    private EssayPaperVO essayPaper;
    //题目信息
    private List<EssayQuestionVO> essayQuestions;

}
