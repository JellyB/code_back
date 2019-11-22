package com.huatu.tiku.essay.vo.admin.correct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/3
 * @描述
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class LabelCommentRelationVO {

    private long id;

    /**
     * 评语Id
     */
    private long commentId;

    /**
     * 1套题批注2单题总批注3单题详细批注
     */
    private int type;

    /**
     * type=1取paper_total_id
     * type=2取label_total_id
     * type=3取label_detail_id
     */
    private long labelId;


    /**
     * 1论点（选中的评语需关联论点-关键句id）
     * 2论据（选中的评语需关联详细批改id）
     * 3其他（选中的评语，如有子评语，需关联子评语ID）
     */
    private int bizType;

    /**
     * 根据bizType情况关联的各类ID组成的字符串（，隔开）
     * 这个字段只有在bizType=1,2或者bizType=3且有子评语的时候有值
     */
    private String bizId;

    /**
     * 评语内容
     */
    private String content;

    /**
     * 模版ID
     */
    private long templateId;

    /**
     * 分数（非必须填写）
     */
    private double score;

    //模版内容
    private String templateContent;


    //评语内容
    private String commentContent;


}
