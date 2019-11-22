package com.huatu.tiku.course.ztk.api.v1.question;

import com.huatu.tiku.course.ztk.api.fail.question.QuestionServiceV1Fallback;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by lijun on 2018/6/20
 */
@FeignClient(value = "ztk-service", fallback = QuestionServiceV1Fallback.class, path = "/q")
public interface QuestionServiceV1 {

    @GetMapping(value = "/v3/questions/batch")
    Object listQuestionByIds(@RequestParam("ids") String questionIds);

    /**
     * 获取试题类型
     */
    default String getQuestionTypeName(int type) {
        switch (type) {
            case 99:
                return "单选题";
            case 101:
                return "不定项选择";
            case 100:
                return "多选题";
            case 109:
                return "对错题";
            case 105:
                return "复合题";
            case 106:
                return "单一主观题";
            case 107:
                return "复合主观题";
            default:
                return "";
        }
    }
}
