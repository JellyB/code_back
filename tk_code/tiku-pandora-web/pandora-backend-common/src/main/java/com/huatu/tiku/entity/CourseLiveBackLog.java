package com.huatu.tiku.entity;

import javax.persistence.Table;

import com.huatu.common.bean.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 直播转回放对应关系
 * 
 * @author zhangchong
 *
 */
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "course_live_back_log")
public class CourseLiveBackLog extends BaseEntity {
	/**
	 * 百家云id
	 */
	private Long roomId;

	/**
	 * 大纲id
	 */
	private Long liveCoursewareId;

	/**
	 * 课程id
	 */
	private Long liveBackCoursewareId;

}