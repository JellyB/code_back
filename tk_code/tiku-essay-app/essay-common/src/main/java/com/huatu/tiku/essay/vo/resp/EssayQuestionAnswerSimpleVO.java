package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 试题答题卡简单vo
 * 
 * @author zhangchong
 *
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayQuestionAnswerSimpleVO {

	/* 题目base表id */
	private Long questionBaseId;
	
	/**
	 * 试题答题卡id
	 */
	private Long id;

}
