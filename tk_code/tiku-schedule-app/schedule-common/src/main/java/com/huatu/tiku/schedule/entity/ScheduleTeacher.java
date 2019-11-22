package com.huatu.tiku.schedule.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.huatu.tiku.schedule.entity.enums.ScheduleExamType;
import com.huatu.tiku.schedule.entity.enums.ScheduleTeacherStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 老师
 * 
 * @author Geek-S
 *
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_schedule_teacher")
@EqualsAndHashCode(callSuper = false)
public class ScheduleTeacher extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 3652134760600352714L;

	/**
	 * 姓名
	 */
	private String name;

	/**
	 * 所属考试类型
	 */
	private ScheduleExamType examType;

	/**
	 * 科目
	 */
	private Long subjectId;

	/**
	 * 类型 0 组长 1 组员
	 */
	private Integer type;

	/**
	 * 手机
	 */
	private String phone;

	/**
	 * 微信
	 */
	private String wechat;

	/**
	 * 密码
	 */
	private String password;

	/**
	 * 状态
	 */
	private ScheduleTeacherStatus status;

	/**
	 * 录入人ID
	 */
	private Long teacherId;

	/**
	 * 授课
	 */
	@Transient
	List<ScheduleTeacherSubject> teacherSubjects;
}
