package com.huatu.tiku.essay.entity.correct;

import com.huatu.tiku.essay.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author huangqingpeng
 * 评语模版选项表
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@DynamicInsert(true)//动态插入
@DynamicUpdate(true)//动态更新
@Table(name="v_essay_comment_template_detail")
public class CommentTemplateDetail extends BaseEntity implements Serializable {
    /**
     * 评语模版ID(-1表示是二级子评语)
     */
    private long templateId;

    /**
     * 评语内容
     */
    private String content;

    /**
     * 上级评语id（pid=-1为一级评语，否则是2级评语）
     */
    private long pid;

    /**
     * 评语展示顺序
     */
    private int sort;

    /**
     * 是否可开展 0 不能展开，没有下级 1 可以展开有下级
     */
    @Column(columnDefinition = "smallint default 1")
    private int isExtended;
}
