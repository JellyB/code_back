package com.huatu.tiku.schedule.repository;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.huatu.tiku.schedule.ScheduleApplicationTests;
import com.huatu.tiku.schedule.biz.repository.TeacherRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TeacherReportRepositoryT extends ScheduleApplicationTests {

	@Autowired
	private TeacherRepository teacherRepository;

	@Test
	public void getAuthorities() {
		Long id = 1L;

		Set<String> authorities = teacherRepository.getAuthorities(id);

		log.info("Authorities is {}", authorities);
	}

	@Test
	public void getUnavailableTime() {
		Long id = 1L;

		Date date = Date.from(LocalDate.now().atStartOfDay(ZoneOffset.systemDefault()).toInstant());

		teacherRepository.getUnavailableTime(id, date);
	}

	@Test
	public void existsByPhone() {
		String phone = "15288545471";

		boolean flag = teacherRepository.existsByPhone(phone);

		log.info("Flag is {}", flag);
	}

	@Test
	public void getRolesById() {
		Long id = 1L;

		List<Object[]> roles = teacherRepository.getRolesById(id);

		roles.forEach(role -> {
			log.info("Roles is {}", Arrays.toString(role));
		});

	}

	@Test
	public void findDataPermissionIdsById() {
		Long id = 1L;

		Set<Integer> dataPermissionIds = teacherRepository.findDataPermissionIdsById(id);

		log.info("DataPermissionIds is ", dataPermissionIds);
	}
}
