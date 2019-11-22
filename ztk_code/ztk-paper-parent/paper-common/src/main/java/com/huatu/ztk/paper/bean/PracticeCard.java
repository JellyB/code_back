package com.huatu.ztk.paper.bean;


import lombok.*;

import java.io.Serializable;

/**
 * 练习试卷答题卡
 * Created by shaojieyue
 * Created time 2016-04-29 16:44
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@ToString(callSuper=true)
@Builder
public class PracticeCard extends AnswerCard implements Serializable{
    private static final long serialVersionUID = 1L;

    private PracticePaper paper;//练习卷
    private int recommendedTime; //练习的推荐时间
    /**
     * pc端模考解决精确度问题
     */
    private String idStr;
}
