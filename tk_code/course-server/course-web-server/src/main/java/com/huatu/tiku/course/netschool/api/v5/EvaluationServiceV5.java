package com.huatu.tiku.course.netschool.api.v5;

import com.google.common.collect.Maps;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.util.ResponseUtil;
import com.netflix.hystrix.HystrixCommand;
import feign.hystrix.Fallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Created by lijun on 2018/6/25
 */
@FeignClient(value = "o-course-service", path = "/lumenapi", fallbackFactory = EvaluationServiceV5.EvaluationServiceV5FallbackFactory.class)
public interface EvaluationServiceV5 {

    /**
     * 获取售前课程详情评价列表
     */
    @GetMapping(value = "/v4/common/class/evaluation")
    NetSchoolResponse getClassEvaluation(@RequestParam Map<String, Object> params);

    /**
     * 获取老师介绍页评价列表
     */
    @GetMapping(value = "/v4/common/teacher/evaluation")
    NetSchoolResponse getTeacherEvaluation(@RequestParam Map<String, Object> params);

    /**
     * 获取用户当前课件评价信息接口
     */
    @GetMapping(value = "/v4/common/evaluation/click")
    NetSchoolResponse getClickEvaluation(@RequestParam Map<String, Object> params);

    /**
     * 提交评价
     */
    @PostMapping(value = "/v4/common/evaluation/submit")
    NetSchoolResponse submit(@RequestParam Map<String, Object> params);

    @Slf4j
    @Component
    class EvaluationServiceV5FallbackFactory implements Fallback<EvaluationServiceV5>{

        @Override
        public EvaluationServiceV5 create(Throwable throwable, HystrixCommand command) {
            return new EvaluationServiceV5(){
                /**
                 * 获取售前课程详情评价列表
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse getClassEvaluation(Map<String, Object> params) {
                    log.error("evaluation service v5 getClassEvaluation fallback,params: {}, fall back reason: {}", params, throwable);
                    return ResponseUtil.DEFAULT_PHP_SIMPLE_PAGE_RESPONSE;
                }

                /**
                 * 获取老师介绍页评价列表
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse getTeacherEvaluation(Map<String, Object> params) {
                    log.error("evaluation service v5 getTeacherEvaluation fallback,params: {}, fall back reason: {}", params, throwable);
                    return ResponseUtil.DEFAULT_PHP_SIMPLE_PAGE_RESPONSE;
                }

                /**
                 * 获取用户当前课件评价信息接口
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse getClickEvaluation(Map<String, Object> params) {
                    log.error("evaluation service v5 getClickEvaluation fallback,params: {}, fall back reason: {}", params, throwable);
                    return new NetSchoolResponse(com.huatu.common.Result.SUCCESS_CODE, "", Maps.newHashMap());
                }

                /**
                 * 提交评价
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse submit(Map<String, Object> params) {
                    log.error("evaluation service v5 submit fallback,params: {}, fall back reason: {}", params, throwable);
                    return new NetSchoolResponse(com.huatu.common.Result.SUCCESS_CODE, "", "");
                }
            };
        }
    }

}
