package com.huatu.tiku.essay.service.v2.impl.question;

import com.google.common.collect.Lists;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.cache.QuestionReportRedisKeyConstant;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.EssayAnswerConstant;
import com.huatu.tiku.essay.entity.EssayLabelTotal;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.essayEnum.CorrectFeedBackEnum;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.repository.EssayLabelTotalRepository;
import com.huatu.tiku.essay.repository.EssayQuestionAnswerRepository;
import com.huatu.tiku.essay.repository.EssayQuestionDetailRepository;
import com.huatu.tiku.essay.service.EssaySimilarQuestionService;
import com.huatu.tiku.essay.service.comment.AdminCommentTemplateService;
import com.huatu.tiku.essay.service.v2.CorrectFeedBackService;
import com.huatu.tiku.essay.service.v2.EssayTemplateService;
import com.huatu.tiku.essay.service.v2.question.QuestionReportService;
import com.huatu.tiku.essay.service.v2.question.QuestionTypeService;
import com.huatu.tiku.essay.util.video.BjyHandler;
import com.huatu.tiku.essay.vo.admin.correct.CorrectFeedBackVo;
import com.huatu.tiku.essay.vo.resp.correct.report.EssayQuestionCorrectReportVO;
import com.huatu.tiku.essay.vo.resp.correct.report.RemarkListVo;
import com.huatu.ztk.commons.JsonUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.huatu.tiku.essay.service.impl.EssayLabelServiceImpl.keepTwoDecimal;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/11
 * @描述 人工批改, 单题批改报告
 */
@Service
public class QuestionReportServiceImpl implements QuestionReportService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionReportServiceImpl.class);

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;

    @Autowired
    EssayQuestionDetailRepository essayQuestionDetailRepository;

    @Autowired
    EssayLabelTotalRepository essayLabelTotalRepository;

    @Autowired
    EssayTemplateService essayTemplateService;

    @Autowired
    AdminCommentTemplateService adminCommentTemplateService;

    @Autowired
    QuestionTypeService questionTypeService;

    @Autowired
    CorrectFeedBackService correctFeedBackService;

    @Autowired
    BjyHandler bjyHandler;

    @Autowired
    EssaySimilarQuestionService essaySimilarQuestionService;


    /**
     * 试题答题卡ID
     *
     * @param answerId
     * @return
     */
    public EssayQuestionCorrectReportVO getQuestionReport(long answerId) {

        //1.学员报告缓存在redis中,缓存有,直接返回
        String questionReportKey = QuestionReportRedisKeyConstant.getQuestionReportKey(answerId);
        logger.info("单题报告key是:{}", questionReportKey);
        try {
            EssayQuestionCorrectReportVO questionReportVO = (EssayQuestionCorrectReportVO) redisTemplate.opsForValue().get(questionReportKey);
            if (null != questionReportVO) {
                getPaperFeedBack(questionReportVO, answerId);
                return questionReportVO;
            }
        } catch (Exception e) {
            logger.info("getQuestionReport ,answerId是:{},error:{}", answerId, e);
        }

        //2.缓存中无,重新组装数据
        //2.1 答题卡信息
        EssayQuestionAnswer questionAnswer = essayQuestionAnswerRepository.findByIdAndStatus(answerId,
                EssayStatusEnum.NORMAL.getCode());
        if (null == questionAnswer) {
            throw new BizException(EssayErrors.ANSWER_CARD_ID_ERROR);
        }
        if (questionAnswer.getBizStatus() != EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus()) {
            throw new BizException(EssayErrors.ANSWER_CARD_NOT_CORRECTED);
        }
        if (questionAnswer.getCorrectMode() == CorrectModeEnum.INTELLIGENCE.getMode()) {
            throw new BizException(EssayErrors.ONlY_SUPPORT_MANUAL_CORRECT_MODE);
        }
        // 报告名称
        String correctDate = "";
        String reportName = essaySimilarQuestionService.getSimilarNameByQuestionId(questionAnswer.getQuestionBaseId());

        //批改日期
        if (null != questionAnswer.getCorrectDate()) {
            correctDate = DateFormatUtils.format(questionAnswer.getCorrectDate(), "yyyy年MM月dd日 HH:mm:ss");
        }

        EssayQuestionCorrectReportVO reportVO = EssayQuestionCorrectReportVO.builder()
                .score(questionAnswer.getScore())
                .examScore(questionAnswer.getExamScore())
                .correctDate(correctDate)
                .questionBaseId(questionAnswer.getQuestionBaseId())
                .questionDetailId(questionAnswer.getQuestionDetailId())
                .audioToken("")
                .questionBaseId(questionAnswer.getQuestionBaseId())
                .remarkList(Lists.newArrayList())
                .correctMode(questionAnswer.getCorrectMode())
                .spendTime(questionAnswer.getSpendTime())
                .paperName(reportName)
                .feedBackStar(0)
                .feedBackContent("")
                .build();


        //2.2  排名信息
        addReportRankInfo(questionAnswer, reportVO);

        //2.3 名师之声
        List<EssayLabelTotal> essayLabelTotalList = essayLabelTotalRepository.findByAnswerIdAndStatus(answerId, EssayStatusEnum.NORMAL.getCode());
        if (CollectionUtils.isNotEmpty(essayLabelTotalList)) {
            EssayLabelTotal essayLabelTotal = essayLabelTotalList.stream().findFirst().get();
            int audioId = essayLabelTotal.getAudioId() == null ? -1 : essayLabelTotal.getAudioId();
            reportVO.setAudioId(audioId);
            reportVO.setAudioToken(bjyHandler.getToken(audioId));

            //2.4 本题阅卷
            String correctRemark = questionAnswer.getCorrectRemark();
            if (StringUtils.isNotEmpty(correctRemark)) {
                RemarkListVo remarkListVo = JsonUtil.toObject(correctRemark, RemarkListVo.class);
                reportVO.setRemarkList(remarkListVo.getQuestionRemarkList());
            }
        }
        //2.5 批注评价内容
        getPaperFeedBack(reportVO, answerId);

        if (null != reportVO) {
            redisTemplate.opsForValue().set(questionReportKey, reportVO);
            redisTemplate.expire(questionReportKey, 15, TimeUnit.MINUTES);
        }
        return reportVO;
    }


    /**
     * 组装排名信息
     */

    public void addReportRankInfo(EssayQuestionAnswer essayQuestionAnswer, EssayQuestionCorrectReportVO questionCorrectReportVo) {
        if (null == essayQuestionAnswer || null == questionCorrectReportVo) {
            return;
        }

        long questionBaseId = essayQuestionAnswer.getQuestionBaseId();
        long answerCardId = essayQuestionAnswer.getId();

        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String questionSumScoreKey = QuestionReportRedisKeyConstant.getQuestionReportScoreSumKey(questionBaseId);
        String questionReportScoreZsetKey = QuestionReportRedisKeyConstant.getQuestionReportScoreZsetKey(questionBaseId);
        logger.info("总分key是:{},成绩排名:{}", questionSumScoreKey, questionReportScoreZsetKey);

        int totalCount = zSetOperations.size(questionReportScoreZsetKey).intValue();
        if (totalCount > 0) {
            //zSet不为空，且没有当前答题卡数据，将用户数据加入redis
            Double score = zSetOperations.score(questionReportScoreZsetKey, answerCardId);
            if (null == score) {
                zSetOperations.add(questionReportScoreZsetKey, essayQuestionAnswer.getId(), essayQuestionAnswer.getExamScore());
                valueOperations.increment(questionSumScoreKey, essayQuestionAnswer.getExamScore());
            }
        } else {
            redisTemplate.delete(questionReportScoreZsetKey);
            //从mysql中查询,查询所有答题卡
            List<EssayQuestionAnswer> essayQuestionAnswerList = essayQuestionAnswerRepository.findByQuestionBaseIdAndStatusAndBizStatusAndAnswerCardType(questionBaseId,
                    EssayStatusEnum.NORMAL.getCode(),
                    EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(),essayQuestionAnswer.getAnswerCardType()
                    );
            if (CollectionUtils.isNotEmpty(essayQuestionAnswerList)) {
                totalCount = essayQuestionAnswerList.size();
                essayQuestionAnswerList.forEach(questionAnswer -> {
                    zSetOperations.add(questionReportScoreZsetKey, questionAnswer.getId(), questionAnswer.getExamScore());
                    valueOperations.increment(questionSumScoreKey, questionAnswer.getExamScore());
                });
            }
        }

        //全站总数量
        Object object = redisTemplate.opsForValue().get(questionSumScoreKey);
        Double scoreSum = (object == null ? 0 : Double.parseDouble(object.toString()));
        int totalRank;
        Long totalRankLong = zSetOperations.reverseRank(questionReportScoreZsetKey, answerCardId);
        if (null == totalRankLong) {
            totalRank = totalCount;
        } else {
            totalRank = totalRankLong.intValue() + 1;
        }
        //最高分
        Double maxScore = 0D;
        Set<ZSetOperations.TypedTuple<Object>> examScoreList =
                zSetOperations.reverseRangeWithScores(questionReportScoreZsetKey, 0, 1);
        if (CollectionUtils.isNotEmpty(examScoreList)) {
            maxScore = new ArrayList<>(examScoreList).get(0).getScore();
        }

        //计算平均分
        double avgScore = scoreSum / (totalCount == 0 ? 1L : totalCount);
        avgScore = keepTwoDecimal(avgScore);

        questionCorrectReportVo.setTotalCount(totalCount);
        questionCorrectReportVo.setTotalRank(totalRank);
        questionCorrectReportVo.setMaxScore(maxScore);
        questionCorrectReportVo.setAvgScore(avgScore);
    }

    /**
     * 报告评价信息
     *
     * @param reportVO
     * @param answerId
     */
    public void getPaperFeedBack(EssayQuestionCorrectReportVO reportVO, long answerId) {
        //2.5 批注评价内容
        List<CorrectFeedBackVo> correctFeedBackVoList = correctFeedBackService.findByAnswerId(answerId, EssayAnswerCardEnum.TypeEnum.QUESTION.getType());
        if (CollectionUtils.isNotEmpty(correctFeedBackVoList)) {
            CorrectFeedBackVo correctFeedBackVo = correctFeedBackVoList.get(0);
            reportVO.setFeedBackStatus(CorrectFeedBackEnum.YES.getCode());
            reportVO.setFeedBackStar(correctFeedBackVo.getStar());
            reportVO.setFeedBackContent(correctFeedBackVo.getContent());
        }else {
            reportVO.setFeedBackStatus(CorrectFeedBackEnum.NO.getCode());
        }
    }
}
