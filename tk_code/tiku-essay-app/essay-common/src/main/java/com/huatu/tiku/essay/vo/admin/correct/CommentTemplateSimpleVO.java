package com.huatu.tiku.essay.vo.admin.correct;


import lombok.AllArgsConstructor;
import lombok.Builder;

@AllArgsConstructor
@Builder
public class CommentTemplateSimpleVO {
    /**
     * 评语ID
     */
    private Long commentId;
    /**
     * 评语模版ID
     */
    private Long templateId;
    /**
     * 评语内容
     */
    private String content;
    /**
     * 1论点评语2论据评语3其他评语
     */
    private int bizType;
    /**
     * type=1,论点ID,type=2,选中的detailId,type3=3,子评语Id
     */
    private String bizIds;
    /**
     * 评语得分
     */
    private Double score;
}
