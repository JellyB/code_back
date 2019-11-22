package com.huatu.tiku.schedule.entity.enums;

import lombok.Getter;

/**
 * 老师状态
 * 
 * @author Geek-S
 *
 */
@Getter
public enum ScheduleTeacherStatus {

	DSH("待审核"), SH("审核"), XX("下线"), GB("关闭");

	private String text;

	private ScheduleTeacherStatus(String text) {
		this.text = text;
	}

}