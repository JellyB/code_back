package com.huatu.tiku.schedule.biz.dto;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

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
	 * 开始日期
	 */
	@NotNull(message = "请假日期不能为空")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date date;

	/**
	 * 开始时间
	 */
	@NotNull(message = "开始时间不能为空")
	private Integer timeBegin;

	public void setTimeBegin(String timeBegin) {
		this.timeBegin = Integer.parseInt(timeBegin.replace(":", ""));
	}

	/**
	 * 结束时间
	 */
	@NotNull(message = "结束时间不能为空")
	private Integer timeEnd;

	public void setTimeEnd(String timeEnd) {
		this.timeEnd = Integer.parseInt(timeEnd.replace(":", ""));
	}
}
