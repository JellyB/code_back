package com.huatu.tiku.course.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseBreakPointPracticeDto {
	/**
	 * "questionId": 21959671, "submitTime": 1555493517679, "correct": 2,
	 * "courseWareId": 952263, "questionSource": 1, "step": -1, "listened": -1,
	 * "time": 1, "userId": 233982185, "subjectId": 1, "knowledgePoint": 757,
	 * "courseWareType": 1
	 */

	private Integer questionId;
	private Integer correct;
	private Integer courseWareId;
	private Integer time;
	
	private String answer;

	private Long userId;

}
