package com.huatu.tiku.schedule.entity.enums;

import lombok.Getter;

/**
 * 考试类型
 * 
 * @author Geek-S
 *
 */
@Getter
public enum ScheduleExamType {

	GWY("公务员"), SYDW("事业单位"), GAZJ("公安招警");

	private String text;

	private ScheduleExamType(String text) {
		this.text = text;
	}

}
