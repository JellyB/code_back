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
 * 评语模版表
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@DynamicInsert(true)//动态插入
@DynamicUpdate(true)//动态更新
@Table(name = "v_essay_comment_template")
public class CommentTemplate extends BaseEntity implements Serializable {


    /**
     * 最大分组（1小题2应用文3议论文4套题）
     */
    private int type;

    /**
     * 1单个批注（单题）、2本题阅卷（单题）、3综合评价（套题）、4扣分项（单题）
     * 小题（1，2，4）；应用文（1，2，4）；议论文（1，2）；套题（3）
     */
    private int labelType;

    /**
     * 1论点（选中的评语需关联论点-关键句id）
     * 2论据（选中的评语需关联详细批改id）
     * 3其他（选中的评语，如有子评语，需关联子评语ID）
     */
    private int bizType;

    /**
     * 模版名称（如文采）
     */
    private String name;

    /**
     * 顺序
     */
    private int sort;
}
