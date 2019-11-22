package com.huatu.tiku.schedule.biz.vo;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * 直播间预约记录
 * 
 * @author geek-s
 *
 */
@Getter
@Setter
public class LiveRoomBookingRecordVo implements Serializable {

	private static final long serialVersionUID = 5730966574014090038L;

	/**
	 * 日期
	 */
	private String date;

	/**
	 * 星期
	 */
	private String dayOfWeek;

	/**
	 * 开始时间
	 */
	private String timeBegin;

	/**
	 * 结束时间
	 */
	private String timeEnd;

	/**
	 * 预约人
	 */
	private String username;

	/**
	 * 联系方式
	 */
	private String phone;

}
