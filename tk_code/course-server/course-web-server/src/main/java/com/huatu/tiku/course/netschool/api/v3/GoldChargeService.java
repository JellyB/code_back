package com.huatu.tiku.course.netschool.api.v3;

import com.huatu.common.spring.web.MediaType;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.netflix.hystrix.HystrixCommand;
import feign.hystrix.Fallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

/**
 * @author hanchao
 * @date 2017/10/14 11:08
 */
@FeignClient(value = "o-course-service", fallbackFactory = GoldChargeService.GoldChargeServiceFallBack.class)
public interface GoldChargeService {
    @PostMapping( value = "/web_v/order/actionRemark.php",consumes = MediaType.APPLICATION_FORM_URLENCODED_UTF8_VALUE)
    NetSchoolResponse chargeGold(Map<String,Object> params);

    @Slf4j
    @Component
    class GoldChargeServiceFallBack implements Fallback<GoldChargeService>{
        @Override
        public GoldChargeService create(Throwable throwable, HystrixCommand command) {
            return new GoldChargeService(){
                @Override
                public NetSchoolResponse chargeGold(Map<String, Object> params) {
                    log.error("gold charge service chargeGold request fall back, reason:{}", throwable);
                    return NetSchoolResponse.DEFAULT_ERROR;
                }
            };
        }
    }
}
