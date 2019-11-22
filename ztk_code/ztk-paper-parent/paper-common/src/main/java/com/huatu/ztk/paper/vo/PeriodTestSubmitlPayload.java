package com.huatu.ztk.paper.vo;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 阶段测试用户交卷信息上报到php所需数据
 * 
 * @author zhangchong
 *
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PeriodTestSubmitlPayload implements Serializable {

	private static final long serialVersionUID = 6970289483098746920L;

	/**
	 * 试卷id
	 * 
	 */
	private Integer papeId;

	/**
	 * 大纲id
	 */
	private Long syllabusId;

	/**
	 * 用户id
	 */
	private Long userId;

	/**
	 * 用户名
	 */
	private String userName;


	/**
	 * 是否完成0否1是
	 */
	private Integer isFinish;

}
