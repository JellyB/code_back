package com.huatu.tiku.schedule.biz.dto;

import java.io.Serializable;
import java.util.List;


import org.hibernate.validator.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 一键排课参数
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
@ToString
public class OneKeyScheduleDto implements Serializable {

	private static final long serialVersionUID = 5730966574014090038L;

	@NotEmpty(message = "id不能为空")
	private List<Long> courseLiveIds;
}
