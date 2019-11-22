package com.huatu.ztk.course.controller;

import com.google.common.collect.ImmutableMap;
import com.huatu.ztk.course.common.CourseClient;
import com.huatu.ztk.course.common.CourseFallbackConfig;
import com.huatu.ztk.course.common.NetSchoolUrl;
import com.huatu.ztk.course.service.biz.CourseBizServiceMock;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by shaojieyue
 * Created time 2016-12-05 20:51
 */

@RestController
@RequestMapping(value = "v1/courses",produces = MediaType.TEXT_HTML_VALUE+ ";charset=UTF-8")
public class CourseHtmlController {
    private static final Logger logger = LoggerFactory.getLogger(CourseHtmlController.class);

    @Autowired
    private CourseClient courseClient;

    @Resource(name = "coreRedisTemplate")
    private RedisTemplate redisTemplate;

    @Autowired
    private CourseFallbackConfig courseFallbackConfig;

    @Autowired
    private CourseBizServiceMock courseBizServiceMock;

    public static final String COURSE_HTML_KEY = "course.course_h5_";

    /**
     * h5页面
     * @param courseId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "{courseId}",produces = MediaType.TEXT_HTML_VALUE+ ";charset=UTF-8")
    public Object detail(@PathVariable int courseId) throws Exception{
        if(courseFallbackConfig.getSpecialInfo() == 1 && courseFallbackConfig.containsCourseId(courseId)){
            return courseBizServiceMock.getCourseH5Mock(courseId);
        }
        String key = COURSE_HTML_KEY + courseId;
        Object result = redisTemplate.opsForValue().get(key);
        if(result == null){
            result = courseClient.getHttpData(NetSchoolUrl.COURSE_H5 + "?rid=" + courseId, null);
            redisTemplate.opsForValue().set(key,result,30, TimeUnit.MINUTES);//缓存30分钟
        }
        return result;
    }

    /**
     * 临时使用，在新的版本使用统一更完整的策略
     * @param courseId
     * @return
     */
    @RequestMapping(value = "{courseId}/_rmCache")
    public Object deleteCache(@PathVariable int courseId){
        String key = COURSE_HTML_KEY + courseId;
        redisTemplate.delete(key);
        return ImmutableMap.of("result","删除成功");
    }

    /**
     * ios审核h5页面
     * @param courseId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "static/{courseId}",produces = MediaType.TEXT_HTML_VALUE+ ";charset=UTF-8")
    public Object auditDetail(@PathVariable int courseId) throws Exception{
        String filePath = System.getProperty("server_resources") + "/webapp/WEB-INF/pages/ios_audit/" + courseId + ".html";

        File file = new File(filePath);
        if (file.exists()) {
            return FileUtils.readFileToString(file);
        } else {
            return detail(courseId);
        }
    }
}
