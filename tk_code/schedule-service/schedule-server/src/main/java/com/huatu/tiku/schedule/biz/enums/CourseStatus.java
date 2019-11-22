package com.huatu.tiku.schedule.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;

/**
 * 课程状态
 * 
 * @author wangjian
 **/
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum CourseStatus {

	ZBAP("课程安排"), JSAP("讲师安排"), JSQR("讲师确认"), ZJAP("助教安排"), ZJQR("助教确认"), WC("完成");

	/**
	 * 值
	 */
	private String value;

	/**
	 * 显示字体
	 */
	private String text;

	private CourseStatus(String text) {
		this.value = name();
		this.text = text;
	}

}
