package com.huatu.tiku.schedule.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;

/**
 * 课程确认状态
 * 
 * @author Geek-S
 *
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum CourseConfirmStatus {

	DQR("待确认"), QR("确认"), DGT("待沟通"),CPDQR("重排待确认");

	/**
	 * 值
	 */
	private String value;

	/**
	 * 显示字体
	 */
	private String text;

	private CourseConfirmStatus(String text) {
		this.value = name();
		this.text = text;
	}

}
