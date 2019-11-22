package com.huatu.tiku.schedule.biz.dto;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 滚动排课参数
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
@ToString
public class RollingScheduleDto implements Serializable {

	private static final long serialVersionUID = 5730966574014090038L;

	/**
	 * 课程ID
	 */
	@NotNull(message = "课程ID不能为空")
	private Long courseId;

	/**
	 * 滚动排课IDs
	 */
	@NotNull(message = "课程直播ID不能为空")
	private List<Long> courseLiveIds;

}
