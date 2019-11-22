package com.huatu.tiku.schedule.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**
 * 课程分类
 * 
 * @author Geek-S
 *
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SchoolType {

	HTZX("华图在线"),HTJY("华图教育"),ZJFX("浙江分校");
	/**
	 * 值
	 */
	private String value;
	/**
	 * 显示字体
	 */
	private String text;

	private SchoolType(String text) {
		this.value=name();
		this.text = text;
	}

}
