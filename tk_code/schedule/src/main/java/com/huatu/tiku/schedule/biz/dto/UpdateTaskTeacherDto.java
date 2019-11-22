package com.huatu.tiku.schedule.biz.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.huatu.tiku.schedule.biz.enums.CourseConfirmStatus;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;
import java.util.List;

/**
 * 教师确认任务dto
 *
 * @author wangjian
 **/
@Getter
@Setter
@ToString
public class UpdateTaskTeacherDto implements Serializable {

	private static final long serialVersionUID = 13498690017049797L;

	/**
	 * IDs
	 */
	@NotEmpty(message = "liveIds不能为空")
	private List<Long> liveIds;

	/**
	 * 课程状态
	 */
	//@NotNull(message = "课程确认状态不能为空")
	private CourseConfirmStatus courseConfirmStatus;

}
