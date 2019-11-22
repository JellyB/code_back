package com.huatu.tiku.match.ztk.api;

import com.huatu.tiku.match.common.FeignResponse;
import com.huatu.tiku.match.ztk.api.fallback.PositionFallBack;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-03 下午12:02
 **/



@FeignClient(value = "t-paper-server", path = "/p", fallback = PositionFallBack.class)
@Primary
public interface PositionFeignClient {

    /**
     * 代理paper服务地区接口
     * @return
     */
    @GetMapping(value = "/v1/matches/positions")
    FeignResponse getPositions();
}
