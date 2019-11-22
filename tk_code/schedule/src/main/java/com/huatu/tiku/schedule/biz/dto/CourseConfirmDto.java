package com.huatu.tiku.schedule.biz.dto;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.huatu.tiku.schedule.biz.enums.CourseConfirmStatus;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 课程确认参数
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
@ToString
public class CourseConfirmDto implements Serializable {

	private static final long serialVersionUID = 5730966574014090038L;

	/**
	 * 课程直播确认Token
	 */
	@NotEmpty(message = "Token不能为空")
	private String token;

	/**
	 * 课程确认状态
	 */
	@NotNull(message = "课程确认状态不能为空")
	private CourseConfirmStatus courseConfirmStatus;
}
