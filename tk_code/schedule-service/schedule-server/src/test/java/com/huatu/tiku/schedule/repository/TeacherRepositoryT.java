package com.huatu.tiku.schedule.repository;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.huatu.tiku.schedule.ScheduleApplicationTests;
import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.repository.TeacherRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TeacherRepositoryT extends ScheduleApplicationTests {

	@Autowired
	private TeacherRepository teacherRepository;

//	@Test
//	public void findByExamTypeAndSubjectId() {
//		ExamType examType = ExamType.GWY;
//
//		Long subjectId = 1L;
//
//		List<Teacher> teachers = teacherRepository.findByExamTypeAndSubjectId(examType, subjectId);
//
//		teachers.forEach(teacher -> {
//			log.info("Teacher name {}", teacher.getName());
//		});
//
//	}

}
