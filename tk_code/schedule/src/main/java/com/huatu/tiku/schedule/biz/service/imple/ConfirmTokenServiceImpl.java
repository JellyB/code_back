package com.huatu.tiku.schedule.biz.service.imple;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.domain.ConfirmToken;
import com.huatu.tiku.schedule.biz.domain.Course;
import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.enums.CourseConfirmStatus;
import com.huatu.tiku.schedule.biz.enums.TeacherType;
import com.huatu.tiku.schedule.biz.repository.ConfirmTokenRepository;
import com.huatu.tiku.schedule.biz.service.ConfirmTokenService;
import com.huatu.tiku.schedule.biz.service.CourseLiveService;
import com.huatu.tiku.schedule.biz.service.CourseLiveTeacherService;
import com.huatu.tiku.schedule.biz.service.CourseService;
import com.huatu.tiku.schedule.biz.service.TeacherService;

@Service
public class ConfirmTokenServiceImpl extends BaseServiceImpl<ConfirmToken, Long> implements ConfirmTokenService {

	private final ConfirmTokenRepository confirmTokenRepository;

	private final CourseService courseService;

	private final CourseLiveService courseLiveService;

	private final CourseLiveTeacherService courseLiveTeacherService;

	private TeacherService teacherService;

	public ConfirmTokenServiceImpl(ConfirmTokenRepository confirmTokenRepository, CourseService courseService,
			CourseLiveService courseLiveService, CourseLiveTeacherService courseLiveTeacherService,
			TeacherService teacherService) {
		this.confirmTokenRepository = confirmTokenRepository;
		this.courseService = courseService;
		this.courseLiveService = courseLiveService;
		this.courseLiveTeacherService = courseLiveTeacherService;
		this.teacherService = teacherService;
	}

	@Override
	public void confirm(String token, CourseConfirmStatus courseConfirmStatus) {
		ConfirmToken confirmToken = confirmTokenRepository.findByToken(token);

		if (confirmToken == null) {
			throw new BadRequestException("未找到相关记录");
		}

		if (confirmToken.getTeacherType().equals(TeacherType.JS)) {
			courseLiveTeacherService.updateTaskTeacher(confirmToken.getTeacherId(),
					Arrays.asList(confirmToken.getSourseId()), courseConfirmStatus);
		} else {
			Teacher teacher = teacherService.findOne(confirmToken.getTeacherId());

			courseLiveService.updateTaskTeacher(teacher, Arrays.asList(confirmToken.getSourseId()), courseConfirmStatus);
		}

		List<Course> courses = courseService.findAllByLives(Arrays.asList(confirmToken.getSourseId()));// 提交的直播id查找出课程列表

		for (Course course : courses) {
			courseService.updateStatus(course, confirmToken.getTeacherType());// 将课程状态更改
		}
	}

	@Override
	public ConfirmToken findByToken(String token) {
		return this.confirmTokenRepository.findByToken(token);
	}

}
