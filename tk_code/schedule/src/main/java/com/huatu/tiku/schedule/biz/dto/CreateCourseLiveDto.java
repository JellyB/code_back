package com.huatu.tiku.schedule.biz.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 接收课程直播创建参数
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
@ToString
public class CreateCourseLiveDto implements Serializable {

	private static final long serialVersionUID = 5730966574014090038L;

	/**
	 * 所属课程
	 */
	@NotNull(message = "课程ID内容不能为空")
	private Long courseId;

	/**
	 * 日期
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@NotNull(message = "直播日期不能为空")
	private List<Date> dates;

	/**
	 * 课程直播时间
	 */
	@NotNull(message = "直播时间不能为空")
	private List<List<String>> times;

	//特殊标记 如果true 根据两个日期 创建日期区间直播数据
	private Boolean token=false;

}
