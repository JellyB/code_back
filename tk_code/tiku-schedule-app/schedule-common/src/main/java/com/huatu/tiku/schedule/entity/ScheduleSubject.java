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
 * 科目，考试类型作为一级科目
 * 
 * @author Geek-S
 *
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_schedule_subject")
@EqualsAndHashCode(callSuper = false)
public class ScheduleSubject extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 3652134760600352714L;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 状态
	 */
	private Boolean status;

	/**
	 * 所属考试类型
	 */
	private ScheduleExamType examType;

	public String getExamTypeText() {
		return examType.getText();
	}

	/**
	 * 录入人ID
	 */
	private Long teacherId;

	/**
	 * 排序
	 */
	private Integer sort;
}
