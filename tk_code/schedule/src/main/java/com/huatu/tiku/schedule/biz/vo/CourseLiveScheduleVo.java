package com.huatu.tiku.schedule.biz.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 课表
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
@ToString
public class CourseLiveScheduleVo implements Serializable {

	private static final long serialVersionUID = -4085191186879887982L;

	/**
	 * ID
	 */
	private Long id;

	/**
	 * 日期
	 */
	private String date;

	/**
	 * 开始时间
	 */
	private String timeBegin;

	/**
	 * 结束时间
	 */
	private String timeEnd;

	/**
	 * 课程名称
	 */
	private String courseName;

	/**
	 * 直播内容
	 */
	private String courseLiveName;

	/**
	 * 科目
	 */
	private String subject;

	/**
	 * 教师姓名
	 */
	private List<TeacherInfo> teacherInfos;

//	/**
//	 * 直播间
//	 */
//	private String liveRoom;

	/**
	 * 场控
	 */
	private String controllerName;

	/**
	 * 助教
	 */
	private String assistantName;

	/**
	 * 主持人
	 */
	private String compereName;

	/**
	 * 学习师
	 */
	private String learningTeacherName;

	/**
	 * 考试类型
	 */
	private String examType;

	private String place;//上课地点

	private String categoryName;//课程类型

	/**
	 * 滚动排课ID
	 */
	private Long sourceId;

	/**
	 * 教师信息
	 * 
	 * @author Geek-S
	 *
	 */
	@Getter
	@Setter
	public static class TeacherInfo implements Serializable {

		private static final long serialVersionUID = 9071885376852849662L;

		/**
		 * 阶段
		 */
		private String phase;

		/**
		 * 科目
		 */
		private String subject;

		/**
		 * 模块
		 */
		private String model;

		/**
		 * 教师姓名
		 */
		private String name;
	}
}
