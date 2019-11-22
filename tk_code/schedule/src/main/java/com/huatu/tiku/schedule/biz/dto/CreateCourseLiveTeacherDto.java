package com.huatu.tiku.schedule.biz.dto;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 接收课程直播教师创建参数
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
@ToString
public class CreateCourseLiveTeacherDto implements Serializable {

	private static final long serialVersionUID = 5730966574014090038L;

	/**
	 * 所属课程直播
	 */
	@NotNull(message = "课程直播ID内容不能为空")
	private Long courseLiveId;

}
