package com.huatu.ztk.backend.question.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by aatrox on 2017/3/8.
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class ViewPracticeQuestion {
    private int id;//试题id
    private String stem;//题干
    private int subject;//科目
    private int moduleId;//模块
    private int tkType;//题库类型
    private long createTime;//创建时间
}
