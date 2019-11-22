package com.huatu.tiku.course.service.v1.practice;

import com.huatu.tiku.course.bean.practice.QuestionInfo;

import java.util.List;

/**
 * Created by lijun on 2019/2/27
 */
public interface QuestionInfoService {

    /**
     * 获取试题信息
     */
    List<QuestionInfo> getBaseQuestionInfo(List<Long> questionIdList);
}
