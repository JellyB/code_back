package com.huatu.tiku.schedule.biz.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

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

	/**
	 * 课程直播教师ID
	 */
	@NotNull(message = "课程ID不能为空")
	private Long courseId;

	/**
	 * 教师ID
	 */
	@NotEmpty(message = "日期不能为空")
	private List<Date> dates;
}
