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

/**
 * 批改反馈意见
 *
 * @author zhangchong
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@DynamicInsert(true) // 动态插入
@DynamicUpdate(true) // 动态更新
@Table(name = "v_essay_feed_back")
public class CorrectFeedBack extends BaseEntity {

    /**
     * 星级
     */
    private int star;

    /**
     * 反馈内容
     */
    private String content;

    /**
     * 答题卡id
     */
    private long answerId;

    /**
     * 答题卡类型 1单题 2套卷
     */
    private int answerType;

    //批改老师
    private long teacherId;
    //v_correct_order表中的ID
    private long orderId;

    private long userId;

}
