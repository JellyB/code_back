package com.huatu.tiku.essay.test;

import com.huatu.common.test.BaseWebTest;
import com.huatu.tiku.essay.constant.status.EssayAnswerReductRuleConstant;
import com.huatu.tiku.essay.constant.status.EssayAnswerRuleConstant;
import com.huatu.tiku.essay.constant.status.EssayQuestionConstant;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionDetail;
import com.huatu.tiku.essay.entity.EssayStandardAnswerRule;
import com.huatu.tiku.essay.repository.EssayQuestionAnswerRepository;
import com.huatu.tiku.essay.repository.EssayQuestionBaseRepository;
import com.huatu.tiku.essay.repository.EssayQuestionDetailRepository;
import com.huatu.tiku.essay.repository.EssayStandardAnswerRuleRepository;
import com.huatu.tiku.essay.service.LabelXmlService;
import com.huatu.tiku.essay.service.impl.EssayQuestionServiceImpl;
import com.huatu.tiku.essay.util.admin.EssayAnalyzeUtil;
import com.huatu.tiku.essay.web.controller.admin.EssayXmlUtilController;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.assertj.core.util.Sets;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

/**
 * Created by huangqp on 2018\1\3 0003.
 */
@Slf4j
public class QuestionTest extends BaseWebTest {
	@Autowired
	private EssayQuestionDetailRepository essayQuestionDetailRepository;
	@Autowired
	private EssayQuestionAnswerRepository essayQuestionAnswerRepository;

	@Autowired
	private EssayQuestionBaseRepository essayQuestionBaseRepository;
	@Autowired
	private EssayStandardAnswerRuleRepository essayStandardAnswerRuleRepository;
	@Autowired
	private EssayQuestionServiceImpl essayQuestionService;
	@Autowired
	private LabelXmlService labelXmlService;

	@Test
	public void test() {
		List<EssayQuestionDetail> questionDetails = essayQuestionDetailRepository
				.findByStatusNot(EssayQuestionConstant.EssayQuestionStatusEnum.DELETED.getStatus());
		Set<Long> noLimitSet = Sets.newHashSet();
		Set<Long> compositeLimitSet = Sets.newHashSet();
		Set<Long> limitSet = Sets.newHashSet();
		for (EssayQuestionDetail detail : questionDetails) {
			List<EssayStandardAnswerRule> essayStandardAnswerRules = essayStandardAnswerRuleRepository
					.findByQuestionDetailIdAndTypeAndBizStatusAndStatus(detail.getId(),
							EssayAnswerReductRuleConstant.WORDNUM_LIMIT,
							EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
							EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());
			if (CollectionUtils.isEmpty(essayStandardAnswerRules)) {
				log.error("detail no existed limit ,id={}", detail.getId());
				noLimitSet.add(detail.getId());
				continue;
			}
			if (essayStandardAnswerRules.size() > 1) {
				log.error("detail has limit composited,,id={}", detail.getId());
				compositeLimitSet.add(detail.getId());
			}
			detail.setInputWordNumMin(essayStandardAnswerRules.get(0).getMinNum());
			detail.setInputWordNumMax(essayStandardAnswerRules.get(0).getMaxNum());
			essayQuestionDetailRepository.save(detail);
			limitSet.add(detail.getId());
		}
		log.info("nolimit={}", noLimitSet);
		log.info("compositelimit={}", compositeLimitSet);
		log.info("limit={}", limitSet);
	}

	@Test
	public void test2() {
		List<EssayQuestionDetail> questionDetails = essayQuestionDetailRepository
				.findByStatusNot(EssayQuestionConstant.EssayQuestionStatusEnum.DELETED.getStatus());
		log.info("有{}个试题需要更新", questionDetails.size());
		int total = 0;
		for (EssayQuestionDetail questionDetail : questionDetails) {
			EssayAnalyzeUtil.splitAnalyze(questionDetail);
			total++;
			log.info("count:{}", total);
		}
		essayQuestionDetailRepository.save(questionDetails);
	}

	@Test
	public void testHtml() {
		String content = "<p>asdasdasdas</p>";
		System.out.println("cone:" + essayQuestionService.convertImgAndHtml(content, "1"));
	}

	@Test
	public void testHtmEl() throws Exception {
//        essayXmlUtilController.testXml(14518);
		labelXmlService.findTotalAndProduceXml(14518);
	}

	/**
	 * 纠正批改类型字段 根据qtype
	 */
	@Test
	public void updateCorrectType() throws Exception {

		List<EssayQuestionDetail> questionDetails = essayQuestionDetailRepository
				.findByStatusNot(EssayQuestionConstant.EssayQuestionStatusEnum.DELETED.getStatus());

		for (EssayQuestionDetail detail : questionDetails) {
			Integer type = detail.getType();
			if (type == 4) {
				// 变为关键句
				detail.setCorrectType(2);

			} else {
				detail.setCorrectType(1);
			}
			essayQuestionDetailRepository.save(detail);

			List<EssayQuestionAnswer> findByQuestionDetail = essayQuestionAnswerRepository
					.findByQuestionDetailId(detail.getId());
			for (EssayQuestionAnswer qAnswer : findByQuestionDetail) {
				qAnswer.setCorrectType(type == 4 ? 2 : 1);
				essayQuestionAnswerRepository.save(qAnswer);
			}
		}
	}

}
