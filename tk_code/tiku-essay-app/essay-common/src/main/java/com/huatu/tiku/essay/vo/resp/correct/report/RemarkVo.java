package com.huatu.tiku.essay.vo.resp.correct.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/8
 * @描述 app 学员报告展示评语
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RemarkVo {
    /**
     * 评语内容
     */
    private String content;
    /**
     * 评语展示顺序
     */
    private int sort;

    //模版ID
    private long templateId;

    //模版内容
    private String templateContent;

    //评语ID
    private long commentId;

    //评语内容
    private String commentContent;

    /**
     * 得分
     */
    private Double score;

    /**
     * 批注类型
     */
    private int labelType;

    /**
     * 批注ID
     */
    private long labelId;

}
