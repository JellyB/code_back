package com.huatu.tiku.schedule.biz.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.huatu.tiku.schedule.base.domain.BaseDomain;

import lombok.Getter;
import lombok.Setter;

/**
 * 教师授课课时统计
 * 
 * @author Geek-S
 *
 */
@Entity
@Getter
@Setter
public class TeacherScheduleReport extends BaseDomain implements Serializable {

	private static final long serialVersionUID = 3652134760600352714L;

	/**
	 * 日期
	 */
	@Temporal(TemporalType.DATE)
	private Date date;

	/**
	 * 年
	 */
	private Integer year;

	/**
	 * 月
	 */
	private Integer month;

	/**
	 * 日
	 */
	private Integer day;

	/**
	 * 教师
	 */
	private Long teacherId;

	/**
	 * 上午
	 */
	private Integer morningTime;

	/**
	 * 下午
	 */
	private Integer afternoonTime;

	/**
	 * 晚上
	 */
	private Integer eveningTime;

}
