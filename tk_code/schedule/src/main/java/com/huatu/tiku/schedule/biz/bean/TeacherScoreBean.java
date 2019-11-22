package com.huatu.tiku.schedule.biz.bean;

import lombok.Getter;
import lombok.Setter;

/**
 * 智能排课教师评分
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
public class TeacherScoreBean {

	/**
	 * 教师ID
	 */
	private Long id;

	/**
	 * 教师姓名
	 */
	private String name;

	/**
	 * 评分
	 */
	private Integer score;
}
