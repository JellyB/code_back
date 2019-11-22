package com.huatu.tiku.teacher.service.common;

import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionMeta;

/**
 *mongo question表中quesitonMeta字段属性获取
 * Created by huangqingpeng on 2018/11/8.
 */
public interface QuestionMetaService {

    /**
     * question项目获取试题统计数据的方式
     * @param genericQuestion
     * @return
     */
    QuestionMeta findMeta(GenericQuestion genericQuestion);


    /**
     * 获取question的权重值
     * @param question
     * @return
     */
    Double getQuestionWeight(Question question);
}
