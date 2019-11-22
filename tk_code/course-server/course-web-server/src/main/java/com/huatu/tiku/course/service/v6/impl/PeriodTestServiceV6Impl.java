package com.huatu.tiku.course.service.v6.impl;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.v6.UserCourseServiceV6;
import com.huatu.tiku.course.service.v6.PeriodTestServiceV6;
import com.huatu.ztk.paper.vo.PeriodTestSubmitlPayload;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author zhangchong
 *
 */
@Service
@Slf4j
public class PeriodTestServiceV6Impl implements PeriodTestServiceV6 {

	@Autowired
	private UserCourseServiceV6 userCourseServiceV6;

	@Override
	public void uploadPeriodStatus2PHP(PeriodTestSubmitlPayload payload) {
		HashMap<String, Object> params = Maps.newHashMap();
		params.put("isFinish", 1);
		params.put("syllabusId", payload.getSyllabusId());
		params.put("userName", payload.getUserName());
		NetSchoolResponse res = userCourseServiceV6.stageTestStudyRecord(params);
		log.info("上报阶段测试进度结果:{}大纲id为:{}用户name为:{}", res.getData(), payload.getSyllabusId(), payload.getUserName());
	}

}
