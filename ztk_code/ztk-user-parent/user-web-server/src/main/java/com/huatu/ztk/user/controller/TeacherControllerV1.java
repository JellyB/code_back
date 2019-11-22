package com.huatu.ztk.user.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.user.service.SensorsUserService;
import com.huatu.ztk.user.service.UcenterService;
import com.huatu.ztk.user.service.UserService;
import com.huatu.ztk.user.service.UserSessionService;

/**
 * 教师网用户相关
 * 
 * @author zhangchong
 *
 */

@RestController
@RequestMapping(value = "/teacher/v1/users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class TeacherControllerV1 {
	// logger
	private static final Logger logger = LoggerFactory.getLogger(TeacherControllerV1.class);

	@Autowired
	private UserService userService;

	@Autowired
	private UserSessionService userSessionService;

	@Autowired
	private UcenterService ucenterService;

	@Autowired
	private SensorsUserService sensorsService;

	/**
	 * 教师网用户同步到ztk以及uc表
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "syncUserInfo", method = RequestMethod.POST)
	public Object syncTeacherUserInfo(@RequestBody List<String> params) {
		userService.syncTeacherUserInfo(params);
		return SuccessMessage.create("用户同步成功");
	}
	

}
