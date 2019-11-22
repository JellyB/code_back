package com.huatu.tiku.essay.test;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.huatu.tiku.essay.entity.EssayMockExam;
import com.huatu.tiku.essay.repository.EssayMaterialRepository;
import com.huatu.tiku.essay.repository.EssayMockExamRepository;
import com.huatu.tiku.essay.repository.EssayPaperBaseRepository;
import com.huatu.tiku.essay.repository.EssayQuestionBaseRepository;
import com.huatu.tiku.essay.service.EssayFileService;
import com.huatu.tiku.essay.test.correct.TikuBaseTest;
import com.huatu.tiku.essay.vo.resp.FileResultVO;

import lombok.extern.slf4j.Slf4j;

/**
 * 真题卷绑定到模考卷相关处理
 * 
 * @author zhangchong
 *
 */
@Slf4j
public class Paper2MockTest extends TikuBaseTest {

	@Autowired
	private EssayQuestionBaseRepository essayQuestionBaseRepository;

	@Autowired
	private EssayMockExamRepository essayMockExamRepository;

	@Autowired
	private EssayMaterialRepository essayMaterialRepository;

	@Autowired
	private EssayPaperBaseRepository essayPaperBaseRepository;
	
	@Autowired
	private EssayFileService essayFileService;
	
	@Autowired
	RestTemplate restTemplate;
	
	public static String GETMOCKPDFURL = "https://ns.huatu.com/e/api/wxApi/getMockPaperPdf";


	/**
	 * 真题卷id
	 */
	final Long paperId = 701L;

	/**
	 * 模考卷id
	 */
	final Long mockId = 700L;

	/**
	 * 真题卷绑定到模考卷
	 */
	@Test
	public void paperBindMock() {
		// 查询模考信息
		EssayMockExam mockExam = essayMockExamRepository.findOne(mockId);
		// select * from v_essay_question_base where paper_id = 634;
		// 删除旧的模考试题信息
		int updateQuestionCount = essayQuestionBaseRepository.updateStatus(mockId);
		log.info("删除旧模考绑定试题个数:{}", updateQuestionCount);
		// 删除旧模考材料信息
		int modifyMaterialCount = essayMaterialRepository.modifyByPaperId(mockId);
		log.info("删除旧模考绑定材料个数:{}", modifyMaterialCount);
		// 将真题卷中的试题信息绑定到模考卷中
		int updateQuestionPaper2Mock = essayQuestionBaseRepository.updatePaper2Mock(paperId, mockId);
		log.info("绑定到新模考的试题个数:{}", updateQuestionPaper2Mock);
		// 将真题卷中的材料信息绑定到模考卷中
		int updateMaterialPaper2Mock = essayMaterialRepository.updatePaper2Mock(paperId, mockId);
		log.info("绑定到新模考的材料个数:{}", updateMaterialPaper2Mock);
		// 删除真题卷
		int updateStatus = essayPaperBaseRepository.updateStatusById(-1, paperId);

		log.info("删除真题卷状态:{}", updateStatus);
		log.error("-----------------开始同步试卷pdf信息！");
		ResponseEntity<Object> forEntity = restTemplate.getForEntity(GETMOCKPDFURL+"?paperId="+mockId, Object.class);
		log.error("-----------------试卷pdf信息地址为:{}",forEntity.getBody());

	}

}
