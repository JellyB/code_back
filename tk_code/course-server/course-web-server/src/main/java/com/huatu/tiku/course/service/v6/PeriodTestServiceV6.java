package com.huatu.tiku.course.service.v6;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.huatu.ztk.paper.vo.PeriodTestSubmitlPayload;

/**
 * 阶段测试上报php
 * 
 * @author zhangchong
 *
 */
public interface PeriodTestServiceV6 {

	/**
	 * 同步阶段测试状态到php
	 * 
	 * @param payload
	 * @return
	 */
	void uploadPeriodStatus2PHP(PeriodTestSubmitlPayload payload);

}
