package com.huatu.tiku.course.netschool.api;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.netflix.hystrix.HystrixCommand;
import feign.hystrix.Fallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

import static com.huatu.tiku.course.consts.NetSchoolSydwUrlConst.*;

/**
 * @author hanchao
 * @date 2017/8/18 16:05
 */
@FeignClient(value = "course-service")
public interface SydwCourseServiceV1 {
    @RequestMapping(value = SYDW_TOTAL_LIST,method = RequestMethod.GET)
    NetSchoolResponse sydwTotalList(@RequestParam Map<String,Object> params);

    @RequestMapping(value = SYDW_COURSE_DATAIL,method = RequestMethod.GET)
    NetSchoolResponse courseDetail(@RequestParam Map<String,Object> params);

    @GetMapping(SYDW_ALL_COLLECTION_LIST)
    NetSchoolResponse allCollectionList(@RequestParam Map<String,Object> params);


    @GetMapping(SYDW_LOGIN_COURSE)
    NetSchoolResponse sendFree(@RequestParam Map<String,Object> params);


    @Component
    @Slf4j
    class SydwCourseServiceV1FallbackFactory implements Fallback<SydwCourseServiceV1>{
        @Override
        public SydwCourseServiceV1 create(Throwable throwable, HystrixCommand command) {
            return new SydwCourseServiceV1(){
                @Override
                public NetSchoolResponse sydwTotalList(Map<String, Object> params) {
                    log.error("SydwCourseService v1 sydwTotalList fall back params:{}, fall back reason:{}", params, throwable);
                    return NetSchoolResponse.DEFAULT_ERROR;
                }

                @Override
                public NetSchoolResponse courseDetail(Map<String, Object> params) {
                    log.error("SydwCourseService v1 courseDetail fall back params:{}, fall back reason:{}", params, throwable);
                    return NetSchoolResponse.DEFAULT_ERROR;
                }

                @Override
                public NetSchoolResponse allCollectionList(Map<String, Object> params) {
                    log.error("SydwCourseService v1 allCollectionList fall back params:{}, fall back reason:{}", params, throwable);
                    return NetSchoolResponse.DEFAULT_ERROR;
                }

                @Override
                public NetSchoolResponse sendFree(Map<String, Object> params) {
                    log.error("SydwCourseService v1 sendFree fall back params:{}, fall back reason:{}", params, throwable);
                    return NetSchoolResponse.DEFAULT_ERROR;
                }
            };
        }
    }
}
