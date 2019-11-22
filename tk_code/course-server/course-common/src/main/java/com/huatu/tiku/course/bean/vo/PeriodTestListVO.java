package com.huatu.tiku.course.bean.vo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 阶段测试列表vo
 * 
 * @author zhangchong
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodTestListVO {
	private int currentPage;
	private int next;
	private List<CourseInfo> list;

	@AllArgsConstructor
	@Setter
	@Getter
	@Builder
	@NoArgsConstructor
	public static class CourseInfo {
		private int classId;
		private String className;
		private int undoCount;
		/**
		 * 阶段测试列表
		 */
		private List<PeriodTestInfo> child;
	}

	@AllArgsConstructor
	@Setter
	@Getter
	@Builder
	@NoArgsConstructor
	@ToString
	public static class PeriodTestInfo {
		private String examName;
		private long examId;
		private String endTime;
		private String startTime;
		private int isAlert;
		private String questionIds;
		private long syllabusId;
		private int coursewareNum;//排序
		private int status;//阶段测试状态
		private String showTime;//app端展示时间
		private Integer isEffective;//是否有活动时间 1有效0无效
		private Boolean isExpired;//是否过期 1过期0未过期
		private Integer alreadyRead;// 0未读1已读
	}
}
