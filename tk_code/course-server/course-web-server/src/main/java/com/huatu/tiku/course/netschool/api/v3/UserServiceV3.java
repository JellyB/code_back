package com.huatu.tiku.course.netschool.api.v3;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author hanchao
 * @date 2017/10/12 16:49
 */
@FeignClient("course-service")
public interface UserServiceV3 {
    /**
     * 修改用户昵称
     */
    @GetMapping("/v3/modifyNickName.php")
    NetSchoolResponse updateNickname(@RequestParam("p")String p);
}
