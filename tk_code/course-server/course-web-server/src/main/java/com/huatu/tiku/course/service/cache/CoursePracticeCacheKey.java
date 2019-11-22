package com.huatu.tiku.course.service.cache;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Splitter;
import com.huatu.tiku.course.bean.practice.UserCourseBo;

/**
 * key 前缀 course-web-server. Created by lijun on 2019/2/27
 */
public final class CoursePracticeCacheKey {

	/**
	 * 统计hash中做对题数
	 */
	public static String RCOUNT = "rcount";

	/**
	 * 统计hash中总用时
	 */
	public static String TOTALTIME = "totalTime";

	/**
	 * 已经送过金币的学员
	 */
	public static String GIVECOINKEY = "course:practice:coin:key";

	/**
	 * 每日已送金币数
	 */
	public static String GIVECOINDAILYKEY = "course:practice:coin:daily:key:";

	/**
	 * 获取用户个人信息统计
	 *
	 * @param userId   用户ID
	 * @param courseId 课程ID
	 * @return 用户个人信息统计 key
	 */
	public static String userMetaKey(Integer userId, Long courseId) {
		return "course:practice:userMeta:" + userId + ":" + courseId;
	}

	/**
	 * 获取房间答题信息统计
	 *
	 * @param roomId 获取用户答题信息统计
	 * @return 房间答题信息统计
	 */
	public static String roomRankKey(Long roomId) {
		return "course:practice:roomRank:" + roomId;
	}

	/**
	 * 房间中已经练习试题
	 */
	public static String roomPractedQuestionNumKey(Long roomId) {
		return "course:practice:roomPracticedQuestionNumKey:" + roomId;
	}

	/**
	 * 获取试题统计信息key
	 *
	 * @param roomId     房间ID
	 * @param questionId 试题信息
	 * @return 获取试题统计信息key
	 */
	public static String questionMetaKey(Long roomId, Long questionId) {
		return "course:practice:questionMeta:" + questionId + ":" + roomId;
	}

	/**
	 * 房间内所有答题 用户id+课程id set
	 * 
	 * @param roomId
	 * @return
	 */
	public static String roomInfoMetaKey(Long roomId) {
		return "course:practice:roomInfoMeta:" + roomId;
	}

	/**
	 * 获取一次听课答题中默认key 失效时间
	 */
	public static Integer getDefaultKeyTTL() {
		return 7;
	}

	public static TimeUnit getDefaultTimeUnit() {
		return TimeUnit.DAYS;
	}

	/**
	 * 根据userMetaKey获取userid和courseid
	 * 
	 * @param userMetaKey
	 * @return
	 */
	public static UserCourseBo getUserAndCourseByUserMetaKey(String userMetaKey) {
		List<String> ret = Splitter.on(":").trimResults().splitToList(userMetaKey);
		return UserCourseBo.builder().userId(Integer.valueOf(ret.get(3))).courseId(Long.parseLong(ret.get(4))).build();

	}

	/**
	 * 指定课中答对题的总题数
	 * 使用持久化存储数据
	 *
	 * @param courseId
	 * @return
	 */
	public static String roomRightQuestionSum(Long courseId) {
		return "course:practice:roomRightQuestionSum:" + courseId;
	}

	/**
	 * 指定课中总答题人数
	 * 使用持久化存储数据
	 *
	 * @param courseId
	 * @return
	 */
	public static String roomAllUserSum(Long courseId) {
		return "course:practice:roomAllUserSum:" + courseId;
	}

	/**
	 * 获取课件下试题统计信息key
	 *
	 * @param coursewareId 课件ID
	 * @param questionId   试题信息
	 * @return 获取试题统计信息key
	 */
	public static String questionCoursewareMetaKey(Long roomId, Long coursewareId, Long questionId) {
		return "course:practice:courseware:questionMeta:" + roomId + ":" + questionId + ":" + coursewareId;
	}

	/**
	 * 直播班级统计数据用户查看
	 * 
	 * @param roomId
	 * @param coursewareId
	 * @param type         2直播 1录播
	 * @return
	 */
	public static String roomIdCourseIdTypeMetaKey(Long roomId, Long coursewareId, Integer type) {
		return "course:practice:courseware:roomIdCourseIdTypeMetaKey" + roomId + ":" + coursewareId + ":" + type;
	}

	/**
	 * 直播班级统计人数
	 * 
	 * @param roomId
	 * @return
	 */
	public static String roomIdUserMetaKey(Long roomId, Long coursewareId, Integer type) {
		return "course:practice:courseware:roomIdCourseIdTypeUserMetaKey" + roomId + ":" + coursewareId;
	}

	/**
	 * 获取赠送金币每日限额
	 * @param userId
	 * @return
	 */
	public static String getCoinDailyKey(Integer userId) {
		return GIVECOINDAILYKEY + userId;
	}
}
