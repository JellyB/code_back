package com.huatu.ztk.course.service.biz;

import com.google.common.collect.Maps;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.course.common.CourseClient;
import com.huatu.ztk.course.common.CourseFallbackConfig;
import com.huatu.ztk.course.common.NetSchoolUrl;
import com.huatu.ztk.course.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.huatu.ztk.course.service.biz.CourseBizService.COURSELIST_KEY;

/**
 * @author hanchao
 * @date 2017/10/1 14:12
 */
@Service
@Slf4j
public class CourseBizServiceMock {

    public static final String COURSELIST_MOCK_KEY = "course.courses_list_mock";
    public static final String COLLECTION_MOCK_KEY = "course.collection_mock_";
    public static final String COURSE_MOCK_KEY = "course.course_mock_";
    public static final String COURSE_H5_MOCK_KEY = "course.course_h5_mock_";

    @Autowired
    private CourseFallbackConfig fallbackConfig;
    @Autowired
    private CourseService courseService;
    @Resource(name = "coreRedisTemplate")
    private ValueOperations<String, Object> valueOperations;
    @Resource(name = "coreRedisTemplate")
    private RedisTemplate redisTemplate;
    @Autowired
    private CourseClient courseClient;



    /**
     * 刷新课程列表，得到默认的列表数据
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void refreshList(){
        String key = COURSELIST_KEY;
        try {
            Object result = courseService.getJson(Maps.newHashMapWithExpectedSize(2), NetSchoolUrl.ALL_COLLECTION_LIST_SP, false);
            //数据验证失败则不缓存,防止mock到错误数据
            if(result != null && result instanceof Map && ((Map) result).containsKey("result") && ((List)((Map) result).get("result")).size() > 0 ){
                ((Map)result).put("_timestamp", System.currentTimeMillis());
                valueOperations.set(key, result, 7, TimeUnit.DAYS);//缓存一周
            }else{
                log.error(">>> data illegal,will not put into cache...");
            }
        } catch (BizException e) {
            log.warn("catch BizException,maybe the response data is null...", e);
        } catch (Exception e) {
            log.error("catch exception",e);
        }
    }

    public Object getCourseListMock(){
        return valueOperations.get(COURSELIST_KEY);
    }


    /**
     * 刷新合集数据
     */
    @Scheduled(cron = "0 0/2 * * * ?")
    public void refreshCollection(){
        Set<String> titles = fallbackConfig.get_titles();
        if(CollectionUtils.isEmpty(titles)){
            return;
        }
        for (String title : titles) {
            Map params = Maps.newHashMap();
            params.put("username","httk_55505c2g");
            params.put("page",1);
            params.put("shortTitle",title);
            params.put("pagesize",Integer.MAX_VALUE);
            Object result = null;
            try {
                result = courseService.getJson(params, NetSchoolUrl.COLLECTION_DETAIL, false);
            } catch (BizException e) {
                log.warn("catch BizException,maybe the response data is null...");
            } catch (Exception e) {
                log.error("catch exception",e);
            }
            if(result != null && result instanceof Map && ((Map) result).containsKey("result") && ((List)((Map) result).get("result")).size() > 0  ){
                ((Map)result).put("_timestamp", System.currentTimeMillis());
                valueOperations.set(COLLECTION_MOCK_KEY+title, result, 7, TimeUnit.DAYS);//缓存一周
            }else{
                log.error(">>> data illegal,will not put into cache...");
            }
        }
    }

    public Object getCollectionListMock(String title){
        return valueOperations.get(COLLECTION_MOCK_KEY+title);
    }

    /**
     * 刷新课程数据
     */
    @Scheduled(cron = "0 0/2 * * * ?")
    public void refreshCourseDetail(){
        Set<Integer> courseIds = fallbackConfig.get_courseIds();
        if(CollectionUtils.isEmpty(courseIds)){
            return;
        }
        for (Integer courseId : courseIds) {
            try {
                Map<String, Object> parameterMap = Maps.newHashMapWithExpectedSize(2);
                parameterMap.put("rid", courseId);
                Object result = null;
                try {
                    result = courseService.getJsonByEncryptParams(parameterMap, NetSchoolUrl.COURSE_DATAIL_NEW_V2, true);
                } catch (BizException e) {
                    log.warn("catch BizException,maybe the response data is null...", e);
                } catch (Exception e) {
                    log.error("catch exception",e);
                }
                if(result != null && result instanceof Map && MapUtils.isNotEmpty((Map) result) && ((Map)result).containsKey("classInfo")){
                    valueOperations.set(COURSE_MOCK_KEY+courseId, result, 7, TimeUnit.DAYS);//缓存一周
                }else{
                    log.error(">>> data illegal,will not put into cache...");
                }
            } catch(Exception e){
                log.error("",e);
            }
            try {
                String key = COURSE_H5_MOCK_KEY+courseId;
                if(!redisTemplate.hasKey(key)){
                    String data = courseClient.getHttpData(NetSchoolUrl.COURSE_H5 + "?rid=" + courseId, null);
                    valueOperations.set(key,data,7, TimeUnit.DAYS);
                }
            } catch(Exception e){
                log.error("",e);
            }
        }
    }

    public Object getCourseDetailMock(int courseId){
        return valueOperations.get(COURSE_MOCK_KEY+courseId);
    }

    public Object getCourseH5Mock(int courseId){
        return valueOperations.get(COURSE_H5_MOCK_KEY+courseId);
    }
}
