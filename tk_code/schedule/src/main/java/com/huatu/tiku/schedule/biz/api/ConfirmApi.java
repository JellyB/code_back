package com.huatu.tiku.schedule.biz.api;

import javax.validation.Valid;

import com.huatu.tiku.schedule.biz.domain.*;
import com.huatu.tiku.schedule.biz.enums.CourseCategory;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.biz.dto.CourseConfirmDto;
import com.huatu.tiku.schedule.biz.service.ConfirmTokenService;
import com.huatu.tiku.schedule.biz.service.CourseLiveService;
import com.huatu.tiku.schedule.biz.service.CourseLiveTeacherService;
import com.huatu.tiku.schedule.biz.util.DateformatUtil;
import com.huatu.tiku.schedule.biz.util.TimeformatUtil;
import com.huatu.tiku.schedule.biz.vo.CourseConfirmVo;

/**
 * 教师确认课程
 * 
 * @author Geek-S
 *
 */
@RestController
@RequestMapping("api")
public class ConfirmApi {

	private final ConfirmTokenService confirmTokenService;

	private final CourseLiveService courseLiveService;

	private final CourseLiveTeacherService courseLiveTeacherService;

	public ConfirmApi(ConfirmTokenService confirmTokenService, CourseLiveService courseLiveService,
			CourseLiveTeacherService courseLiveTeacherService) {
		this.confirmTokenService = confirmTokenService;
		this.courseLiveService = courseLiveService;
		this.courseLiveTeacherService = courseLiveTeacherService;
	}

	/**
	 * 根据Token查询课程信息
	 * 
	 * @param token
	 *            token
	 */
	@GetMapping("confirm/info/{token}")
	public CourseConfirmVo confirm(@PathVariable String token) {
		ConfirmToken confirmToken = confirmTokenService.findByToken(token);

		if (confirmToken == null) {
			throw new BadRequestException("未找到相关记录");
		}

		// 直播
		CourseLive courseLive = courseLiveService.findOne(confirmToken.getSourseId());

		CourseConfirmVo courseConfirmVo = new CourseConfirmVo();
		courseConfirmVo.setCourseLiveName(courseLive.getName());
        Course course = courseLive.getCourse();
        courseConfirmVo.setCourseName(course.getName());
		courseConfirmVo.setCourseCategory(course.getCourseCategory().getText());
		if(CourseCategory.XXK.equals(course.getCourseCategory())){//如果线下课
            courseConfirmVo.setPlace(course.getPlace());
        }
		courseConfirmVo.setDate(
				DateformatUtil.format5(courseLive.getDate()) + " " + TimeformatUtil.format(courseLive.getTimeBegin())
						+ "-" + TimeformatUtil.format(courseLive.getTimeEnd()));

		switch (confirmToken.getTeacherType()){
			case JS:
//				CourseLiveTeacher courseLiveTeacher = courseLiveTeacherService
//						.findByCourseLiveIdAndTeacherId(courseLive.getId(), confirmToken.getTeacherId());
				CourseLiveTeacher courseLiveTeacher = courseLiveTeacherService.findOne(confirmToken.getCourseLiveTeacherId());
				courseConfirmVo.setCourseConfirmStatus(courseLiveTeacher.getConfirm());
				break;
			case ZJ:
				courseConfirmVo.setCourseConfirmStatus(courseLive.getAssConfirm());
				break;
			case XXS:
				courseConfirmVo.setCourseConfirmStatus(courseLive.getLtConfirm());
				break;
			case CK:
				courseConfirmVo.setCourseConfirmStatus(courseLive.getCtrlConfirm());
				break;
			case ZCR:
				courseConfirmVo.setCourseConfirmStatus(courseLive.getComConfirm());
				break;
		}
		courseConfirmVo.setExpire(confirmToken.getExpire());//设置过期标志
		return courseConfirmVo;
	}

	/**
	 * 根据Token确认课程
	 * 
	 * @param courseConfirmDto
	 *            token
	 */
	@PostMapping("confirm")
	public Boolean confirm(@Valid @RequestBody CourseConfirmDto courseConfirmDto, BindingResult bindingResult) {
		// 校验参数是否合法
		if (bindingResult.hasErrors()) {
			throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
		}

		confirmTokenService.confirm(courseConfirmDto.getToken(), courseConfirmDto.getCourseConfirmStatus());

		return true;
	}

}
