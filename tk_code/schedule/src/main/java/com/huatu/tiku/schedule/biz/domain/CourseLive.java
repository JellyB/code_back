package com.huatu.tiku.schedule.biz.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.huatu.tiku.schedule.base.domain.BaseDomain;
import com.huatu.tiku.schedule.biz.enums.CourseConfirmStatus;

import com.huatu.tiku.schedule.biz.enums.CourseLiveCategory;
import lombok.Getter;
import lombok.Setter;

/**
 * 课程直播
 * 
 * @author Geek-S
 *
 */
@Entity
@Getter
@Setter
public class CourseLive extends BaseDomain {

	private static final long serialVersionUID = 5730966574014090038L;

	/**
	 * 所属课程
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "courseId", insertable = false, updatable = false)
	private Course course;

	/**
	 * 课程ID
	 */
	private Long courseId;

	/**
	 * 课程内容
	 */
	private String name;

	/**
	 * 日期
	 */
	private Integer dateInt;

	/**
	 * 日期
	 */
	@Temporal(TemporalType.DATE)
	private Date date;

	/**
	 * 开始时间
	 */
	private Integer timeBegin;

	/**
	 * 结束时间
	 */
	private Integer timeEnd;

	/**
	 * 直播间
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "liveRoomId",insertable = false, updatable = false)
	private LiveRoom liveRoom;

	/**
	 * 直播间ID
	 */
	private  Long liveRoomId;

	/**
	 * 学习师
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "learningTeacherId", insertable = false, updatable = false)
	private Teacher learningTeacher;

	/**
	 * 学习师ID
	 */
	private Long learningTeacherId;

	/**
	 * 学习师确认标记
	 */
	private CourseConfirmStatus ltConfirm;

	/**
	 * 助教
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assistantId", insertable = false, updatable = false)
	private Teacher assistant;

	/**
	 * 助教ID
	 */
	private Long assistantId;

	/**
	 * 助教确认标记
	 */
	private CourseConfirmStatus assConfirm;

	/**
	 * 场控
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "controllerId", insertable = false, updatable = false)
	private Teacher controller;

	/**
	 * 场控ID
	 */
	private Long controllerId;

	/**
	 * 场控确认标记
	 */
	private CourseConfirmStatus ctrlConfirm;

	/**
	 * 主持人
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "compereId", insertable = false, updatable = false)
	private Teacher compere;

	/**
	 * 主持人ID
	 */
	private Long compereId;

	/**
	 * 主持人确认标记
	 */
	private CourseConfirmStatus comConfirm;

	/**
	 * 关联课程直播教师
	 */
	@OneToMany(mappedBy = "courseLive", cascade = CascadeType.ALL)
	private List<CourseLiveTeacher> courseLiveTeachers;

	/**
	 * 滚动排课关联
	 */
	private Long sourceId;

	/**
	 * 授课类型
	 */
	private CourseLiveCategory courseLiveCategory;
}
