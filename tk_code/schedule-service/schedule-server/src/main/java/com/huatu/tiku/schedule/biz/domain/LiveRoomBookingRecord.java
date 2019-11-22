package com.huatu.tiku.schedule.biz.domain;

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
 * 直播间预约记录
 * 
 * @author Geek-S
 *
 */
@Entity
@Getter
@Setter
public class LiveRoomBookingRecord extends BaseDomain {

	private static final long serialVersionUID = 5730966574014090038L;

	/**
	 * 直播间
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "liveRoomId", insertable = false, updatable = false)
	private LiveRoom liveRoom;

	/**
	 * 直播间ID
	 */
	private Long liveRoomId;

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
	 * 预约人
	 */
	private String username;

	/**
	 * 联系方式
	 */
	private String phone;

}
