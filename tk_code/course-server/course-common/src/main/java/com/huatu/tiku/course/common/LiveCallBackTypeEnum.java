package com.huatu.tiku.course.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 直播上下课类型
 * 
 * @author zhangchong
 *
 */
@AllArgsConstructor
@Getter
public enum LiveCallBackTypeEnum {

	START("start", "上课", 1), END("end", "下课", 2);

	private String key;
	private String name;
	private int order;

	public static LiveCallBackTypeEnum create(String key) {
		for (LiveCallBackTypeEnum studyTypeEnum : values()) {
			if (studyTypeEnum.getKey().equals(key)) {
				return studyTypeEnum;
			}
		}
		return null;
	}
}
