package com.huatu.tiku.schedule.biz.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

import com.huatu.tiku.schedule.biz.enums.CoursePhase;
import com.huatu.tiku.schedule.biz.enums.TeacherCourseLevel;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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

	/**
	 * 课程直播阶段
	 */
	private CoursePhase coursePhase;

	/**
	 * 科目ID
	 */
	private Long subjectId;

	/**
	 * 模块ID
	 */
	private Long moduleIdl;

	/**
	 * 教师授课级别
	 */
	private TeacherCourseLevel teacherCourseLevel;

	/**
	 * 日期
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@NotNull(message = "直播日期不能为空")
	private List<Date> dates;

	/**
	 * 课程直播时间
	 */
	@NotNull(message = "直播时间不能为空")
	private List<String> times;

}
