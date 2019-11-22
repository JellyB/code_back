package com.huatu.tiku.schedule.biz.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 接收添加教师参数
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
@ToString
public class UpdateDateTimeDto implements Serializable {

	private static final long serialVersionUID = -6264405184308415605L;

	/**
	 * liveID
	 */
	@NotNull(message = "id不能为空")
	private Long courseLiveId;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@NotNull(message = "直播日期不能为空")
	private Date date;

	@NotNull(message = "直播时间不能为空")
	private Integer timeBegin;

	@NotNull(message = "直播时间不能为空")
	private Integer timeEnd;
}
