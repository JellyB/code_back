package com.huatu.tiku.essay.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;


/**
 * 综合批注
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_essay_label_total")
@EqualsAndHashCode(callSuper = false)
@DynamicInsert(true)//动态插入
@DynamicUpdate(true)//动态更新
@Builder
public class EssayLabelTotal extends EssayLabelBase {
    //分段得分
//    @NotNull(message = "分段得分不能为空")
    private String paragraphScore;

    //字数
//    @NotNull(message = "字数不能为空")
    private Integer inputWordNum;

    //字数得分
//    @NotNull(message = "字数得分不能为空")
    private String wordNumScore;

    //抄袭度
//    @NotNull(message = "抄袭度不能为空")
    private Double copyRatio;

    //此次批注得分
//    @NotNull(message = "批注得分不能为空")
    private Double totalScore;

    //是否是终审(0 不是 1是)
//    @NotNull(message = "是否是终审批注的标识不能为空")
    private int isFinal;

    //批注后的正文内容
    private String labeledContent;

    //批注后的标题内容
    private String titleContent;

    /**
     * 批注耗时
     */
    private Long spendTime;

    /**
     * 其他评语
     */
    private String elseRemark;

    /**
     * 1老师教研批注2为用户提供的批注3老师资格审核用批注
     */
    @Column(columnDefinition = "smallint default 1")
    private Integer labelFlag;

    private Double score;
    /**
     * 名师之声百家云音频Id
     */
    private Integer audioId;

    /**
     * ArticleLevelEnum  几类文
     */
    private Integer articleLevel;
}
