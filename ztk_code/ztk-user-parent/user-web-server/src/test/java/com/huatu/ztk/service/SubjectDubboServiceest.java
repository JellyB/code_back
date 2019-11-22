package com.huatu.ztk.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.huatu.ztk.BaseTest;
import com.huatu.ztk.knowledge.api.SubjectDubboService;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author zhangchong
 *
 */
@Slf4j
public class SubjectDubboServiceest extends BaseTest {

	@Autowired
	private SubjectDubboService subjectDubboService;

	@Test
	public void getCategoryNameById() {
		String name = subjectDubboService.getCategoryNameById(41);
		log.info("获取的考试类型为:{}",name);
	}

}
