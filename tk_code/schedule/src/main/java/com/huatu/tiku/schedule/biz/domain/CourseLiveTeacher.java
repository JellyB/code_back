package com.huatu.tiku.schedule.biz.domain;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.huatu.tiku.schedule.base.domain.BaseDomain;
import com.huatu.tiku.schedule.biz.enums.CourseStatus;
import com.huatu.tiku.schedule.biz.enums.CourseConfirmStatus;
import com.huatu.tiku.schedule.biz.enums.CoursePhase;
import com.huatu.tiku.schedule.biz.enums.TeacherCourseLevel;

import lombok.Getter;
import lombok.Setter;

/**
 * 课程直播教师
 * 
 * @author Geek-S
 *
 */
@Entity
@Getter
@Setter
public class CourseLiveTeacher extends BaseDomain {

	private static final long serialVersionUID = 5730966574014090038L;

	/**
	 * 所属直播
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "courseLiveId", insertable = false, updatable = false)
	private CourseLive courseLive;

	/**
	 * 课程直播ID
	 */
	private Long courseLiveId;

	/**
	 * 课程阶段
	 */
	private CoursePhase coursePhase;

	/**
	 * 科目
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subjectId", insertable = false, updatable = false)
	private Subject subject;

	/**
	 * 科目id
	 */
	private Long subjectId;

	/**
	 * 所属模块
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "moduleId", insertable = false, updatable = false)
	private Module module;

	/**
	 * 模块ID
	 */
	private Long moduleId;

	/**
	 * 教师授课等级
	 */
	private TeacherCourseLevel teacherCourseLevel;

	/**
	 * 教师
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "teacherId", insertable = false, updatable = false)
	private Teacher teacher;

	private  Long teacherId;

	/**
	 *是否确认
	 */
	private CourseConfirmStatus confirm;

}
