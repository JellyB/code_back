package com.huatu.tiku.schedule.biz.vo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 教师授课课时统计
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
@ToString
public class TeacherScheduleReportVo implements Serializable {

	private static final long serialVersionUID = 7277195785747115287L;

	/**
	 * 日期（前端展示表头&属性）
	 */
	private List<Map<String, Object>> headers;

	/**
	 * 前端展示数据
	 */
	private List<List<Object>> datas;

	/**
	 * 前端展示数据
	 * 
	 * @author Geek-S
	 *
	 */
	@Getter
	@Setter
	@ToString
	public static class TeacherScheduleReportDataVo implements Serializable {

		private static final long serialVersionUID = -6410487677480880142L;

		/**
		 * 上午
		 */
		private String morningTime;

		/**
		 * 下午
		 */
		private String afternoonTime;

		/**
		 * 晚上
		 */
		private String eveningTime;

	}

}
