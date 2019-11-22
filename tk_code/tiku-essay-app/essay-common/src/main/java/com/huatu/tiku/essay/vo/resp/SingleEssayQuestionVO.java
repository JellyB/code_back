package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by x6 on 2017/11/23.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SingleEssayQuestionVO {

    //题目id
    private Long id;
    //题干
    private String stem;
    //答题时间
    private Integer limitTime;
    //批改次数
    private Integer correctTimes;

    //所属地区
    private List<EssayQuestionAreaVO> essayQuestionBelongPaperVOList;

    //分数
    private Double point;

    //答题要求  文字说明
    private String answerRequire;
    //输入字数最低限制
    private Integer inputWordNumMin;
    //输入字数最多限制
    private Integer inputWordNumMax;


}
