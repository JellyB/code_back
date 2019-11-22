package com.huatu.tiku.schedule.biz.api;

import java.util.List;

import javax.validation.Valid;

import com.google.common.collect.Lists;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.biz.domain.ConfirmToken;
import com.huatu.tiku.schedule.biz.domain.Course;
import com.huatu.tiku.schedule.biz.domain.CourseLive;
import com.huatu.tiku.schedule.biz.domain.CourseLiveTeacher;
import com.huatu.tiku.schedule.biz.dto.CourseConfirmDto;
import com.huatu.tiku.schedule.biz.enums.CourseCategory;
import com.huatu.tiku.schedule.biz.service.ConfirmTokenService;
import com.huatu.tiku.schedule.biz.service.CourseLiveService;
import com.huatu.tiku.schedule.biz.service.CourseLiveTeacherService;
import com.huatu.tiku.schedule.biz.service.CourseService;
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

	private final CourseService courseService;

	public ConfirmApi(ConfirmTokenService confirmTokenService, CourseLiveService courseLiveService,
			CourseLiveTeacherService courseLiveTeacherService, CourseService courseService) {
		this.confirmTokenService = confirmTokenService;
		this.courseLiveService = courseLiveService;
		this.courseLiveTeacherService = courseLiveTeacherService;
		this.courseService = courseService;
	}

	/**
	 * 根据Token查询课程信息
	 * 
	 * @param token
	 *            token
	 */
	@GetMapping("confirm/info/{token}")
	public List<CourseConfirmVo> confirm(@PathVariable String token) {
		ConfirmToken confirmToken = confirmTokenService.findByToken(token);

		if (confirmToken == null) {
			throw new BadRequestException("未找到相关记录");
		}

		List<CourseConfirmVo> courseConfirmVos = Lists.newArrayList();

		// 课程
		Course course = courseService.findOne(confirmToken.getSourseId());

		// 直播
		List<CourseLive> courseLives = courseLiveService.findByCourseIdAndTeacherId(course.getId(),
				confirmToken.getTeacherId());
		if(null!=courseLives&&!courseLives.isEmpty()){
			courseLives.sort((o1,o2)->{
				int result=o1.getDateInt()-o2.getDateInt();//日期排序
				result=result==0?o1.getTimeBegin()-o2.getTimeBegin():result;//开始时间排序
				result=result==0?o1.getTimeEnd()-o2.getTimeEnd():result;//结束时间排序
				result=result==0?o1.getId().intValue()-o2.getId().intValue():result;
				return result;
			});
		}


		courseLives.forEach(courseLive -> {
			CourseConfirmVo courseConfirmVo = new CourseConfirmVo();
			courseConfirmVo.setCourseLiveName(courseLive.getName());
			courseConfirmVo.setCourseName(course.getName());
			courseConfirmVo.setCourseCategory(course.getCourseCategory().getText());

			CourseLiveTeacher courseLiveTeacher = courseLiveTeacherService
					.findByCourseLiveIdAndTeacherId(courseLive.getId(), confirmToken.getTeacherId());

			courseConfirmVo.setCourseLiveTeacherId(courseLiveTeacher.getId());
			courseConfirmVo.setCourseConfirmStatus(courseLiveTeacher.getConfirm());

			// 如果线下课
			if (CourseCategory.XXK.equals(course.getCourseCategory())) {
				courseConfirmVo.setPlace(course.getPlace());
			}

			courseConfirmVo.setDate(DateformatUtil.format5(courseLive.getDate()) + " "
					+ TimeformatUtil.format(courseLive.getTimeBegin()) + "-"
					+ TimeformatUtil.format(courseLive.getTimeEnd()));
			courseConfirmVo.setExpire(confirmToken.getExpire());// 设置过期标志

			courseConfirmVos.add(courseConfirmVo);
		});

		return courseConfirmVos;
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

		confirmTokenService.confirm(courseConfirmDto.getToken(), courseConfirmDto.getCourseLiveTeacherIds(),
				courseConfirmDto.getCourseConfirmStatus());

		return true;
	}

}
