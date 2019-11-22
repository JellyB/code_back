package com.huatu.tiku.interview.entity.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by x6 on 2018/4/11.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionDetailVO {
    private long id;
    //试题名称
    private String stem;
    //选项列表
    private List<ChoiceDetailVO> choiceList;
    //题目类型
    private int questionType;
    //用户答案
    private String answer;
}
