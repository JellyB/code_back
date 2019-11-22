package com.huatu.tiku.match.bo.paper;

import com.huatu.ztk.question.bean.QuestionMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by lijun on 2019/1/7
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericQuestionAnalysisBo extends GenericQuestionSimpleBo {

    /**
     * 正确答案
     */
    private Integer answer;

    /**
     * 解析
     */
    private String analysis;

    /**
     * 拓展
     */
    private String extend;

    /**
     * 知识点信息
     */
    private List<String> pointsName;

    /**
     * 来源
     */
    private String source;

    /**
     * 试题统计
     */
    private QuestionMeta meta;

}
