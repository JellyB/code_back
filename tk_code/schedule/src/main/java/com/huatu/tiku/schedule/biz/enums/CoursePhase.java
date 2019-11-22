package com.huatu.tiku.schedule.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;

/**
 * 课程阶段
 * 
 * @author Geek-S
 *
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum CoursePhase {

	ONE("阶段一"), TWO("阶段二"), THREE("阶段三"), FOUR("阶段四"), FIVE("阶段五"), SIX("阶段六"), SEVEN("阶段七"), EIGHT("阶段八"), NINE("阶段九"), TEN("阶段十");
	/**
	 * 值
	 */
	private String value;
	/**
	 * 显示字体
	 */
	private String text;

	private CoursePhase(String text) {
		this.value=name();
		this.text = text;
	}

	public static CoursePhase create(Integer value){
		for(CoursePhase coursePhase: CoursePhase.values()){
			if(coursePhase.ordinal() == (value - 1)){
				return coursePhase;
			}
		}
		return null;
	}

}
