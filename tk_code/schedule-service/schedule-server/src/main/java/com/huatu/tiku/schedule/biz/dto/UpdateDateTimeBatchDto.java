package com.huatu.tiku.schedule.biz.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 接收添加教师参数
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
@ToString
public class UpdateDateTimeBatchDto implements Serializable {

	private static final long serialVersionUID = -6264405184308415605L;

	/**
	 * liveID
	 */
	@NotEmpty(message = "id不能为空")
	private List<Long> courseLiveIds;

	@NotNull(message = "课程id不能为空")
	private Long courseId;

	@NotNull(message = "移动位置不能为空")
	private Integer index;
}
