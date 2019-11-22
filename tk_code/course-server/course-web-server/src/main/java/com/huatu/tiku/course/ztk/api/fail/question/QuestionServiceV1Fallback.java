package com.huatu.tiku.course.ztk.api.fail.question;

import com.google.common.collect.Lists;
import com.huatu.tiku.course.ztk.api.v1.question.QuestionServiceV1;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by lijun on 2018/6/20
 */
@Slf4j
@Component
public class QuestionServiceV1Fallback implements QuestionServiceV1 {

    @Override
    public Object listQuestionByIds(String questionIds) {
        log.info("listQuestionByIds fail,ids = {}", questionIds);
        return Lists.newArrayList();
    }
}
