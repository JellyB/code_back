package com.huatu.tiku.schedule.biz.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

import com.huatu.tiku.schedule.biz.enums.CourseCategory;
import com.huatu.tiku.schedule.biz.enums.ExamType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 接收课程创建参数
 * 
 * @author Geek-S
 *
 */
@Getter
@Setter
@ToString
public class CreateCourseDto implements Serializable {

	private static final long serialVersionUID = 5730966574014090038L;

	/**
	 * 课程名称
	 */
	@NotEmpty(message = "课程名称不能为空")
	private String name;

	/**
	 * 课程日期开始
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@NotNull(message = "课程开始日期不能为空")
	private Date dateBegin;

	/**
	 * 课程日期开始
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@NotNull(message = "课程结束日期不能为空")
	private Date dateEnd;


	/**
	 * 是否隔天
	 */
	private Boolean separatorFlag = false;

	public void setSeparatorFlag(Boolean separatorFlag) {
		if (separatorFlag != null) {
			this.separatorFlag = separatorFlag;
		}
	}

	/**
	 * 是否周六上课
	 */
	private Boolean satFlag = false;

	public void setSatFlag(Boolean satFlag) {
		if (satFlag != null) {
			this.satFlag = satFlag;
		}
	}

	/**
	 * 是否周日上课
	 */
	private Boolean sunFlag = false;

	public void setSunFlag(Boolean sunFlag) {
		if (sunFlag != null) {
			this.sunFlag = sunFlag;
		}
	}

	/**
	 * 课程类型（直播）
	 */
	@NotNull(message = "课程类型不能为空")
	private CourseCategory courseCategory;

	/**
	 * 考试类型
	 */
	@NotNull(message = "考试类型不能为空")
	private ExamType examType;

	/**
	 * 科目
	 */
	private Long subjectId;

	/**
	 * 是否需要学习师
	 */
	private Boolean learningTeacherFlag = false;

	public void setLearningTeacherFlag(Boolean learningTeacherFlag) {
		if (learningTeacherFlag != null) {
			this.learningTeacherFlag = learningTeacherFlag;
		}
	}

	/**
	 * 是否需要助教
	 */
	private Boolean assistantFlag = false;

	public void setAssistantFlag(Boolean assistantFlag) {
		if (assistantFlag != null) {
			this.assistantFlag = assistantFlag;
		}
	}

	/**
	 * 是否需要场控
	 */
	private Boolean controllerFlag = false;

	public void setControllerFlag(Boolean controllerFlag) {
		if (controllerFlag != null) {
			this.controllerFlag = controllerFlag;
		}
	}

	/**
	 * 是否需要主持人
	 */
	private Boolean compereFlag = false;

	public void setCompereFlag(Boolean compereFlag) {
		if (compereFlag != null) {
			this.compereFlag = compereFlag;
		}
	}

	/**
	 * 面试推荐教师列表
	 */
	private List<Long> teacherIds;

	/**
	 * 上课地点
	 */
	private String place;
}
