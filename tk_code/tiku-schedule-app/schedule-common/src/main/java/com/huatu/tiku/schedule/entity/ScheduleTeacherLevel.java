package com.huatu.tiku.schedule.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 老师等级
 * 
 * @author Geek-S
 *
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_schedule_teacher_level")
@EqualsAndHashCode(callSuper = false)
public class ScheduleTeacherLevel extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 3652134760600352714L;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 排序
	 */
	private Integer sort;
}
