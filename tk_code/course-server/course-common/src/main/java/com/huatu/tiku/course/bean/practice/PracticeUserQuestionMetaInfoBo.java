package com.huatu.tiku.course.bean.practice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by lijun on 2019/2/27
 * 用户答题信息
 */
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class PracticeUserQuestionMetaInfoBo implements Serializable {

    /**
     * 答案
     */
    private String answer;

    /**
     * 耗时
     */
    private Integer time;

    /**
     * 是否对错 0-未答 1-对 2-错误
     */
    private Integer correct;

}
