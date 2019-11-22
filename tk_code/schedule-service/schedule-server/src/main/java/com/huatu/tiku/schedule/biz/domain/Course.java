package com.huatu.tiku.schedule.biz.domain;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import com.huatu.tiku.schedule.base.domain.BaseDomain;
import com.huatu.tiku.schedule.biz.enums.CourseCategory;
import com.huatu.tiku.schedule.biz.enums.CourseStatus;
import com.huatu.tiku.schedule.biz.enums.ExamType;

import com.huatu.tiku.schedule.biz.enums.SchoolType;
import lombok.Getter;
import lombok.Setter;

/**
 * 课程
 * 
 * @author Geek-S
 *
 */
@Entity
@Getter
@Setter
public class Course extends BaseDomain {

	private static final long serialVersionUID = 5730966574014090038L;

	/**
	 * 课程名称
	 */
	private String name;

	/**
	 * 课程类型（直播）
	 */
	private CourseCategory courseCategory;

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
	 * 是否需要助教
	 */
	private Boolean assistantFlag;

	/**
	 * 是否需要场控
	 */
	private Boolean controllerFlag;

	/**
	 * 是否需要主持人
	 */
	private Boolean compereFlag;

	/**
	 * 是否周六上课
	 */
	private Boolean satFlag;

	/**
	 * 是否周日上课
	 */
	private Boolean sunFlag;

	/**
	 * 是否隔天
	 */
	private Boolean separatorFlag;

	/**
	 * 开始日期
	 */
	@Temporal(TemporalType.DATE)
	private Date dateBegin;

	/**
	 * 结束日期
	 */
	@Temporal(TemporalType.DATE)
	private Date dateEnd;

	/**
	 * 课程直播
	 */
	@OneToMany(mappedBy = "course",cascade = CascadeType.REMOVE)
	private List<CourseLive> courseLives;

	/**
	 * 课程状态
	 * 初始为排课状态
	 */
	private CourseStatus status;

	@Column(name = "interview_teacher")
	@ElementCollection
	@JoinTable(joinColumns = @JoinColumn(name = "course_id"))
	private Set<ExamType> interviewTeacher;

	/**
	 * 上课地点
	 */
	private String place;

	private SchoolType schoolType;

}
