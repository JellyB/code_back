package com.huatu.tiku.schedule.biz.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 批量删除直播
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
@ToString
public class DeleteCourseLiveBatchDto implements Serializable {

	private static final long serialVersionUID = 5730966574014090038L;

	/**
	 * 课程ID
	 */
	@NotNull(message = "课程ID不能为空")
	private Long courseId;

	/**
	 * 课程日期
	 */
	@NotNull(message = "课程日期不能为空")
	private List<Date> dates;

	/**
	 * 课程时间
	 */
	private List<List<String>> times;
}
