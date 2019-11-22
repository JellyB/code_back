package com.huatu.ztk.user.service;

import org.springframework.stereotype.Component;
import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;

/**
 * 注册送课信息
 * 
 * @author zhangchong
 *
 */
@Component
@DisconfFile(filename = "registerFreeCourseDetail.properties")
public class RegisterFreeCourseDetailConfig {

	/**
	 * 是否开启0否1是
	 */
	private Integer openRegisterFreeCourse;

	/**
	 * 开启时的提示内容
	 */
	private String regTitle;

	/**
	 * 注册成功标示
	 */
	private String title;

	private Integer coin;

	private Integer growUpValue;

	private String courseList;

	/**
	 * 课程id字符串
	 */
	private String registerClassIds;

	/**
	 * 是否是测试第一次登录
	 */
	private Integer testFirstLogin;
	
	
    /**
	 * 调用php加密key
	 */
    private String parentKey;
    
    
    @DisconfFileItem(name = "parentKey", associateField = "parentKey")
	public String getParentKey() {
		return parentKey;
	}

	public void setParentKey(String parentKey) {
		this.parentKey = parentKey;
	}

	@DisconfFileItem(name = "registerClassIds", associateField = "registerClassIds")
	public String getRegisterClassIds() {
		return registerClassIds;
	}

	public void setRegisterClassIds(String registerClassIds) {
		this.registerClassIds = registerClassIds;
	}

	@DisconfFileItem(name = "testFirstLogin", associateField = "testFirstLogin")
	public Integer getTestFirstLogin() {
		return testFirstLogin;
	}

	public void setTestFirstLogin(Integer testFirstLogin) {
		this.testFirstLogin = testFirstLogin;
	}

	@DisconfFileItem(name = "openRegisterFreeCourse", associateField = "openRegisterFreeCourse")
	public Integer getOpenRegisterFreeCourse() {
		return openRegisterFreeCourse;
	}

	public void seOpenRegisterFreeCourse(Integer openRegisterFreeCourse) {
		this.openRegisterFreeCourse = openRegisterFreeCourse;
	}

	@DisconfFileItem(name = "regTitle", associateField = "regTitle")
	public String getRegTitle() {
		return regTitle;
	}

	public void setRegTitle(String regTitle) {
		this.regTitle = regTitle;
	}

	@DisconfFileItem(name = "title", associateField = "title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@DisconfFileItem(name = "coin", associateField = "coin")
	public Integer getCoin() {
		return coin;
	}

	public void setCoin(Integer coin) {
		this.coin = coin;
	}

	@DisconfFileItem(name = "growUpValue", associateField = "growUpValue")
	public Integer getGrowUpValue() {
		return growUpValue;
	}

	public void setGrowUpValue(Integer growUpValue) {
		this.growUpValue = growUpValue;
	}

	@DisconfFileItem(name = "courseList", associateField = "courseList")
	public String getCourseList() {
		return courseList;
	}

	public void setCourseList(String courseList) {
		this.courseList = courseList;
	}

}
