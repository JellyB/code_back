package com.huatu.tiku.essay.test;

import java.util.LinkedList;
import java.util.List;

import com.huatu.tiku.essay.constant.status.EssayAnswerConstant;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.huatu.common.test.BaseWebTest;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.constant.status.EssayQuestionAnswerConstant;
import com.huatu.tiku.essay.entity.EssayMockUserMeta;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.repository.EssayMockExamRepository;
import com.huatu.tiku.essay.repository.EssayMockUserMetaRepository;
import com.huatu.tiku.essay.repository.EssayQuestionAnswerRepository;
import com.huatu.tiku.essay.vo.resp.EssayMockExamAnswerVO;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhouwei
 * @Description: 批量插入测试
 * @create 2018-03-31 下午6:02
 **/
@Slf4j
public class MockExamTest extends BaseWebTest {
    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;
    
    @Autowired
    EssayMockExamRepository essayMockExamRepository;
    
    @Autowired
    EssayMockUserMetaRepository essayMockUserMetaRepository;
    
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testBatchInsert() {
        List<EssayQuestionAnswer> essayQuestionAnswerList = new LinkedList<>();
        for (int i = 0; i < 2; i++) {
            EssayQuestionAnswer essayQuestionAnswer = EssayQuestionAnswer.builder()
                    .userId(11)
                    .terminal(1)
                    .questionType(1)
                    .areaId(1)
                    .areaName("北京")
                    .questionBaseId(11)
                    .questionYear(11 + "")
                    .questionDetailId(11)
                    .score(11)
                    .paperAnswerId(11)//答题卡对应的paperId是试卷答题卡
                    .paperId(11)
                    .build();
            essayQuestionAnswer.setStatus(EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus());
            essayQuestionAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus());
            essayQuestionAnswer.setCreator(1 + "");
            //  essayQuestionAnswer = essayQuestionAnswerRepository.save(essayQuestionAnswer);
            essayQuestionAnswerList.add(essayQuestionAnswer);
        }
        //换成批量插入，待测试  id是否会自动存到list中？
        essayQuestionAnswerRepository.save(essayQuestionAnswerList);
        //test code
        essayQuestionAnswerList.forEach(a -> log.info("zwtest:" + a.getId() + ""));
    }
    
    /**
     * 清除redis中模考答题卡信息
     */
    @Test
	public void cleanExamAnswer() {
		List<EssayMockUserMeta> list = essayMockUserMetaRepository.findByPaperIdAndStatus(634L, 1);
		list.forEach(meta -> {
			String examAnswerKey = RedisKeyConstant.getExamAnswerKey(634, meta.getUserId());
			redisTemplate.delete(examAnswerKey);
		});

	}
    
}
