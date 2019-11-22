package com.huatu.tiku.course.test;

import com.huatu.common.test.BaseWebTest;
import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.tiku.course.service.v1.CourseBreakpointQuestionService;
import com.huatu.tiku.course.service.v1.CourseBreakpointService;
import com.huatu.tiku.course.util.ZTKResponseUtil;
import com.huatu.tiku.course.ztk.api.v1.paper.PracticeCardServiceV1;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by lijun on 2018/6/19
 */
public class CourseQuestionTest extends BaseWebTest {

    @Autowired
    CourseBreakpointService service;

    @Autowired
    CourseBreakpointQuestionService questionService;

    @Autowired
    PracticeCardServiceV1 practiceCardServiceV1;

    @Autowired
    private RedisTemplate redisTemplate;

    @Resource(name = "redisTemplate")
    private ValueOperations<String, Integer> valueOperations;

    @Test
    public void test() {
//        List<Map<String, Object>> mapList = questionService.listQuestionIdByPointId(2L);
//        System.out.println(mapList.size());

        ArrayList<HashMap<String, Object>> paramsList = new ArrayList<>();
        paramsList.add(
                HashMapBuilder.<String, Object>newBuilder()
                        .put("courseType", 0)
                        .put("courseId", 917519)
                        .build()
        );
        paramsList.add(
                HashMapBuilder.<String, Object>newBuilder()
                        .put("courseType", 0)
                        .put("courseId", 917577)
                        .build()
        );
        Object courseExercisesCardInfo = practiceCardServiceV1.getCourseBreakPointCardInfo(233906496L, paramsList);
        Object build = ZTKResponseUtil.build(courseExercisesCardInfo);
        System.out.println(build.toString());
    }

    @Test
    public void testKey() {
        redisTemplate.opsForHash().put("_test:hash:key", "test", "test");
    }

    @Test
    public void testTransactional() {
        RedisAtomicInteger num = new RedisAtomicInteger("num", redisTemplate);
        boolean compareAndSet = num.compareAndSet(2, 3);
        System.out.println(compareAndSet);
    }

    private final static String getTestNumKey() {
        return "testNum";
    }

    private static void sleep() {
        try {
            for (; ; ) {
                Thread.sleep(5 * 1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
