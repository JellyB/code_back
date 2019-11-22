package com.huatu.tiku.match.ztk.api;

import com.huatu.tiku.match.ztk.api.fallback.TestApiServiceFallback;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * feign 远程调用 基础测试，此处调用 course-server. ForTestController 下接口
 * Created by lijun on 2018/10/12
 */
@FeignClient(value = ZtkApiCommon.SERVICE_URL, fallback = TestApiServiceFallback.class, path = ZtkApiCommon.PATH_COURSE)
public interface TestApiService {

    @GetMapping("4/test/get")
    Object testGet();
}
