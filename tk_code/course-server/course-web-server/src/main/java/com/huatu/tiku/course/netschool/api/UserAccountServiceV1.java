package com.huatu.tiku.course.netschool.api;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.fall.UserAccountServiceFallBack;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-03-06 3:19 PM
 **/

@FeignClient(name = "ztk-user-service", path = "/u", fallback = UserAccountServiceFallBack.class)
public interface UserAccountServiceV1 {

    /**
     * 根据userName换区 userId
     * @param userNames
     * @return
     */
    @PostMapping(value = "/v1/users/userIdBatch")
    NetSchoolResponse getUIdByUsernameBatch(@RequestBody List<String> userNames);

    /**
     * 判断 ios 版本是否为审核版本
     * @param cv
     * @return
     */
    @GetMapping(value = "/v2/users/check/audit")
    NetSchoolResponse isIosAudit(@RequestParam(value = "cv") String cv);

}
