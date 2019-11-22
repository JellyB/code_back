package com.huatu.tiku.schedule.biz.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.huatu.tiku.schedule.base.domain.BaseDomain;

import lombok.Getter;
import lombok.Setter;

/**
 * 请假记录
 * 
 * @author Geek-S
 *
 */
@Entity
@Getter
@Setter
public class OffRecord extends BaseDomain implements Serializable {

	private static final long serialVersionUID = 3652134760600352714L;

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

	/**
	 * 开始日期
	 */
	@Temporal(TemporalType.DATE)
	private Date date;

	/**
	 * 结束时间
	 */
	private Integer timeBegin;

	/**
	 * 结束时间
	 */
	private Integer timeEnd;

}
