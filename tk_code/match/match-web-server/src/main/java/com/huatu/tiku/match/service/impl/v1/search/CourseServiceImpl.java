package com.huatu.tiku.match.service.impl.v1.search;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.huatu.tiku.match.bo.CourseInfoBo;
import com.huatu.tiku.match.common.FeignResponse;
import com.huatu.tiku.match.common.MatchInfoRedisKeys;
import com.huatu.tiku.match.service.v1.search.CourseService;
import com.huatu.tiku.match.ztk.api.CourseFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 描述：
 *
 * @author biguodong Create time 2019-01-09 下午5:45
 **/
@Service
@Slf4j
public class CourseServiceImpl implements CourseService {

	private static final String DEFAULT_COURSE_TITLE = "名师深度解析";

	@Autowired
	private CourseFeignClient courseFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    private final static String COURSE_NULL = "course_null";
    /**
     * 根据classId获取课程信息
     *
     * @param classId
     * @return
     */
    @Override
    public Object courseInfo(int classId) {
        CourseInfoBo courseInfoBo = new CourseInfoBo();
        courseInfoBo.setClassId(classId);
        Map<String,Object> params = Maps.newHashMap();
        LinkedHashMap linkedHashMap = Maps.newLinkedHashMap();
        params.put("classIds", classId);
        ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
        String key = MatchInfoRedisKeys.getCourseAnalysisInfo(classId);
        try{
            String valueStr = valueOperations.get(key);
            if(StringUtils.isNotBlank(valueStr)){
                if(COURSE_NULL.equals(valueStr)){
                    return courseInfoBo;
                }
                CourseInfoBo tempCourseInfo = JSONObject.parseObject(valueStr, CourseInfoBo.class);
                BeanUtils.copyProperties(tempCourseInfo, courseInfoBo);
                return courseInfoBo;
            }
        }catch (Exception e){
            redisTemplate.delete(key);
            log.error("obtain course info from cache error:{}", classId);
        }
        FeignResponse feignResponse = courseFeignClient.analysis(params);
        if(null != feignResponse.getData()){
            linkedHashMap = (LinkedHashMap) feignResponse.getData();
            if(linkedHashMap.containsKey(String.valueOf(classId))){
                LinkedHashMap content = (LinkedHashMap)linkedHashMap.get(String.valueOf(classId));
                if(content.containsKey(CourseFeignClient.LIVE_DATE) && content.containsKey(CourseFeignClient.PRICE)){
                    courseInfoBo.setLiveDate(NumberUtils.toLong(String.valueOf(content.get(CourseFeignClient.LIVE_DATE))));
                    courseInfoBo.setPrice(NumberUtils.toInt(String.valueOf(content.get(CourseFeignClient.PRICE))));
                    if(StringUtils.isEmpty(String.valueOf(content.get(CourseFeignClient.COURSE_TITLE)))){
                        log.info("课程缓存时间：1分钟");
                        valueOperations.set(key, JSONObject.toJSONString(courseInfoBo), 1, TimeUnit.MINUTES);
                    }else{
                        log.info("课程缓存时间：1小时");
                        valueOperations.set(key, JSONObject.toJSONString(courseInfoBo), 1, TimeUnit.HOURS);
                    }
                    return courseInfoBo;
                }
            }else{
                log.info("obtain course info from php client error, classId:{}", classId);
            }
        }
        valueOperations.set(key, COURSE_NULL, 1, TimeUnit.MINUTES);
        return courseInfoBo;
    }

}
