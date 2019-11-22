package com.huatu.ztk.user.common;

import java.util.List;

import lombok.Data;

/**
 * 注册送课信息
 * 
 * @author zhangchong
 *
 */
@Data
public class RegisterFreeCourseDetailVo {

	/**
	 * 是否开启0否1是
	 */
	// private Integer openRegisterFreeCourse;

	/**
	 * 开启时的提示内容
	 */
	// private String regTitle;

	/**
	 * 注册成功标示
	 */
	private String rtitle;

	private Integer rcoin;

	private Integer rgrowUpValue;

	private List<CourseInfo> rcourseList;

}
