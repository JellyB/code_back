package com.huatu.tiku.schedule.biz.dto;

import java.io.Serializable;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import com.huatu.tiku.schedule.biz.enums.TeacherCourseLevel;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * 接收批量添加课程直播教师参数
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
@ToString
public class SaveCourseLiveTeacherBatchDto implements Serializable {

	private static final long serialVersionUID = 3289196376042064813L;

	/**
	 * 课程ID
	 */
	@NotNull(message = "课程ID不能为空")
	private Long courseId;

	@NotEmpty(message = "ids不能为空")
	private List<Long> courseLiveIds;

	/**
	 * 科目ID
	 */
	private Long subjectId;

	/**
	 * 教师授课级别
	 */
	private TeacherCourseLevel teacherCourseLevel;

}
