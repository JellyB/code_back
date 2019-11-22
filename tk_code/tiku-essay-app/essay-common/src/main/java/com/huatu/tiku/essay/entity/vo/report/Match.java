package com.huatu.tiku.essay.entity.vo.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by linkang on 17-7-14.
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class Match implements Serializable {

	// private Integer paperId; //试卷id
	private String name; // 大赛名称

	private String timeInfo; // 时间信息
	private String courseInfo; // 课程信息

	private Integer courseId; // 课程id
	private String instruction; // 考试说明
	private String instructionPC; // 考试说明
	private Integer tag;// 标签 用来区别2018国考/省考
	private Integer subject;// 考试科目

	// private Long startTime; //考试开始时间
//    private Long endTime; //结束时间
	private Integer status; // 模考大赛状态
	private long essayPaperId; // 申论模考大赛试卷id
	private Long essayStartTime; // 申论模考大赛开始时间
	private Long essayEndTime; // 申论模考大赛结束时间
	private Integer flag; // (0表示没有成绩报告1表示只有行测报告2只有申论报告3行测申论报告都有)
	private Integer stage; // 1表示行测模考大赛阶段2表示申论模考大赛阶段
	private Integer enrollCount; // 总报名人数
	private Integer enrollFlag; // 报名方式 0选择地区报名1无地区报名
	private MatchUserMeta userMeta; // 用户信息

	private Long liveDate;
	private Integer price;

	/**
	 * add by zhaoxi (v7.1.12新需求，添加答题交卷时间限制)
	 */

	/**
	 * 可提前交卷时间（单位：分钟）
	 */
	private Integer commitLimitTime;
	/**
	 * 可提前进入查看试卷时间（单位：分钟）
	 */
	private Integer enterLimitTime;

}
