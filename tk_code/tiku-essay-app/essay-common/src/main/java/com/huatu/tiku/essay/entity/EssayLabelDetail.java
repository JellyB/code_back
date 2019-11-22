package com.huatu.tiku.essay.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 详细批注（针对每个句子的标注）
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="v_essay_label_detail")
@EqualsAndHashCode(callSuper = false)
@DynamicInsert(true)//动态插入
@DynamicUpdate(true)//动态更新
public class EssayLabelDetail extends EssayLabelBase{
    //綜合批註的id
    private Long totalId;

    //起始字符的位置
    private Integer startPosition;
    //结束字符的位置
    private Integer endPosition;
    //被标注的文字内容
    private String content;

    //其他标注
    private String elseRemark;


    /**
     * 批注后的内容
     */
    private String labeledContent;

    /**
     * 新增imageId，imageAxis(单个坐标),imageAllAxis（整体坐标）三个字段
     */
    private Long imageId;

    private String imageAxis;

    private String imageAllAxis;
}
