package com.huatu.tiku.course.test;

import com.huatu.common.test.BaseWebTest;
import com.huatu.tiku.course.service.v1.practice.CourseLiveBackLogService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-08-21 9:53 AM
 **/

@Slf4j
public class CourseLiveBackLogServiceTest extends BaseWebTest {


    @Autowired
    private CourseLiveBackLogService courseLiveBackLogService;


    @Test
    public void test(){
        courseLiveBackLogService.findByRoomIdAndLiveCourseWareIdV2(60571125512l, 925511l);
    }
}
