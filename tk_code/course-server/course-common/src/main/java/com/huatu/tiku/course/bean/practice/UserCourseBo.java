package com.huatu.tiku.course.bean.practice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author zhangchong
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCourseBo {

	private Integer userId;

	private Long courseId;
}
