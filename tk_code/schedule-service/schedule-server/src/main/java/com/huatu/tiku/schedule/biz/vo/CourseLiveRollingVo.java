package com.huatu.tiku.schedule.biz.vo;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 滚动排课查询
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
@ToString
public class CourseLiveRollingVo implements Serializable {

	private static final long serialVersionUID = -4085191186879887982L;

	/**
	 * 直播ID
	 */
	private Long id;

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
	 * 时间段
	 */
	public String getTimePeriod() {
		return timeBegin + "-" + timeEnd;
	}

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

}
