package com.huatu.ztk.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 用户提交的答案
 * Created by shaojieyue
 * Created time 2016-06-01 12:09
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class UserAnswers implements Serializable {
    private static final long serialVersionUID = 1L;

    private long uid;//用户id
    private long practiceId;//答案对应的练习id
    private int subject;//答案所属知识点
    private int catgory;//科目
    private int area;//做题所属区域
    private long submitTime;//提交答案的时间
    private List<Answer> answers;//答案列表
}
