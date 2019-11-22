package com.huatu.tiku.course.netschool.api.fall;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.bean.vo.PeriodTestListVO;
import com.huatu.tiku.course.netschool.api.v6.UserCourseServiceV6;
import com.netflix.hystrix.HystrixCommand;
import feign.hystrix.Fallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;


/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-23 下午1:32
 **/
@Slf4j
@Component
public class UserCourseServiceV6FallBackFactory implements Fallback<UserCourseServiceV6>{

    @Autowired
    private UserCourseServiceV6FallBack userCourseServiceV6FallBack;


    @Override
    public UserCourseServiceV6 create(Throwable throwable, HystrixCommand command) {

        return new UserCourseServiceV6(){

            /**
             * 我的学习-日历接口
             *
             * @param params
             * @return
             */
            @Override
            public NetSchoolResponse obtainLearnCalender(Map<String, Object> params) {
                log.error("UserCourseService V6 obtainLearnCalender fall back params:{}, fallback reason:{}", params, throwable);
                return userCourseServiceV6FallBack.obtainMineCourses(params);
            }

            /**
             * 我的-已过期课程
             *
             * @param params
             * @return
             */
            @Override
            public NetSchoolResponse obtainExpiredCourses(Map<String, Object> params) {
                log.error("UserCourseService V6 obtainExpiredCourses fall back params:{}, fallback reason:{}", params, throwable);
                return userCourseServiceV6FallBack.obtainExpiredCourses(params);
            }

            /**
             * 我的课程等筛选列表
             *
             * @param params
             * @return
             */
            @Override
            public NetSchoolResponse obtainCourseFilterList(Map<String, Object> params) {
                log.error("UserCourseService V6 obtainCourseFilterList fall back params:{}, fallback reason:{}", params, throwable);
                return userCourseServiceV6FallBack.obtainCourseFilterList(params);
            }

            /**
             * 我的课程
             *
             * @param params
             * @return
             */
            @Override
            public NetSchoolResponse obtainMineCourses(Map<String, Object> params) {
                log.error("UserCourseService V6 obtainMineCourses fall back params:{}, fallback reason:{}", params, throwable);
                return userCourseServiceV6FallBack.obtainMineCourses(params);
            }

            /**
             * 一键清除我的已过期课程
             *
             * @param params
             * @return
             */
            @Override
            public NetSchoolResponse clearExpiredCourses(Map<String, Object> params) {
                log.error("UserCourseService V6 clearExpiredCourses fall back params:{}, fallback reason:{}", params, throwable);
                return NetSchoolResponse.DEFAULT;
            }

            /**
             * 课程所属考试接口
             *
             * @param params
             * @return
             */
            @Override
            public NetSchoolResponse cateList(Map<String, Object> params) {
                log.error("UserCourseService V6 cateList fall back params:{}, fallback reason:{}", params, throwable);
                return NetSchoolResponse.DEFAULT;
            }

            /**
             * 列表设置考试类型
             *
             * @param params
             * @return
             */
            @Override
            public NetSchoolResponse setCategory(Map<String, Object> params) {
                log.error("UserCourseService V6 setCategory fall back params:{}, fallback reason:{}", params, throwable);
                return NetSchoolResponse.DEFAULT;
            }

            /**
             * 直播学习记录上报
             *
             * @param params
             * @return
             */
            @Override
            public NetSchoolResponse saveLiveRecord(Map<String, Object> params) {
                log.error("UserCourseService V6 saveLiveRecord fall back params:{}, fallback reason:{}", params, throwable);
                return NetSchoolResponse.DEFAULT;
            }

            /**
             * 获取未完成的阶段测试列表
             *
             * @param params
             * @return
             */
            @Override
            public NetSchoolResponse<PeriodTestListVO> unfinishStageExamList(Map<String, Object> params) {
                log.error("UserCourseService V6 unfinishStageExamList fall back params:{}, fallback reason:{}", params, throwable);
                return NetSchoolResponse.DEFAULT;
            }

            /**
             * 阶段测完成试状态上报php
             *
             * @param params
             * @return
             */
            @Override
            public NetSchoolResponse stageTestStudyRecord(Map<String, Object> params) {
                log.error("UserCourseService V6 stageTestStudyRecord fall back params:{}, fallback reason:{}", params, throwable);
                return NetSchoolResponse.DEFAULT;
            }

            /**
             * 用户未完成阶段测试总数
             *
             * @param params
             * @return
             */
            @Override
            public NetSchoolResponse unfinishStageExamCount(Map<String, String> params) {
                log.error("UserCourseService V6 unfinishStageExamCount fall back params:{}, fallback reason:{}", params, throwable);
                return NetSchoolResponse.DEFAULT;
            }

            /**
             * 阶段测试单条获取全部已读
             *
             * @param params
             * @return
             */
            @Override
            public NetSchoolResponse readPeriod(Map<String, Object> params) {
                log.error("UserCourseService V6 readPeriod fall back params:{}, fallback reason:{}", params, throwable);
                return NetSchoolResponse.DEFAULT;
            }

            /**
             * 一对一信息提交
             *
             * @param p
             * @return
             */
            @Override
            public NetSchoolResponse one2One(String p) {
                log.error("UserCourseService V6 one2One fall back params:{}, fallback reason:{}", p, throwable);
                return NetSchoolResponse.DEFAULT;
            }

            /**
             * 一对一信息获取
             *
             * @param p
             * @return
             */
            @Override
            public NetSchoolResponse obtainOne2One(String p, int terminal) {
                log.error("UserCourseService V6 obtainOne2One fall back params:{}, terminal:{}, fallback reason:{}", p, terminal, throwable);
                return NetSchoolResponse.DEFAULT;
            }
        };
    }
}
