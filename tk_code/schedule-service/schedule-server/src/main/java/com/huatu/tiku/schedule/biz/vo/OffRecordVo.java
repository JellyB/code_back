package com.huatu.tiku.schedule.biz.vo;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 请假记录
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
@ToString
public class OffRecordVo implements Serializable {

	private static final long serialVersionUID = -4085191186879887982L;

	/**
	 * ID
	 */
	private Long id;

	/**
	 * 教师
	 */
	private Long teacherId;

	/**
	 * 开始时间
	 */
	private String timeBegin;

	/**
	 * 结束时间
	 */
	private String timeEnd;

}
