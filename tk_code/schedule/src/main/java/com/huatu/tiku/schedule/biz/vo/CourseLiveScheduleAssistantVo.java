package com.huatu.tiku.schedule.biz.vo;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 课表助教
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
@ToString
public class CourseLiveScheduleAssistantVo implements Serializable {

	private static final long serialVersionUID = -4085191186879887982L;

	/**
	 * 直播ID
	 */
	private Long courseLiveId;

	/**
	 * 日期
	 */
	private String date;

	/**
	 * 开始时间
	 */
	private String timeBegin;

	/**
	 * 结束时间
	 */
	private String timeEnd;

	/**
	 * 课程名称
	 */
	private String courseName;

	/**
	 * 直播内容
	 */
	private String courseLiveName;

	/**
	 * 科目
	 */
	private String subject;

//	/**
//	 * 直播间
//	 */
//	private String liveRoom;

	/**
	 * 场控
	 */
	private String controllerName;

	/**
	 * 助教
	 */
	private String assistantName;

	/**
	 * 主持人
	 */
	private String compereName;

	/**
	 * 学习师
	 */
	private String learningTeacherName;

	/**
	 * 考试类型
	 */
	private String examType;

	private String place;//上课地点

	private String categoryName;//课程类型
	/**
	 * 滚动排课直播ID
	 */
	private Long sourceId;

	private String teacherNames;

	@Override
	public int hashCode() {
		return this.courseLiveId.intValue();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else {
			CourseLiveScheduleAssistantVo target = (CourseLiveScheduleAssistantVo) obj;

			return this.getCourseLiveId().equals(target.getCourseLiveId());
		}
	}
}
