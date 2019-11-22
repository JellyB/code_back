package com.huatu.ztk.user.bean;

import lombok.*;

/**
 * Author: xuhuiqiang
 * Time: 2017-05-25  18:18 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@ToString(callSuper=true)
public class PractiseRecord extends LearnRecord{
    private int subject;//科目
    private long uid;//用户id
    private long answerId;
    private int practiseType;//练习类型
    private int practiseStatus;//试卷完成状态
}
