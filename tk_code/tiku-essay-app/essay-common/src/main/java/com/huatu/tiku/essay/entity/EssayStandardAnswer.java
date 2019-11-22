package com.huatu.tiku.essay.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by x6 on 2018/5/3.
 * 标准答案表
 */
@Entity
@Data
@Builder
@Table(name="v_essay_standard_answer")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class EssayStandardAnswer  extends BaseEntity  implements Serializable {

    /* 试题id */
    private long questionId;

    /* 答案内容 */
    private String answerComment;
    /* 标题 */
    private String topic;
    /* 子标题 */
    private  String subTopic;
    /* 称呼 */
    private String callName;

    /* 落款日期 */
    private String inscribedDate;
    /* 落款名称 */
    private String inscribedName;

    //答案类型(0 参考答案  1标准答案)
    private int answerFlag;


}
