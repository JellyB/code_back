package com.huatu.tiku.course.netschool.api.v5;

import com.huatu.tiku.course.bean.NetSchoolResponse;
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
 * @author zhouwei
 * @create 2018-07-19 下午8:00
 **/
@FeignClient(value = "o-course-service", path = "/lumenapi", fallbackFactory = BarrageServiceV5.BarrageServiceV5FallBack.class)
public interface BarrageServiceV5 {
    /**
     * 弹幕列表
     */
    @GetMapping(value = "/v4/common/barrage/get_list")
    NetSchoolResponse barrageList(@RequestParam Map<String, Object> params);

    /**
     * 弹幕获取
     */
    @PostMapping(value = "/v4/common/barrage/submit")
    NetSchoolResponse barrageAdd(@RequestParam Map<String, Object> params);


    @Slf4j
    @Component
    class BarrageServiceV5FallBack implements Fallback<BarrageServiceV5>{
        @Override
        public BarrageServiceV5 create(Throwable throwable, HystrixCommand command) {
            return new BarrageServiceV5(){
                /**
                 * 弹幕列表
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse barrageList(Map<String, Object> params) {
                    log.error("barrage service v5 barrageList request fall back, params:{}, reason:{}", params, throwable);
                    return NetSchoolResponse.DEFAULT_ERROR;
                }

                /**
                 * 弹幕获取
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse barrageAdd(Map<String, Object> params) {
                    log.error("barrage service v5 barrageAdd request fall back, params:{}, reason:{}", params, throwable);
                    return NetSchoolResponse.DEFAULT_ERROR;
                }
            };
        }
    }
}
