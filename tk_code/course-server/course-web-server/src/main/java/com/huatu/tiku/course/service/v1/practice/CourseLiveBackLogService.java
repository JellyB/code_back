package com.huatu.tiku.course.service.v1.practice;

import com.huatu.tiku.course.consts.SimpleCourseLiveBackLog;
import com.huatu.tiku.entity.CourseLiveBackLog;

import service.BaseServiceHelper;

/**
 * 直播转回放
 * 
 * @author zhangchong
 *
 */
public interface CourseLiveBackLogService extends BaseServiceHelper<CourseLiveBackLog> {

	/**
	 * 根据 roomId 直播课件id 查询绑定关系
	 */
	@Deprecated
	CourseLiveBackLog findByRoomIdAndLiveCoursewareId(Long roomId, Long coursewareId);


	/**
	 * 根据 roomId 直播课件id 查询绑定关系 v2
	 */
	SimpleCourseLiveBackLog findByRoomIdAndLiveCourseWareIdV2(Long roomId, Long courseWareId);
}
