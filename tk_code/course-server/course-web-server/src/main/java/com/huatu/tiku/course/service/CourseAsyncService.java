package com.huatu.tiku.course.service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.common.exception.BizException;
import com.huatu.common.spring.cache.Cached;
import com.huatu.common.utils.cache.NullHolder;
import com.huatu.common.utils.concurrent.ConcurrentBizLock;
import com.huatu.springboot.degrade.core.Degrade;
import com.huatu.tiku.course.bean.*;
import com.huatu.tiku.course.netschool.api.CourseServiceV1;
import com.huatu.tiku.course.netschool.api.fall.CourseServiceV3Fallback;
import com.huatu.tiku.course.netschool.api.v3.CourseServiceV3;
import com.huatu.tiku.course.netschool.api.v3.UserCoursesServiceV3;
import com.huatu.tiku.course.util.CourseCacheKey;
import com.huatu.tiku.course.util.RequestUtil;
import com.huatu.tiku.course.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 在同一个类中a
 *
 * @author hanchao
 * @date 2017/9/12 21:46
 */
@Service
@Slf4j
@Async
public class CourseAsyncService {
    @Autowired
    private CourseServiceV1 courseServiceV1;
    @Autowired
    private CourseServiceV3 courseServiceV3;
    @Autowired
    private UserCoursesServiceV3 userCoursesServiceV3;

    @Resource(name = "redisTemplate")
    private ValueOperations valueOperations;

    @Autowired
    private CourseServiceV3Fallback courseServiceV3Fallback;
    @Autowired
    private PromoteBizService promoteBizService;



    @Async
    @Degrade(key = "userBuy", name = "用户已购课程")
    @Deprecated
    public ListenableFuture<Set<Integer>> getUserBuy(String username) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(1);
        params.put("username", username);
        //默认返回，list，里面包含了字符串的产品id
        NetSchoolResponse response = userCoursesServiceV3.findProducts(RequestUtil.encryptParams(params));
        if (response == null || response.getData() == null) {
            return new AsyncResult(Sets.newHashSet());
        } else {
            List<String> data = (List<String>) response.getData();
            Set<Integer> result = data.stream().map(Integer::parseInt).collect(Collectors.toSet());
            return new AsyncResult(result);
        }
    }

    //用户已购课程降级方法
    @Deprecated
    public ListenableFuture<Set<Integer>> getUserBuyDegrade(String username) {
        return new AsyncResult<>(Sets.newHashSet());
    }


    /**
     * 获取用户是否购买
     *
     * @param username
     * @return
     */
    public ListenableFuture<Set<Integer>> hasBuy(int rid,String username) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(1);
        params.put("username", username);
        params.put("rid",rid);
        //默认返回，list，里面包含了字符串的产品id
        NetSchoolResponse response = userCoursesServiceV3.getProductIsBuy(RequestUtil.encryptParams(params));
        if (response == null || response.getData() == null) {
            return new AsyncResult(Sets.newHashSet());
        } else {
            List<String> data = (List<String>) response.getData();
            Set<Integer> result = data.stream().map(Integer::parseInt).collect(Collectors.toSet());
            return new AsyncResult(result);
        }
    }


    /**
     * 获取产品数量限额
     *
     * @param courseId
     * @return
     */
    @Async
    @Degrade(key = "courseLimit", name = "课程已购数量")
    public ListenableFuture<Map<Integer, Integer>> getCourseLimit(int courseId) {

        Map<String, Object> params = Maps.newHashMapWithExpectedSize(2);
        params.put("rid", courseId);
        NetSchoolResponse response = courseServiceV3.getCourseLimit(params);
        if (response == null || response.getData() == null) {
            return new AsyncResult(Maps.<Integer, Integer>newHashMap());
        } else {
            //默认返回，课程id(string)->购买量(string)
            Map<String, String> data = (Map<String, String>) response.getData();
            Map<Integer, Integer> result = data.keySet().stream().collect(Collectors.toMap(Integer::parseInt, (k) -> Integer.parseInt(data.get(k))));
            return new AsyncResult(result);
        }
    }

    //课程已购数量降级方法
    public ListenableFuture<Map<Integer, Integer>> getCourseLimitDegrade(int courseId) {
        return new AsyncResult(Maps.newHashMap());
    }

    /**
     * 老版本拆分课程详情
     *
     * @param courseId
     * @return 会有null情况，需判断
     */
    @Async
    public ListenableFuture<CourseDetailV2DTO> getCourseDetailV2(int courseId) {
        String cacheKey = CourseCacheKey.courseDetailV2(courseId);
        CourseDetailV2DTO result = null;
        Object object = valueOperations.get(cacheKey);
        if (object instanceof NullHolder) {
            return new AsyncResult<>(null);
        }
        if (object == null) {
            Map<String, Object> params = Maps.newHashMap();
            params.put("rid", courseId);
            NetSchoolResponse response = courseServiceV1.courseDetailSp(RequestUtil.encryptParams(params));
            if (response == null) {//fallback等
                return new AsyncResult<>(null);
            }
            if (response.getCode() == -3) { //课程不存在
                valueOperations.set(cacheKey, NullHolder.DEFAULT, 300, TimeUnit.SECONDS);
                return new AsyncResult<>(null);
            }
            if (response.getData() == null) {//其他未知错误
                return new AsyncResult<>(null);
            }
            try {
                result = ResponseUtil.build(response, CourseDetailV2DTO.class, true);
                valueOperations.set(cacheKey, result, 300, TimeUnit.SECONDS);
            } catch (BizException e) {
                log.error("catch BizException,{}", ExceptionUtils.getFullStackTrace(e));
            }
        } else {
            result = (CourseDetailV2DTO) object;
        }
        return new AsyncResult<>(result);
    }


    /**
     * 老版本的异步获取课程列表
     *
     * @param params
     * @return 会有null
     */
    @Async
    public ListenableFuture<CourseListV2DTO> getCourseListV2(Map<String, Object> params) {
        params.remove("username");
        String cacheKey = CourseCacheKey.courseListV2(com.huatu.common.utils.web.RequestUtil.getParamSign(params));
        CourseListV2DTO result = (CourseListV2DTO) valueOperations.get(cacheKey);
        if (result == null) {
            NetSchoolResponse response = courseServiceV1.collectionList(params);
            result = ResponseUtil.build(response, CourseListV2DTO.class, false);
            if (result != null) {
                valueOperations.set(cacheKey, result, 10, TimeUnit.SECONDS);
            }
        }
        return new AsyncResult<>(result);
    }



    /**
     * 异步获取课程详情 v3
     *
     * @param courseId
     * @return 会有null情况，需判断
     */
    @Cached(name = "课程详情v3",
            key = "T(com.huatu.tiku.course.util.CourseCacheKey).courseDetailV3(#rid)",
            params = {@Cached.Param(name = "课程ID", value = "rid", type = Integer.class)})
    @Degrade(name = "课程详情v3", key = "courseDetailV3")
    @Async
    public ListenableFuture<CourseDetailV3DTO> getCourseDetailV3(int courseId) {

        String cacheKey = CourseCacheKey.courseDetailV3(courseId);
        CourseDetailV3DTO result = null;
        Object object = valueOperations.get(cacheKey);
        if (object instanceof NullHolder) {
            return new AsyncResult<>(null);
        }
        if (object == null) {
            NetSchoolResponse response = courseServiceV3.getCourseDetail(courseId);
            if (response == null) {//fallback等
                return new AsyncResult<>(null);
            }
            if (response.getCode() == -3) { //课程不存在
                valueOperations.set(cacheKey, NullHolder.DEFAULT, 300, TimeUnit.SECONDS);
                return new AsyncResult<>(null);
            }
            if (response.getData() == null) {//其他未知错误
                return new AsyncResult<>(null);
            }
            result = ResponseUtil.build(response, CourseDetailV3DTO.class, false);
            valueOperations.set(cacheKey, result, 300, TimeUnit.SECONDS);
            courseServiceV3Fallback.setCourseDetail(courseId, response);
        } else {
            result = (CourseDetailV3DTO) object;
        }
        return new AsyncResult<>(result);
    }

    public ListenableFuture<CourseDetailV3DTO> getCourseDetailV3Degrade(int courseId) {
        NetSchoolResponse courseDetail = courseServiceV3Fallback.getCourseDetail(courseId);
        if(courseDetail.getCode() == NetSchoolResponse.DEFAULT_ERROR.getCode()){
            String key = "_mock_course_detail$"+ courseId;
            if (ConcurrentBizLock.tryLock(key)) {
                try {
                    courseDetail = courseServiceV3.getCourseDetail(courseId);
                    courseServiceV3Fallback.setCourseDetail(courseId, courseDetail);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    ConcurrentBizLock.releaseLock(key);
                }
            }
        }
        CourseDetailV3DTO result = ResponseUtil.build(courseDetail, CourseDetailV3DTO.class, false);
        return new AsyncResult<>(result);
    }

}
