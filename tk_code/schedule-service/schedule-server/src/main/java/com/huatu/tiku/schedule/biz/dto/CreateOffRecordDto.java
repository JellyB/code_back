package com.huatu.tiku.schedule.biz.dto;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.huatu.tiku.schedule.base.domain.BaseDomain;

import lombok.Getter;
import lombok.Setter;

/**
 * 请假记录
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
public class CreateOffRecordDto extends BaseDomain implements Serializable {

	private static final long serialVersionUID = 3652134760600352714L;

	/**
	 * 教师ID
	 */
	@NotNull(message = "教师ID不能为空")
	private Long teacherId;

	/**
	 * 开始时间
	 */
	@NotNull(message = "开始时间不能为空")
	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	private Date timeBegin;

	/**
	 * 结束时间
	 */
	@NotNull(message = "结束时间不能为空")
	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	private Date timeEnd;
}
