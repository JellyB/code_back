package com.huatu.tiku.course.netschool.api.fall;

import com.google.common.collect.Maps;
import com.huatu.common.spring.cache.Cached;
import com.huatu.common.utils.web.RequestUtil;
import com.huatu.tiku.course.bean.CourseListV3DTO;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.v3.CourseServiceV3;
import com.huatu.tiku.course.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.huatu.tiku.course.bean.NetSchoolResponse.DEFAULT_ERROR;

/**
 * @author hanchao
 * @date 2017/10/2 14:53
 */
@Component
@Slf4j
public class CourseServiceV3Fallback implements CourseServiceV3 {

    /**
     * 添加课程缓存
     * @param params
     * @param response
     */
    public void setLiveList(Map<String,Object> params,NetSchoolResponse response){
        String key = "_mock_live_list$"+ RequestUtil.getParamSign(params);
        if(ResponseUtil.isSuccess(response)){
            if(response.getData() instanceof Map){
                Map result = (Map) response.getData();
                result.put("cacheTimestamp",System.currentTimeMillis());
            }
            FallbackCacheHolder.put(key,response);
        }
    }

    public void setCourseDetail(int rid,NetSchoolResponse response){
        String key = "_mock_course_detail$"+ rid;
        if(ResponseUtil.isSuccess(response)){
            FallbackCacheHolder.put(key,response);
        }
    }

    public void setCourseH5(int rid,String response){
        String key = "_mock_course_h5$"+ rid;
        if(StringUtils.isNotBlank(response)){
            FallbackCacheHolder.put(key,response);
        }
    }

    public void setRecordingList(Map<String,Object> params,NetSchoolResponse response){
        String key = "_mock_recoding_list$"+ RequestUtil.getParamSign(params);
        if(ResponseUtil.isSuccess(response)){
            FallbackCacheHolder.put(key,response);
        }
    }


    public void setCollectionDetail(String shortTitle, int page,NetSchoolResponse response){
        String key = "_mock_collection_detail$"+ shortTitle+"$"+page;
        if(ResponseUtil.isSuccess(response)){
            if(response.getData() instanceof Map){
                Map result = (Map) response.getData();
                result.put("cacheTimestamp",System.currentTimeMillis());
            }
            FallbackCacheHolder.put(key,response);
        }
    }

    public void setTimetable(int rid,NetSchoolResponse response){
        String key = "_mock_course_timetable$"+ rid;
        if(ResponseUtil.isSuccess(response)){
            FallbackCacheHolder.put(key,response);
        }
    }


    //----------------------------------------------------------------


    @Override
    public NetSchoolResponse getCollectionDetail(String shortTitle, int page) {
        String key = "_mock_collection_detail$"+ shortTitle+"$"+page;
        NetSchoolResponse response = FallbackCacheHolder.get(key);
        if(response == null){
            return DEFAULT_ERROR;
        }else{
            CourseListV3DTO courseListV3DTO = ResponseUtil.build(response, CourseListV3DTO.class, false);
            courseListV3DTO.setCache(true);
            //洗掉之前数据和所属用户的关联信息
            if (CollectionUtils.isNotEmpty(courseListV3DTO.getResult())) {
                for (Map item : courseListV3DTO.getResult()) {
                    if ("0".equals(String.valueOf(item.get("isCollect"))) && item.containsKey("rid")) {
                        item.put("isBuy", 0);
                    }
                }
            }
            response.setData(courseListV3DTO);
            return response;
        }
    }

    @Override
    public NetSchoolResponse getCourseLimit(Map<String, Object> params) {
        return NetSchoolResponse.newInstance(Maps.newHashMap());
    }

    @Override
    public NetSchoolResponse findRecordingList(Map<String, Object> params) {
        String key = "_mock_recoding_list$"+ RequestUtil.getParamSign(params);
        NetSchoolResponse response = FallbackCacheHolder.get(key);
        if(response == null){
            return DEFAULT_ERROR;
        }else{
            return response;
        }
    }

    @Override
    public NetSchoolResponse findLiveList(Map<String, Object> params) {
        String key = "_mock_live_list$"+ RequestUtil.getParamSign(params);
        NetSchoolResponse response = FallbackCacheHolder.get(key);
        if(response == null){
            log.error("try to obtain data from cache error:{}", params);
            return DEFAULT_ERROR;
        }else{
            CourseListV3DTO courseListV3DTO = ResponseUtil.build(response, CourseListV3DTO.class, false);
            courseListV3DTO.setCache(true);
            response.setData(courseListV3DTO);
            return response;
        }
    }

    @Override
    @Cached(name="课程详情降级缓存",key = "@courseServiceV3Fallback.getCourseDetail(#rid)",sourceType = Cached.DataScourseType.GETINSIDE,
            params = {@Cached.Param(name = "课程id", value = "rid", type = Integer.class)})
    public NetSchoolResponse getCourseDetail(int rid) {
        String key = "_mock_course_detail$"+ rid;
        NetSchoolResponse response = FallbackCacheHolder.get(key);
        if(response == null){
            return DEFAULT_ERROR;
        }else{
            return response;
        }
    }

    @Override
    public String getCourseHtml(int rid) {
        String key = "_mock_course_h5$"+ rid;
        String response = FallbackCacheHolder.get(key);
        if(StringUtils.isNotBlank(response)){
            return response;
        }else{
            return "";
        }
    }

    @Override
    public NetSchoolResponse getCourseSecrInfo(Map<String, Object> params) {
        return DEFAULT_ERROR;
    }

    @Override
    public NetSchoolResponse findTeachersByCourse(int rid) {
        return DEFAULT_ERROR;
    }

    @Override
    public NetSchoolResponse findTimetable(int rid) {
        String key = "_mock_course_timetable$"+ rid;
        NetSchoolResponse response = FallbackCacheHolder.get(key);
        if(response == null){
            return DEFAULT_ERROR;
        }else{
            return response;
        }
    }

    @Override
    public NetSchoolResponse getHandouts(Map<String, Object> params) {
        return DEFAULT_ERROR;
    }

    @Override
    public NetSchoolResponse findBookList(Map<String, Object> params) {
        return DEFAULT_ERROR;
    }
}
