package com.huatu.tiku.essay.test.correct;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.huatu.tiku.essay.repository.EssayTeacherOrderTypeRepository;
import com.huatu.tiku.essay.repository.EssayTeacherRepository;

/**
 * 老师管理测试用例
 * 
 * @author zhangchong
 *
 */
public class TeacherTest extends TikuBaseTest {

	@Autowired
	EssayTeacherRepository essayTeacherRepository;

	@Autowired
	EssayTeacherOrderTypeRepository essayTeacherOrderTypeRepository;


	@Test
	public void test() {
//		essayTeacherRepository.findAll().forEach(teacher->{
//			essayTeacherOrderTypeRepository.findb
//		});
		
		essayTeacherOrderTypeRepository.findAll().stream().filter(orderType->orderType.getStatus() == 1).forEach(orderType->{
			orderType.setOrderLimit(orderType.getMaxOrderLimit());
			orderType.setReceiptRate(0);
			essayTeacherOrderTypeRepository.save(orderType);
		});
	}

}
