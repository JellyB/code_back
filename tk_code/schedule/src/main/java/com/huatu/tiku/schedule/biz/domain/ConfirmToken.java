package com.huatu.tiku.schedule.biz.domain;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.huatu.tiku.schedule.base.domain.BaseDomain;
import com.huatu.tiku.schedule.biz.enums.TeacherType;

import lombok.Getter;
import lombok.Setter;

/**
 * 教师确认认证
 * 
 * @author Geek-S
 *
 */
@Entity
@Getter
@Setter
public class ConfirmToken extends BaseDomain {

	private static final long serialVersionUID = 5730966574014090038L;

	/**
	 * 教师ID
	 */
	private Long teacherId;

	/**
	 * 教师类型
	 */
	private TeacherType teacherType;

	/**
	 * 源数据ID
	 */
	private Long sourseId;

	private Long courseLiveTeacherId;

	/**
	 * Token
	 */
	@Column(unique = true)
	private String token;

	/**
	 * 过期标志
	 */
	private Boolean expire;


	public ConfirmToken(){
		this.expire=false;//默认false 未过期
	}

}
