package com.huatu.tiku.schedule.biz.vo;

import java.io.Serializable;
import java.util.List;

import com.huatu.tiku.schedule.biz.enums.TeacherType;
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

	/**
	 * 讲师名字
	 */
	private String teacherNames;
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
	 * 摄影师
	 */
	private String sysName;

	/**
	 * 质控师
	 */
	private String zksName;

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

	private Long videoRoomId;

	private String videoRoomName;

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
		 * 科目
		 */
		private String subject;

		/**
		 * 教师姓名
		 */
		private String name;
		/**
		 * 		教师id
		 */
		private Long teacherId;

		/**
		 * 教师类型
		 */
		private TeacherType type;
	}
}
