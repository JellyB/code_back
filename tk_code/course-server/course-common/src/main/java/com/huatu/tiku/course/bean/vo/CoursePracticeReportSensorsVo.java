package com.huatu.tiku.course.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 直播随堂练上报神策vo
 * 
 * @author zhangchong
 *
 */
@NoArgsConstructor
@Setter
@Getter
@Builder
@AllArgsConstructor
@ToString
public class CoursePracticeReportSensorsVo {
	private Long roomId;
	private Long coursewareId;
	private Long questionId;
	//做对题目数
	private int rcount;
	//已经做题目数
	private int docount;
	//总题数
	private int qcount;
	//总用时
	private int times;
	private boolean isFinish;
	
	private Integer userId;

}
