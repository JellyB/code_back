package com.huatu.tiku.essay.entity.correct;

import com.huatu.tiku.essay.entity.BaseEntity;
import com.huatu.tiku.essay.entity.EssayLabelBase;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * @author huangqingpeng
 * 套题阅卷批注信息表
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_essay_paper_label_total")
@EqualsAndHashCode(callSuper = false)
@DynamicInsert(true)//动态插入
@DynamicUpdate(true)//动态更新
@Builder
public class EssayPaperLabelTotal extends BaseEntity {

    /**
     * 试卷答题卡id
     */
    private long answerId;

    /**
     * 试卷ID
     */
    private long paperId;

    /**
     * 其他批注
     */
    private String elseRemark;


    /**
     * 音频ID
     */
    private int audioId;

    /**
     * LabelFlagEnum 批注的渠道
     */
    private int labelFlag;

}
