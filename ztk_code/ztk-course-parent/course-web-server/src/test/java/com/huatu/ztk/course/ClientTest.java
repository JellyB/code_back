package com.huatu.ztk.course;

import com.huatu.ztk.course.common.CourseClient;
import com.huatu.ztk.course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

/**
 * Created by linkang on 11/30/16.
 */
public class ClientTest extends BaseTest{
    @Autowired
    private CourseClient courseClient;

    @Autowired
    private CourseService courseService;

    @Resource(name = "redisTemplate")
    private RedisTemplate<String, String> redisTemplate;
}
