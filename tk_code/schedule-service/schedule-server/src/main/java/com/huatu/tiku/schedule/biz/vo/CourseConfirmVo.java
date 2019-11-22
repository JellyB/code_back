package com.huatu.tiku.schedule.biz.vo;

import java.io.Serializable;

import com.huatu.tiku.schedule.biz.enums.CourseConfirmStatus;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 课程确认显示信息
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
@ToString
public class CourseConfirmVo implements Serializable {

	private static final long serialVersionUID = -4085191186879887982L;

	/**
	 * 授课内容
	 */
	private String courseLiveName;

	/**
	 * 授课时间
	 */
	private String date;

	/**
	 * 授课类型
	 */
	private String courseCategory;

	/**
	 * 上课地点
	 */
	private String place;

	/**
	 * 所属课程
	 */
	private String courseName;

	/**
	 * 状态
	 */
	private CourseConfirmStatus courseConfirmStatus;

	/**
	 * 过期标志
	 */
	private Boolean expire;

	/**
	 * 课程教师ID
	 */
	private Long courseLiveTeacherId;
}
