package com.huatu.tiku.dto;

import com.huatu.tiku.request.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotNull;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/9/13
 * @描述
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DuplicateQuestionVo extends BaseReq {
    /**
     * 题型
     */
    @NotNull(message = "试题类型不能为空")
    private Integer questionType;
    /**
     * 选项
     */
    private String choices;
    /**
     * 题干
     */
    private String stem;
    /**
     * 解析
     */
    private String analysis;
    /**
     * 拓展
     */
    private String extend;
    /**
     * 参考答案
     */
    private String answerComment;
    /**
     * 试题分析
     */
    private String analyzeQuestion;
    /**
     * 答题要求
     */
    private String answerRequest;
    /**
     * 赋分说明
     */
    private String bestowPointExplain;
    /**
     * 解题思路
     */
    private String trainThought;
    /**
     * 总括要求
     */
    private String omnibusRequirements;


}
