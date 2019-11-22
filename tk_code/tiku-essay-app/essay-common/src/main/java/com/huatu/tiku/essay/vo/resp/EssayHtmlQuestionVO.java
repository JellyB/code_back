package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2018/3/21.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayHtmlQuestionVO {
    //序号
    private int sort;
    //题目要求
    private String answerRequire;
    //参考答案
    private String answerComment;

    /*  标题  */
    private String topic;
    /* 子标题 */
    private  String subTopic;
    /* 称呼 */
    private String callName;

    /* 落款日期 */
    private String inscribedDate;
    /* 落款名称 */
    private String inscribedName;
}
