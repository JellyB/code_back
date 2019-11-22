package com.huatu.tiku.schedule.repository;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.huatu.tiku.schedule.ScheduleApplicationTests;
import com.huatu.tiku.schedule.biz.repository.SubjectRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubjectRepositoryT extends ScheduleApplicationTests {

	@Autowired
	private SubjectRepository subjectRepository;

	@Test
	public void count() {
		long count = subjectRepository.count();

		log.info("Count is {}", count);
	}
}
