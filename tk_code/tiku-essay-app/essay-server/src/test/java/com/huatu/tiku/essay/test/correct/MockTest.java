package com.huatu.tiku.essay.test.correct;


import com.google.common.collect.Lists;
import com.huatu.tiku.common.bean.reward.RewardMessage;
import com.huatu.tiku.common.consts.RabbitConsts;
import com.huatu.tiku.essay.constant.cache.QuestionReportRedisKeyConstant;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.entity.correct.CorrectFeedBack;
import com.huatu.tiku.essay.entity.vo.report.EssayAnswerCardVO;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.essayEnum.LabelFlagEnum;
import com.huatu.tiku.essay.mq.listeners.ManualCorrectFinishListener;
import com.huatu.tiku.essay.repository.EssayPaperAnswerRepository;
import com.huatu.tiku.essay.repository.v2.EssayCorrectFeedBackRepository;
import com.huatu.tiku.essay.service.EssayMockExamService;
import com.huatu.tiku.essay.service.EssayQuestionPdfService;
import com.huatu.tiku.essay.service.question.EssayQuestionLabelService;
import com.huatu.tiku.essay.service.v2.EssayTemplateService;
import com.huatu.tiku.essay.service.v2.question.QuestionCorrectDetailService;
import com.huatu.tiku.essay.vo.admin.correct.EssayPaperLabelTotalVo;
import com.huatu.tiku.essay.vo.admin.correct.LabelCommentRelationVO;
import com.huatu.tiku.essay.vo.resp.correct.report.RemarkListVo;
import com.huatu.tiku.essay.web.controller.admin.v2.paper.EssayPaperCorrectController;
import com.huatu.tiku.springboot.basic.reward.RewardAction;
import com.huatu.ztk.commons.JsonUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

public class MockTest extends TikuBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(MockTest.class);


    @Autowired
    EssayMockExamService essayMockExamService;

    @Autowired
    EssayPaperAnswerRepository essayPaperAnswerRepository;

    @Autowired
    EssayQuestionPdfService essayQuestionPdfService;

    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    EssayPaperCorrectController essayPaperCorrectController;
    @Autowired
    EssayTemplateService essayTemplateService;

    @Autowired
    EssayCorrectFeedBackRepository essayCorrectFeedBackRepository;

    @Autowired
    QuestionCorrectDetailService questionCorrectDetailService;

    @Autowired
    EssayQuestionLabelService essayQuestionLabelService;

    @Autowired
    ManualCorrectFinishListener manualCorrectFinishListener;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void test() {
        EssayAnswerCardVO essayAnswerCardVO = new EssayAnswerCardVO(essayMockExamService.getReport(233646703, 634, 1, EssayAnswerCardEnum.ModeTypeEnum.NORMAL));
        //fix 安卓7.1.8以及7.1.7 模考大赛闪退bug修复

        EssayAnswerCardVO essayAnswerCardResult = essayMockExamService.dealAnswerCardScore(essayAnswerCardVO);

        System.out.println("essayAnswerCardResult = " + essayAnswerCardResult.getMatchMeta().getPositionRank());
    }

    @Test
    public void test2() {
        long paperId = 634L;
        List<EssayPaperAnswer> answers = essayPaperAnswerRepository.findByPaperBaseIdAndStatusAndAnswerCardTypeAndBizStatus(paperId, 1, EssayAnswerCardEnum.ModeTypeEnum.NORMAL.getType(), 3);
        for (EssayPaperAnswer answer : answers) {
            restScore(paperId, answer);
        }
    }

    @Test
    public void test3() {
        long paperId = 634L;
        EssayPaperAnswer answer = essayPaperAnswerRepository.findByIdAndStatus(319004L, 1);
        restScore(paperId, answer);
    }

    private void restScore(long paperId, EssayPaperAnswer answer) {
        String essayUserScoreKey = RedisKeyConstant.getEssayUserScoreKey(paperId);
        redisTemplate.opsForZSet().add(essayUserScoreKey, answer.getUserId(), answer.getExamScore());
        //地区排名
        String essayUserAreaScoreKey = RedisKeyConstant.getEssayUserAreaScoreKey(paperId, answer.getAreaId());
        redisTemplate.opsForZSet().add(essayUserAreaScoreKey, answer.getUserId(), answer.getExamScore());

    }

    /**
     * 人工批改
     */
    @Test
    public void test4() {
        //Object paperLabelMark = essayPaperCorrectController.getPaperLabelMark(4557);
        //logger.info("结果信息是:{}", paperLabelMark);
    }


    /**
     * 人工批改保存评语
     */
    @Test
    public void test5() {


        List<LabelCommentRelationVO> remarkList = Lists.newArrayList();

        LabelCommentRelationVO first = LabelCommentRelationVO.builder()
                .bizId("1,2,3")
                .bizType(3)
                .commentId(1)
                .labelId(1)
                .type(1).build();//套卷批注


        LabelCommentRelationVO second = LabelCommentRelationVO.builder()
                .bizId("4,5,6")
                .bizType(3)
                .commentId(2)
                .labelId(2)
                .type(1).build();//套卷批注
        remarkList.add(first);
        remarkList.add(second);


        EssayPaperLabelTotalVo resultVo = EssayPaperLabelTotalVo.builder().paperAnswerCardId(4556)
                .audioId(124)
                .remarkList(remarkList)
                .elseRemark("我是其他评语")
                .paperId(181)
                .paperScore(50D)
                .build();

        //essayPaperCorrectController.save(resultVo);
    }


    /**
     * 人工批改保存评语
     */
    @Test
    public void test6() {

        essayPaperCorrectController.delete(4557L);
    }

    @Test
    public void test7() {


    }


    @Test
    public void test8() {
        CorrectFeedBack correctFeedBack = new CorrectFeedBack();
        correctFeedBack.setAnswerId(12456L);
        correctFeedBack.setAnswerType(1);
        correctFeedBack.setContent("我是测试3");
        essayCorrectFeedBackRepository.save(correctFeedBack);
    }

    @Test
    public void testUploadAudio() {
        long totalId = 1L;
        int questionType = 1;
        //List<RemarkVo> detailRemarkList = questionCorrectDetailService.getDetailRemarkList(4872L, 1);
        //logger.info("结果是:{}", JSONObject.parseObject(detailRemarkList.toString()));
    }


    @Test
    public void testRemarkList() {
        //questionCorrectDetailService.saveRemarkList(21204, 0, , 1);
        //essayQuestionLabelService.updateRemarkInfo(6975, 22365);
    }

    @Test
    public void test9() {
        RemarkListVo id = questionCorrectDetailService.getQuestionRemarkListInfo(0, 374, "我是其他批注");
        logger.info("结果是:{}", JsonUtil.toJson(id));
    }

    /**
     * 套卷的本题阅卷子
     */
    @Test
    public void test10() {
        manualCorrectFinishListener.getPaperRemark(4914, LabelFlagEnum.STUDENT_LOOK);
    }


    @Test
    public void test11() {
        String singleCorrectPdfPath = essayQuestionPdfService.getMultiCorrectPdfPath(332748L);
        System.out.println("singleCorrectPdfPath = " + singleCorrectPdfPath);
    }

    /**
     * 活动赠送金币
     */
    @Test
    public void sendCoins() {

        RewardMessage msg = RewardMessage.builder().gold(100000).action(RewardAction.ActionType.ACTIVTY.name())
                .experience(1).bizId(System.currentTimeMillis() + "").uname("app_ztk1725863157")
                .timestamp(System.currentTimeMillis()).build();
        rabbitTemplate.convertAndSend("", RabbitConsts.QUEUE_REWARD_ACTION, msg);

    }

    @Test
    public void mockTest() {
        String paperConvertCount = QuestionReportRedisKeyConstant.getPaperConvertCount(0, 1974L);
        redisTemplate.delete(paperConvertCount);
    }


}
