package com.huatu.tiku.course.ztk.api.fail.user;

import java.util.List;

import org.springframework.stereotype.Component;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.ztk.api.v4.user.UserServiceV4;

/**
 * 
 * @author zhangchong
 *
 */
@Component
public class UserServiceV4Fallback implements UserServiceV4 {

	@Override
	public NetSchoolResponse getUserLevelBatch(List<String> userIds) {
		return NetSchoolResponse.DEFAULT_ERROR;
	}

}
