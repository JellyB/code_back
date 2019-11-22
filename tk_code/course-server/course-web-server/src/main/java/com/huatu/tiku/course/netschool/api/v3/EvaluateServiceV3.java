package com.huatu.tiku.course.netschool.api.v3;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author hanchao
 * @date 2017/9/20 9:31
 */
@FeignClient(value = "course-service")
public interface EvaluateServiceV3 {
    /**
     * 评价列表
     * @param classid
     * @param lessionid
     * @param page
     * @return
     */
    @PostMapping("/v3/score_list.php")
    NetSchoolResponse findEvaluateList(@RequestParam("classid")int classid,@RequestParam("lessionid")int lessionid,@RequestParam("page")int page,@RequestParam("username")String username);

    /**
     * 获取自己对某个课程的评价
     * @param params
     * @return
     */
    @PostMapping("/v3/click_comments.php")
    NetSchoolResponse getEvaluete(@RequestParam Map<String,Object> params);

    /**
     * 获取自己对某个课程的评价
     * @param params
     * @return
     */
    @PostMapping("/v3/course_evaluation.php")
    NetSchoolResponse saveEvaluate(@RequestParam Map<String,Object> params);

}
