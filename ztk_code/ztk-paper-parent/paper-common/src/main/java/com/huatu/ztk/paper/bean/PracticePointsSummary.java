package com.huatu.ztk.paper.bean;

import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 用户练习知识点汇总
 * Created by shaojieyue
 * Created time 2016-07-26 10:21
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PracticePointsSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private long practiceId;//练习id
    private List<QuestionPointTree> points;//知识点汇总
}
