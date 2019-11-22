package com.huatu.tiku.course.mq.listeners;

import com.alibaba.fastjson.JSONObject;
import com.huatu.tiku.course.service.v1.practice.LiveCallBackService;
import com.huatu.tiku.essay.constant.course.CallBack;
import com.huatu.tiku.essay.constant.status.SystemConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 直播转回放 civil 消费
 * 
 * @author biguodong
 *
 */
@Slf4j
@Component
public class CourseCallBackCivilListener {
	
	@Autowired
	private LiveCallBackService liveCallBackService;

	@RabbitListener(queues= SystemConstant.CALL_BACK_FAN_OUT_CIVIL)
	public void onMessage(String message) {
		log.error("直播转回放 step 2 civil 消费:{}", message);
		CallBack callBack = JSONObject.parseObject(message, CallBack.class);
		liveCallBackService.liveCallBackAllInfo(callBack);
	}
}
