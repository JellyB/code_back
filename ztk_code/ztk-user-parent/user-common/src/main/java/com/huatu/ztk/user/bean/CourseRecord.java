package com.huatu.ztk.user.bean;

import lombok.*;

/**
 * Author: xuhuiqiang
 * Time: 2017-05-23  15:28 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@ToString(callSuper=true)
public class CourseRecord extends LearnRecord{
    private long id;//课程id
    private int courseStatus;//课程状态
}
