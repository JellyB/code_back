package com.huatu.tiku.schedule.repository;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.ScheduleApplicationTests;
import com.huatu.tiku.schedule.biz.domain.Role;
import com.huatu.tiku.schedule.biz.repository.RoleRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RoleRepositoryT extends ScheduleApplicationTests {

	@Autowired
	private RoleRepository roleRepository;

	private final List<String> roleNames = Lists.newArrayList("运营", "教务", "客服", "讲师", "人力");

	@Test
	public void initRoles() {
		List<Role> roles = Lists.newArrayList();

		roleNames.forEach(roleName -> {
			Role role = new Role();
			role.setName(roleName);

			roles.add(role);
		});

		roleRepository.save(roles);
	}

	@Test
	public void findByTeachersId() {
		Long teacherId = 1L;

		Set<Role> roles = roleRepository.findByTeachersId(teacherId);

		roles.forEach(role -> {
			log.info("{}", role.getName());
		});
	}
}
