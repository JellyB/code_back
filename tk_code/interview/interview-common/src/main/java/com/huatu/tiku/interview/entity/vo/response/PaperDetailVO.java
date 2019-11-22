package com.huatu.tiku.interview.entity.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by x6 on 2018/4/11.
 * 学员查看试卷详情
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaperDetailVO {
    //试卷id
    private long id;
    //试卷名称(标题)
    private String paperName;
    //题目列表
    private List<QuestionDetailVO> questionList;
    //是否已经作答过
    private boolean hasCommitted;
    //类型
    private int type;

}
