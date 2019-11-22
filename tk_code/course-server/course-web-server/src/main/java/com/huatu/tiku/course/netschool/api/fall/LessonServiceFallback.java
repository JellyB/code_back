package com.huatu.tiku.course.netschool.api.fall;


import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.v6.LessonServiceV6;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-03-13 1:25 PM
 **/

@Slf4j
@Component
public class LessonServiceFallback implements LessonServiceV6 {

    /**
     * 图书扫码听课详情
     *
     * @param params
     * @return
     */
    @Override
    public NetSchoolResponse playLesson(Map<String, Object> params) {
        return NetSchoolResponse.DEFAULT_ERROR;
    }

    /**
     * 课件收藏列表
     *
     * @param params
     * @return
     */
    @Override
    public NetSchoolResponse collections(Map<String, Object> params) {
        return NetSchoolResponse.DEFAULT_ERROR;
    }

    /**
     * 课件添加收藏
     *
     * @param params
     * @return
     */
    @Override
    public NetSchoolResponse collectionAdd(Map<String, Object> params) {
        return NetSchoolResponse.DEFAULT_ERROR;
    }

    /**
     * 课件取消收藏
     *
     * @param params
     * @return
     */
    @Override
    public NetSchoolResponse collectionCancel(Map<String, Object> params) {
        return NetSchoolResponse.DEFAULT_ERROR;
    }

    /**
     * 我的学习时长
     *
     * @param params 请求参数a
     * @return
     */
    @Override
    public NetSchoolResponse studyReport(Map<String, Object> params) {
        return NetSchoolResponse.DEFAULT_ERROR;
    }

    /**
     * 通过roomI的, 直播回放课件id，查找直播课件id
     *
     * @param params
     * @return
     */
    @Override
    public NetSchoolResponse obtainLiveWareId(Map<String, Object> params) {
        return NetSchoolResponse.DEFAULT_ERROR;
    }


    /**
     * 检查课件是否收藏
     *
     * @param prams
     * @return
     */
    @Override
    public NetSchoolResponse isCollection(Map<String, Object> prams) {
        return NetSchoolResponse.DEFAULT_ERROR;
    }
}
