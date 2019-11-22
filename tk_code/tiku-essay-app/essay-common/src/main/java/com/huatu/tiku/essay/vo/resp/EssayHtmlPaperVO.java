package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by x6 on 2018/3/21.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayHtmlPaperVO {

    private Long paperId;
    //试卷名称
    private String paperName;
    //试卷分数
    private Double score;
    //答题限时
    private Integer limitTime;
    //材料列表
    private List<String> materialList;
    //试题列表
    private List<EssayHtmlQuestionVO> questionList;
    //

}
