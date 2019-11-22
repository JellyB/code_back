package com.huatu.tiku.schedule.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.huatu.tiku.schedule.entity.enums.ScheduleExamType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 老师授课
 * 
 * @author Geek-S
 *
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_schedule_teacher_subject")
@EqualsAndHashCode(callSuper = false)
public class ScheduleTeacherSubject extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 3652134760600352714L;

	/**
	 * 所属考试类型
	 */
	private ScheduleExamType examType;

	/**
	 * 科目
	 */
	private Long subjectId;

	/**
	 * 老师等级
	 */
	private Long teacherLevelId;

	/**
	 * 老师ID
	 */
	private Long teacherId;
}
