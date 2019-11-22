package com.huatu.tiku.essay.essayEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 
 * @author zhangchong
 * 文章类别枚举
 */
@AllArgsConstructor
@Getter
public enum ArticleLevelEnum {

	ONE(1, "一类文"), TWO(2, "二类文"), THREE(3, "三类文"), FOUR(4, "四类文"), FIVE(5,"五类文");

	private int type;
	private String name;

}
