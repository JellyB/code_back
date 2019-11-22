package com.huatu.tiku.schedule.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;

/**
 * 老师状态
 * 
 * @author Geek-S
 *
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TeacherStatus {

	DSH("待审核"), ZC("正常"), JY("禁用");

	/**
	 * 值
	 */
	private String value;
	/**
	 * 显示字体
	 */
	private String text;

	private TeacherStatus(String text) {
		this.value=name();
		this.text = text;
	}

}