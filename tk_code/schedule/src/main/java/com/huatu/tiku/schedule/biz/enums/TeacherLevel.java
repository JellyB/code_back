package com.huatu.tiku.schedule.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;

/**
 * 教师等级
 * 
 * @author Geek-S
 *
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TeacherLevel {

	H1("H1"), H2("H2"), H3("H3"), H4("H4"), H5("H5"), H6("H6"), H7("H7"), H8("H8"), H9("H9"), H10("H10"), H11(
			"H11"), H12("H12"), H13("H13"), H14("H14"), H15("H15"), H16("H16");

	/**
	 * 值
	 */
	private String value;
	/**
	 * 显示字体
	 */
	private String text;

	private TeacherLevel(String text) {
		this.text = text;
		this.value=name();
	}

}
