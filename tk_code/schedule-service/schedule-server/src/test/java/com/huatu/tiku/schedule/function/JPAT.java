package com.huatu.tiku.schedule.function;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.huatu.tiku.schedule.ScheduleApplicationTests;
import com.huatu.tiku.schedule.biz.domain.CourseLive;
import com.huatu.tiku.schedule.biz.repository.CourseLiveRepository;

import java.util.Arrays;

public class JPAT extends ScheduleApplicationTests {




	@Autowired
	private CourseLiveRepository courseLiveRepository;

	/**
	 * 测试直接修改主键来复制数据
	 */
	@Test
	public void save() {
		Long id = 1L;

		CourseLive courseLive = courseLiveRepository.findOne(id);
		courseLive.setId(null);

		courseLiveRepository.save(courseLive);
	}

	/**
	 * 测试直接修改主键来复制数据
	 */
	@Test
	public void save1() {
		Long id = 1L;

		CourseLive courseLive = courseLiveRepository.findOne(id);

		courseLive.setId(2L);

		courseLive.setName("测试");

		courseLiveRepository.save(courseLive);
	}
}
