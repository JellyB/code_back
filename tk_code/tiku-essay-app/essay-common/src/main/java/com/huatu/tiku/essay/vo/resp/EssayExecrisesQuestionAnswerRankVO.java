package com.huatu.tiku.essay.vo.resp;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课后作业试题答题卡简单vo
 * 
 * @author zhangchong
 *
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayExecrisesQuestionAnswerRankVO {

	private Integer userId;
	
	/**
	 * 试题答题卡id
	 */
	private Long id;
	
	private Long spendTime;
	
	private Date submitTime;

}
