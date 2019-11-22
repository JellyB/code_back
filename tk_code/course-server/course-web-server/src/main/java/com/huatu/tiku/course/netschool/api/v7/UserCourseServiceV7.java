package com.huatu.tiku.course.netschool.api.v7;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.fall.UserCourseServiceV6FallBack;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-11-26 下午5:28
 **/

@FeignClient(value = "o-course-service", path = "/lumenapi",fallback = UserCourseServiceV6FallBack.class)
public interface UserCourseServiceV7 {

	/**
	 * 直播学习记录上报，到php
	 * @param params
	 * @return
	 */
	@PostMapping(value = "/v4/common/user/live_record")
	NetSchoolResponse saveLiveRecord(@RequestParam Map<String, Object> params);
	
	
}
