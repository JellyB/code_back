package com.huatu.tiku.schedule.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;

/**
 * 教师授课级别
 * 
 * @author Geek-S
 *
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TeacherCourseLevel {

	COMMON("授课"), GOOD("专长"), MASTER("专注（组长）");
	/**
	 * 值
	 */
	private String value;
	/**
	 * 显示字体
	 */
	private String text;

	private TeacherCourseLevel(String text) {
		this.value=name();
		this.text = text;
	}

	public static TeacherCourseLevel create(Integer value){
		for(TeacherCourseLevel teacherCourseLevel: TeacherCourseLevel.values()){
			if(teacherCourseLevel.ordinal() == value){
				return teacherCourseLevel;
			}
		}
		return null;
	}

}
