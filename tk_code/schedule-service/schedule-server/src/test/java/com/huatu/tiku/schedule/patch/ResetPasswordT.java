package com.huatu.tiku.schedule.patch;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.huatu.tiku.schedule.ScheduleApplicationTests;
import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.repository.TeacherRepository;

/**
 * 重置密码
 * 
 * @author Geek-S
 *
 */
public class ResetPasswordT extends ScheduleApplicationTests {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private TeacherRepository teacherRepository;

	@Test
	public void name() {
		String password = "geeks";

		String passwordEncode = passwordEncoder.encode(password);

		List<Teacher> teachers = teacherRepository.findAll();
		teachers.forEach(teacher -> {
			teacher.setPassword(passwordEncode);

			teacherRepository.save(teacher);
		});
	}
}
