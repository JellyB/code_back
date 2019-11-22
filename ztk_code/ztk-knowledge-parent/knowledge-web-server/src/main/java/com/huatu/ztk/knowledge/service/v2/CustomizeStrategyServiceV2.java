package com.huatu.ztk.knowledge.service.v2;

import com.huatu.ztk.knowledge.bean.QuestionStrategy;

public interface CustomizeStrategyServiceV2 {
    QuestionStrategy randCustomizeStrategy(long userId, int subject, Integer pointId, int size);
}
