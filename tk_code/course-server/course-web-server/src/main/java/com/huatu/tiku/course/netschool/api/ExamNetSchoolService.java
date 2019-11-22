package com.huatu.tiku.course.netschool.api;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/2/15
 * @描述 代理备考精华接口
 */

@FeignClient(value = "o-course-service")
public interface ExamNetSchoolService {

    @GetMapping("/lumenapi/v5/c/article/article")
    NetSchoolResponse getArticleList(@RequestParam Map<String, Object> params);

    @GetMapping("/lumenapi/v5/c/article/detail")
    NetSchoolResponse detail(@RequestParam Map<String,Object> params);

    @PostMapping("/lumenapi/v5/c/article/like")
    NetSchoolResponse like(@RequestParam Map<String, Object> params);

}
