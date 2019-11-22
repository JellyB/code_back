package com.huatu.ztk.paper.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 阶段测试首页
 * 
 * @author zhangchong
 *
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PeriodTestDetailVo {

	/**
	 * 试卷名称
	 */
	private String name;

	/**
	 * 完成人数
	 */
	private Integer submitCount;

	/**
	 * 阶段测试说明
	 */
	private String description;
	/**
	 * 上线时间,毫秒
	 */
	private Long onlineTime;

	/**
	 * 下线时间
	 */
	private Long offlineTime;

	/**
	 * 限定时间
	 */
	private Integer time;
	/**
	 * 试卷id
	 */
	private Integer paperId;
	/**
	 * 知识点
	 */
	private String pointsName;
	/**
	 * 试题数量
	 */
	private Integer qcount;
	/**
	 * 2 开始考试5继续考试6查看报告
	 */
	private Integer status;

	/**
	 * 答题卡id
	 */
	private Long practiceId;

	/**
	 * 时间是否有效 0无效1有效
	 */
	private Integer startTimeIsEffective;

	/**
	 * 答题卡id
	 */
	private String practiceIdStr;


}
