package com.huatu.tiku.course.netschool.api.v6;

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
 * 描述：售后大纲课程等列表
 *
 * @author biguodong
 * Create time 2019-01-03 下午4:13
 **/
@FeignClient(value = "o-course-service", path = "/lumenapi/v4/common/", fallbackFactory = SyllabusServiceV6.SyllabusServiceV6FallBack.class)
public interface SyllabusServiceV6 {


    /**
     * 售后大纲课程列表（7.1.1）
     * @param params
     * @return
     */
    @GetMapping(value = "/class/syllabus_classes")
    NetSchoolResponse syllabusClasses(@RequestParam Map<String, Object> params);


    /**
     * 售后大纲老师列表（7.1.1）
     * @param params
     * @return
     */
    @GetMapping(value = "/class/syllabus_teachers")
    NetSchoolResponse syllabusTeachers(@RequestParam Map<String, Object> params);


    /**
     * 大纲 售后
     * @param params
     * @return
     */
    @GetMapping(value = "/class/buy_after_syllabus")
    NetSchoolResponse buyAfterSyllabus(@RequestParam Map<String, Object> params);


    /**
     * 课程大纲-售前
     * @param params
     * @return
     */
    @GetMapping(value = "/class/class_syllabus")
    NetSchoolResponse classSyllabus(@RequestParam Map<String, Object> params);

    @Slf4j
    @Component
    class SyllabusServiceV6FallBack implements Fallback<SyllabusServiceV6>{
        @Override
        public SyllabusServiceV6 create(Throwable throwable, HystrixCommand command) {
            return new SyllabusServiceV6(){
                /**
                 * 售后大纲课程列表（7.1.1）
                 *
                 * @param params
                 * @return
                 */
                @Override
                public NetSchoolResponse syllabusClasses(Map<String, Object> params) {
                    log.error("SyllabusServiceV6 syllabusClasses request fallback, params:{}, fall back reason:{}", params, throwable);
                    return NetSchoolResponse.DEFAULT_ERROR;
                }

                /**
                 * 售后大纲老师列表（7.1.1）
                 *
                 * @param params
                 * @return
                 */
                @Override
                public NetSchoolResponse syllabusTeachers(Map<String, Object> params) {
                    log.error("SyllabusServiceV6 syllabusTeachers request fallback, params:{}, fall back reason:{}", params, throwable);
                    return NetSchoolResponse.DEFAULT_ERROR;
                }

                /**
                 * 大纲 售后
                 *
                 * @param params
                 * @return
                 */
                @Override
                public NetSchoolResponse buyAfterSyllabus(Map<String, Object> params) {
                    log.error("SyllabusServiceV6 buyAfterSyllabus request fallback, params:{}, fall back reason:{}", params, throwable);
                    return NetSchoolResponse.DEFAULT_ERROR;
                }

                /**
                 * 课程大纲-售前
                 *
                 * @param params
                 * @return
                 */
                @Override
                public NetSchoolResponse classSyllabus(Map<String, Object> params) {
                    log.error("SyllabusServiceV6 classSyllabus request fallback, params:{}, fall back reason:{}", params, throwable);
                    return NetSchoolResponse.DEFAULT_ERROR;
                }
            };
        }
    }
}
