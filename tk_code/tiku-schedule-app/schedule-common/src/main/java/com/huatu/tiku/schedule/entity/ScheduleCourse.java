package com.huatu.tiku.schedule.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 课程
 *
 *
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_schedule_course")
@EqualsAndHashCode(callSuper = false)
public class ScheduleCourse extends BaseEntity implements Serializable {
	private static final long serialVersionUID = -5508660158399479937L;

	/**
	 * 直播id
	 */
	private Long liveId;

	/**
	 * 科目id
	 */
	private Long subjectId;

	/**
	 * 讲师id
	 */
	private Long teacherId;

	/**
	 * 助教id
	 */
	private Long assistantId;

	/**
	 * 课程时间
	 */
	private Date date;

}
