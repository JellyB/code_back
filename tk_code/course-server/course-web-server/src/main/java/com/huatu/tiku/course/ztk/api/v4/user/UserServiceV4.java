package com.huatu.tiku.course.ztk.api.v4.user;

import java.util.List;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.ztk.api.fail.user.UserServiceV4Fallback;

/**
 * 
 * @author zhangchong
 *
 */
@FeignClient(value = "ztk-service", fallback = UserServiceV4Fallback.class, path = "/u")
public interface UserServiceV4 {

	@PostMapping(value = "/v1/users/batchUserInfo")
	NetSchoolResponse getUserLevelBatch(@RequestBody List<String> userIds);

}
