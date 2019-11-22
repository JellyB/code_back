package com.huatu.tiku.schedule.repository;

import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.huatu.tiku.schedule.ScheduleApplicationTests;
import com.huatu.tiku.schedule.biz.domain.Menu;
import com.huatu.tiku.schedule.biz.repository.MenuRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MenuRepositoryT extends ScheduleApplicationTests {

	@Autowired
	private MenuRepository menuRepository;

	@Test
	public void delete() {
		Long teacherId = 1L;

		Set<Menu> menus = menuRepository.getMenus(teacherId);

		menus.forEach(menu -> {
			log.info("Menu name is {}", menu.getName());
		});
	}
}
