package com.huatu.tiku.schedule.biz.domain;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.huatu.tiku.schedule.base.domain.BaseDomain;
import com.huatu.tiku.schedule.biz.enums.*;

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

	/**
	 * 教师类型  用于区分教师的类型 是讲师或者助教场控等
	 */
	private TeacherType teacherType;

	/**
	 * 教师 上次教师id 用于给修改教师的课程发送通知短信
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lastTeacherId", insertable = false, updatable = false)
	private Teacher lastTeacher;

	private  Long lastTeacherId;

	/**
	 * 滚动排课关联
	 */
	private Long sourceId;
}
