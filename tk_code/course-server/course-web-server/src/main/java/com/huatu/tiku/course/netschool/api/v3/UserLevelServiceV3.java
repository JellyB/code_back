package com.huatu.tiku.course.netschool.api.v3;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.netflix.hystrix.HystrixCommand;
import feign.hystrix.Fallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author hanchao
 * @date 2017/10/16 13:44
 */
@FeignClient(value = "course-service")
public interface UserLevelServiceV3 {
    @GetMapping("/v3/mycount/myLevel.php")
    NetSchoolResponse getUserLevel(@RequestParam Map<String,Object> params);

    @GetMapping("/v3/mycount/levelDetail.php")
    NetSchoolResponse getLevelSettings();


    @Component
    @Slf4j
    class UserLevelServiceV3FallbackFactory implements Fallback<UserLevelServiceV3>{
        @Override
        public UserLevelServiceV3 create(Throwable throwable, HystrixCommand command) {
            return new UserLevelServiceV3(){
                @Override
                public NetSchoolResponse getUserLevel(Map<String, Object> params) {
                    log.error("UserLevelService v3 getUserLevel fall back , params:{}, fall back reason:{}", params, throwable);
                    return NetSchoolResponse.DEFAULT_ERROR;
                }

                @Override
                public NetSchoolResponse getLevelSettings() {
                    log.error("UserLevelService v3 getUserLevel fall back , fall back reason:{}", throwable);
                    return NetSchoolResponse.DEFAULT_ERROR;
                }
            };
        }
    }
}
