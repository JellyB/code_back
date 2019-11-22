package com.huatu.tiku.course.netschool.api.v6;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 描述：我的评价
 *
 * @author biguodong
 * Create time 2018-11-30 上午10:30
 **/
@FeignClient(value = "o-course-service", path = "/lumenapi")
public interface EvaluationServiceV6 {

    /**
     * 获取售前课程详情评价列表
     * 新增参数区分是否是我的评价
     * @param params
     * @return
     */
    @GetMapping(value = "/v4/common/class/evaluation")
    NetSchoolResponse getClassEvaluation(@RequestParam Map<String, Object> params);


}
