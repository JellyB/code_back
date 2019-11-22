package com.huatu.tiku.course.service.v5.impl;

import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.v5.CourseServiceV5;
import com.huatu.tiku.course.service.cache.CacheUtil;
import com.huatu.tiku.course.service.cache.CourseCacheKey;
import com.huatu.tiku.course.service.v5.CourseServiceV5Biz;
import com.huatu.tiku.course.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Created by lijun on 2018/6/25
 */
@Service
@Slf4j
public class CourseServiceV5BizImpl implements CourseServiceV5Biz {

    @Autowired
    private CacheUtil cacheUtil;

    @Autowired
    private CourseServiceV5 courseService;

    @Override
    public String getClassExt(int classId, int terminal) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Supplier key = () -> CourseCacheKey.classExtKeyV5(classId, terminal);
        Supplier<String> value = () -> {
            String classExt = courseService.getClassExt(classId, terminal);
            return classExt;
        };
        String result =  cacheUtil.getCacheStringValue(key, value, 2, TimeUnit.MINUTES);
        stopWatch.stop();
        log.info("获取课程说明-返回H5:{}", stopWatch.prettyPrint());
        return result;
    }

    @Override
    public Object findTimetable(int classId, int parentId, int page, int pageSize) {
        Supplier key = () -> CourseCacheKey.timeTableKeyV5(classId, parentId, page, pageSize);
        Supplier<Object> value = () -> {
            HashMap<String, Object> map = HashMapBuilder.<String, Object>newBuilder()
                    .put("classId", classId)
                    .put("parentId", parentId)
                    .put("page", page)
                    .put("pageSize", pageSize)
                    .build();
            NetSchoolResponse timetable = courseService.findTimetable(map);
            return ResponseUtil.build(timetable);
        };
        if (page == 1) {//只缓存第一页数据
            return cacheUtil.getCacheStringValue(key, value, 5, TimeUnit.MINUTES);
        }
        return value.get();
    }


    @Override
    public Object findPurchasesTimetable(long userId, HashMap<String, Object> map) {
        NetSchoolResponse purchasesTimetable = courseService.findPurchasesTimetable(map);
        return ResponseUtil.build(purchasesTimetable);
    }

    @Override
    public Object appClassActivityDetails(int classId, int terminal) {
        Supplier key = () -> CourseCacheKey.appClassActivityDetailsKeyV5(classId);
        Supplier<Object> value = () -> {
            NetSchoolResponse response = courseService.appClassActivityDetails(classId, terminal);
            return ResponseUtil.build(response);
        };
        return cacheUtil.getCacheStringValue(key, value, 30, TimeUnit.SECONDS);
    }
}
