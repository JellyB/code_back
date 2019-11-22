package com.huatu.tiku.schedule.biz.dto;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 接收添加教师参数
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
@ToString
public class BindTeacherDto implements Serializable {

	private static final long serialVersionUID = 5730966574014090038L;

	/**
	 * 课程直播教师ID
	 */
	@NotNull(message = "课程直播ID不能为空")
	private Long courseLiveTeacherId;

	/**
	 * 教师ID
	 */
	private Long teacherId;
}
