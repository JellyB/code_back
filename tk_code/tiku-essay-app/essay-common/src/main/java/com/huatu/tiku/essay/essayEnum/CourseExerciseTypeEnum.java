package com.huatu.tiku.essay.essayEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 批改订单练习类型
 * 
 * @author zhangchong
 *
 */
@Getter
@AllArgsConstructor
public enum CourseExerciseTypeEnum {

	normal(1, "普通类型"), exercises(2, "课后练习");

	private int code;
	private String value;

}
