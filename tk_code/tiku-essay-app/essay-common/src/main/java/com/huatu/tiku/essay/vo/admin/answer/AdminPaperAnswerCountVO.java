package com.huatu.tiku.essay.vo.admin.answer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 试卷统计vo
 * 
 * @author zhangchong
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPaperAnswerCountVO {

	/**
	 * 批改类型
	 */
	private int correctMode;

	/**
	 * 批改次数
	 */
	private long count;
}
