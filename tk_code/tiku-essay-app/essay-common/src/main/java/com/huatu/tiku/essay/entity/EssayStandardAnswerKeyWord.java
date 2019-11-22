package com.huatu.tiku.essay.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by huangqp on 2017\12\6 0006.
 */
@Entity
@Data
@Builder
@Table(name="v_essay_standard_answer_keyword")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class EssayStandardAnswerKeyWord extends BaseEntity{
    //内容
    private String item;
    //分数
    private double  score;
    //拆分数量
    private int splitNum;
    private String firstSplitWord;
    private String secondSplitWord;
    private String thirdSplitWord;
    private String fourthSplitWord;
    private String fifthSplitWord;
    //该关键词类型：1为某小题的关键词；2为某关键词的近义词；3为某关键句下面的关键词；4为格式中标题的关键词；5为格式中称呼的关键词；6为格式中落款的关键词
    private int type;
    //对应的id，若type为1，则为某小题的detail_ID；type为2,则为某关键词的id；type为3，则为某关键句的id；type为4，5,6，则为格式的id
    //对应的id，若type为1，则为某小题的初始关键词规则，值为0；type为2,则为某关键词的id；type为3，则为某关键句的id；type为4，5,6，则为格式的id
    private long correspondingId;
    //试题id
    private long questionDetailId;
}
