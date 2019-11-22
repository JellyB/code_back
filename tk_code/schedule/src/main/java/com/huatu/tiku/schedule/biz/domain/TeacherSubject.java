package com.huatu.tiku.schedule.biz.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.huatu.tiku.schedule.base.domain.BaseDomain;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.enums.TeacherCourseLevel;

import lombok.Getter;
import lombok.Setter;

/**
 * 教师授课
 * 
 * @author Geek-S
 *
 */
@Entity
@Getter
@Setter
public class TeacherSubject extends BaseDomain implements Serializable {

	private static final long serialVersionUID = 3652134760600352714L;

	/**
	 * 考试类型
	 */
	private ExamType examType;

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
	 * 模块
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "moduleId", insertable = false, updatable = false)
	private Module module;

	/**
	 * 模块id
	 */
	private Long moduleId;

	/**
	 * 教师等级
	 */
	private TeacherCourseLevel teacherCourseLevel;

	/**
	 * 教师
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "teacherId", insertable = false, updatable = false)
	private Teacher teacher;

	/**
	 * 教师ID
	 */
	private Long teacherId;
}
