package com.huatu.tiku.course.netschool.api.v4;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.fall.FallbackCacheHolder;
import com.huatu.tiku.course.service.cache.CourseCacheKey;
import com.netflix.hystrix.HystrixCommand;
import feign.hystrix.Fallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lijun on 2018/5/18
 */
@FeignClient(value = "o-course-service", path = "/lumenapi/v4/app", fallbackFactory = AppServiceV4.AppServiceV4FallbackFactory.class)
public interface AppServiceV4 {

    @GetMapping("/lession/evaluate")
    NetSchoolResponse lessionEvaluate(@RequestParam Map<String, Object> params);

    @GetMapping("/lession/token")
    NetSchoolResponse lessionToken(
            @RequestParam("bjyRoomId") String bjyRoomId,
            @RequestParam("bjySessionId") String bjySessionId,
            @RequestParam("videoId") String videoId);

    /**
     * 合集查询
     */
    @GetMapping("/collectionclasses/collection_classes")
    NetSchoolResponse collectionClasses(@RequestParam HashMap map);

    /**
     * 专栏
     */
    @GetMapping(value = "/netclasses/specialcolumn")
    NetSchoolResponse specialColumn();


    @Component
    @Slf4j
    class AppServiceV4FallbackFactory implements Fallback<AppServiceV4>{
        @Override
        public AppServiceV4 create(Throwable throwable, HystrixCommand command) {
            return new AppServiceV4() {
                @Override
                public NetSchoolResponse lessionEvaluate(Map<String, Object> params) {
                    log.error("AppService lessionEvaluate v4 fall back ,params:{}, fall back reason:{}", params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                @Override
                public NetSchoolResponse lessionToken(String bjyRoomId, String bjySessionId, String videoId) {
                    log.error("AppService lessionToken v4 fall back ,fall back reason:{}", throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                @Override
                public NetSchoolResponse collectionClasses(HashMap map) {
                    log.error("AppService collectionClasses v4 fall back ,params:{}, fall back reason:{}", map, throwable);
                    String key = CourseCacheKey.collectionClassesKeyV4(map);
                    return FallbackCacheHolder.get(key);
                }

                @Override
                public NetSchoolResponse specialColumn() {
                    log.error("AppService specialColumn v4 fall back , fall back reason:{}", throwable);
                    return NetSchoolResponse.DEFAULT;
                }
            };
        }
    }

}
