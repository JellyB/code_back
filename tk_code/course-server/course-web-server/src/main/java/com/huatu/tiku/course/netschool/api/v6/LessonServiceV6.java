package com.huatu.tiku.course.netschool.api.v6;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.fall.LessonServiceFallback;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-17 下午6:23
 **/

@FeignClient(value = "o-course-service", path = "/lumenapi", fallback = LessonServiceFallback.class)
public interface LessonServiceV6 {


    /**
     * 图书扫码听课详情
     * @param params
     * @return
     */
    @GetMapping(value = "/v5/c/lesson/play_lessions")
    NetSchoolResponse playLesson(@RequestParam Map<String, Object> params);


    /**
     * 课件收藏列表
     * @param params
     * @return
     */
    @GetMapping(value = "/v5/c/lesson/collection_list")
    NetSchoolResponse collections(@RequestParam Map<String, Object> params);


    /**
     * 课件添加收藏
     * @param params
     * @return
     */
    @PutMapping(value = "/v5/c/lesson/collection_add")
    NetSchoolResponse collectionAdd(@RequestParam Map<String, Object> params);

    /**
     * 课件取消收藏
     * @param params
     * @return
     */
    @PutMapping(value = "/v5/c/lesson/collection_cancel")
    NetSchoolResponse collectionCancel(@RequestParam Map<String, Object> params);

    /**
     * 检查课件是否收藏
     * @param prams
     * @return
     */
    @GetMapping(value = "/v5/c/lesson/is_collection")
    NetSchoolResponse isCollection(@RequestParam Map<String, Object> prams);

    /**
     * 我的学习时长
     * @param params 请求参数a
     * @return
     */
    @GetMapping(value = "/v5/c/lesson/study_report")
    NetSchoolResponse studyReport(@RequestParam Map<String, Object> params);


    /**
     * 通过roomI的, 直播回放课件id，查找直播课件id
     * @param params
     * @return
     */
    @GetMapping(value = "/v5/c/lesson/live_ids")
    NetSchoolResponse obtainLiveWareId(@RequestParam Map<String, Object> params);

}
