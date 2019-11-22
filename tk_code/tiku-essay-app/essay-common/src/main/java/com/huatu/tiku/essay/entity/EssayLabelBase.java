package com.huatu.tiku.essay.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.MappedSuperclass;

/**
 * Created by x6 on 2018/7/5.
 */
@Data
@NoArgsConstructor
@MappedSuperclass
@DynamicInsert
@DynamicUpdate(true)
public class EssayLabelBase extends BaseEntity{

    //答题卡id
    private Long  answerId;
    //题目id
    private Long  questionId;

    //标题得分
    private String titleScore;

    //论点得分
    private String thesisScore;
    //论据得分
    private String evidenceScore;
    //结构得分
    private String structScore;
    // 语言得分
    private String sentenceScore;
    // 文采得分
    private String literaryScore;
    // 思想性得分
    private String thoughtScore;


}
