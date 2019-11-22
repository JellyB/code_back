package com.huatu.tiku.schedule.biz.service;

import com.huatu.tiku.schedule.base.service.BaseService;
import com.huatu.tiku.schedule.biz.domain.ConfirmToken;
import com.huatu.tiku.schedule.biz.enums.CourseConfirmStatus;

/**
 * 教师确认认证Service
 * 
 * @author Geek-S
 *
 */
public interface ConfirmTokenService extends BaseService<ConfirmToken, Long> {

	/**
	 * 确认课程
	 * 
	 * @param token
	 *            Token
	 * @param courseConfirmStatus
	 *            确认状态
	 */
	void confirm(String token, CourseConfirmStatus courseConfirmStatus);

	/**
	 * 根据Token查询
	 * 
	 * @param token
	 *            token
	 * @return 教师确认认证
	 */
	ConfirmToken findByToken(String token);

}
