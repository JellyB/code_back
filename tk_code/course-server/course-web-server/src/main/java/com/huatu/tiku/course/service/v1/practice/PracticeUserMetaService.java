package com.huatu.tiku.course.service.v1.practice;

import java.util.Map;

import com.huatu.tiku.entity.CoursePracticeUserMeta;

import service.BaseServiceHelper;

/**
 * Created by lijun on 2019/3/7
 */
public interface PracticeUserMetaService extends BaseServiceHelper<CoursePracticeUserMeta> {

	/**
	 * 根据百家云房间id 和课件id获取用户答题卡
	 * 
	 * @param roomId
	 * @param userId
	 * @param courseId
	 * @return
	 */
	Long getLiveCourseIdListByRoomId(Long roomId, Long userId, Integer courseId);
	
	/**
	 * 根据直播房间id和课件id获取班级统计数据
	 * @param roomId
	 * @param courseId
	 * @return
	 */
	Map<String,Integer> getCountDateByRIdAndCId(Long roomId, Long courseId);
}
