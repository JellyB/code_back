package com.huatu.tiku.essay.entity.correct;

import com.huatu.tiku.essay.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author huangqingpeng
 * 批注（套题或试题相关）和评语的关联表
 * TODO 其他批注或其他评语在各个批注表中（paper_total|label_total|label_detail）添加字段
 * TODO 名师之声的在（paper_total|label_total)表中添加字段
 * TODO 划档制几类文、划档制还是要点制类型在（label_total）表中添加字段
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@DynamicInsert(true)//动态插入
@DynamicUpdate(true)//动态更新
@Table(name = "v_essay_label_comment_r")
public class LabelCommentRelation extends BaseEntity implements Serializable {

    /**
     * 评语Id
     */
    private long commentId;

    /**
     *1单题详细批注 2单题总批注 3套题批注
     */
    private int type;

    /**
     * type=1取paper_total_id( 批注类型是 套卷批注,labelId代表是v_essay_paper_total表的批注id)
     * type=2取label_total_id (批注类型是 单题阅卷,labelId代表是v_essay_label_total表的批注id)
     * type=3取label_detail_id (批注类型是 单题详细批注,labelId代表是 v_essay_label_detail表的 批注ID,detail_id)
     */
    private long labelId;

    /**
     * 得分（非必填）
     * 套卷的得分是每个单题分数的累加
     */
    private double score;

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
     * 模版ID
     */
    private long templateId;

}
