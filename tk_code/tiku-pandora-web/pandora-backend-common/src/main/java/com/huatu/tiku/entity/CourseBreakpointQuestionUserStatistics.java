package com.huatu.tiku.entity;

import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.huatu.common.bean.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 随堂练用户答题统计
 * 
 * @author zhangchong
 *
 */
@Data
@NoArgsConstructor
@Table(name = "course_breakpoint_question_user_statistics")
@Builder
@AllArgsConstructor
public class CourseBreakpointQuestionUserStatistics extends BaseEntity{

	/**
	 * 试题ID
	 */
	@NotNull(message = "试题ID不能为空")
	private Long questionId;

	/**
	 * 用户ID
	 */
	private Long userId;

	/**
	 * 课件id
	 */
	private Long courseId;
	
	/**
	 * 用户答案
	 */
	private int correct;

}
