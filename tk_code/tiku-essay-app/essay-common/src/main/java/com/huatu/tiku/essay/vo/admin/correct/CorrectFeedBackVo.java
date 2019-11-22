package com.huatu.tiku.essay.vo.admin.correct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/18
 * @描述
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CorrectFeedBackVo {

    private long id;
    //星星
    private int star;
    //评价内容
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
