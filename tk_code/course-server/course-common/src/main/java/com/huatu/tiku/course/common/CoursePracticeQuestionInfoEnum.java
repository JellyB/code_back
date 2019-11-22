package com.huatu.tiku.course.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 随堂练练习记录
 * @author zhangchong
 *
 */
@AllArgsConstructor
@Getter
public enum CoursePracticeQuestionInfoEnum {

	INIT("init", 1), FINISH("finish", 2), FORCESTOP("forceStop", 3);
	private String name;
	private int status;

}
