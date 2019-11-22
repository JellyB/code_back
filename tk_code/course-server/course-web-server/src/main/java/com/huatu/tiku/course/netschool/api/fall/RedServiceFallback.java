package com.huatu.tiku.course.netschool.api.fall;

import org.springframework.stereotype.Component;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.v5.RedPackageServiceV6;

import lombok.extern.slf4j.Slf4j;

/**
 * 描述：
 *
 * @author biguodong Create time 2019-03-07 5:00 PM
 **/

@Slf4j
@Component
public class RedServiceFallback implements RedPackageServiceV6 {
	@Override
	public NetSchoolResponse showRedEvn() {
		return NetSchoolResponse.DEFAULT_ERROR;
	}

}
