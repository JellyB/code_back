package com.huatu.tiku.course.netschool.api.fall;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.CourseServiceV1;
import com.netflix.hystrix.HystrixCommand;
import feign.hystrix.Fallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author hanchao
 * @date 2017/9/5 17:55
 */
@Component
@Slf4j
public class CourseServiceV1FallbackFactory implements Fallback<CourseServiceV1> {
    @Override
    public CourseServiceV1 create(Throwable cause, HystrixCommand command) {
        return new CourseServiceV1() {
            @Override
            public NetSchoolResponse totalList(Map<String, Object> params) {
                log.error("course service v1 fallback,params: {}, fall back reason: {}",params,cause);
                return NetSchoolResponse.DEFAULT;
            }

            @Override
            public NetSchoolResponse collectionList(Map<String, Object> params) {
                log.error("course service v1 fallback,params: {}, fall back reason: {}",params,cause);
                return NetSchoolResponse.DEFAULT;
            }

            @Override
            public NetSchoolResponse allCollectionList(Map<String, Object> params) {
                log.error("course service v1 fallback,params: {}, fall back reason: {}",params,cause);
                return NetSchoolResponse.DEFAULT;
            }

            @Override
            public NetSchoolResponse collectionDetail(Map<String, Object> params) {
                log.error("course service v1 fallback,params: {}, fall back reason: {}",params,cause);
                return NetSchoolResponse.DEFAULT;
            }

            @Override
            public NetSchoolResponse courseDetail(Map<String, Object> params) {
                log.error("course service v1 fallback,params: {}, fall back reason: {}",params,cause);
                return NetSchoolResponse.DEFAULT;
            }

            @Override
            public NetSchoolResponse courseDetailSp(Map<String, Object> params) {
                log.error("course service v1 fallback,params: {}, fall back reason: {}",params,cause); //这个不适合用默认的，要识别真正的null结果
                return null;
            }

            @Override
            public NetSchoolResponse myAndroidDetail(Map<String, Object> params) {
                log.error("course service v1 fallback,params: {}, fall back reason: {}",params,cause);
                return NetSchoolResponse.DEFAULT;
            }

            @Override
            public NetSchoolResponse myIosDetail(Map<String, Object> params) {
                log.error("course service v1 fallback,params: {}, fall back reason: {}",params,cause);
                return NetSchoolResponse.DEFAULT;
            }

            @Override
            public NetSchoolResponse getHandouts(Map<String, Object> params) {
                log.error("course service v1 fallback,params: {}, fall back reason: {}",params,cause);
                return NetSchoolResponse.DEFAULT;
            }

            @Override
            public NetSchoolResponse sendFree(Map<String, Object> params) {
                log.error("course service v1 fallback,params: {}, fall back reason: {}",params,cause);
                return NetSchoolResponse.DEFAULT;
            }
        };
    }
}
