package com.huatu.tiku.essay.service.impl;

import com.huatu.tiku.essay.constant.cache.PaperReportRedisKeyConstant;
import com.huatu.tiku.essay.constant.status.AdminPaperConstant;
import com.huatu.tiku.essay.constant.status.EssayAnswerConstant;
import com.huatu.tiku.essay.constant.status.EssayQuestionAnswerConstant;
import com.huatu.tiku.essay.constant.status.QuestionTypeConstant;
import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.entity.EssayPaperBase;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.essayEnum.CorrectFeedBackEnum;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.essayEnum.LabelFlagEnum;
import com.huatu.tiku.essay.repository.EssayPaperAnswerRepository;
import com.huatu.tiku.essay.repository.EssayQuestionAnswerRepository;
import com.huatu.tiku.essay.repository.v2.IntelligenceConvertManualRecordRepository;
import com.huatu.tiku.essay.service.EssayCommonReportService;
import com.huatu.tiku.essay.service.EssayPaperReportService;
import com.huatu.tiku.essay.service.EssayPaperService;
import com.huatu.tiku.essay.service.EssaySimilarQuestionService;
import com.huatu.tiku.essay.service.correct.IntelligenceConvertManualRecordService;
import com.huatu.tiku.essay.service.paper.EssayPaperLabelService;
import com.huatu.tiku.essay.service.v2.CorrectFeedBackService;
import com.huatu.tiku.essay.service.v2.EssayTemplateService;
import com.huatu.tiku.essay.service.v2.question.QuestionCorrectDetailService;
import com.huatu.tiku.essay.util.video.BjyHandler;
import com.huatu.tiku.essay.vo.admin.AdminSingleQuestionVO;
import com.huatu.tiku.essay.vo.admin.correct.CorrectFeedBackVo;
import com.huatu.tiku.essay.vo.admin.correct.EssayPaperLabelTotalVo;
import com.huatu.tiku.essay.vo.resp.EssayPaperReportQuestionVO;
import com.huatu.tiku.essay.vo.resp.EssayPaperReportVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionTypeVO;
import com.huatu.tiku.essay.vo.resp.correct.report.RemarkListVo;
import com.huatu.ztk.commons.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EssayPaperReportServiceImpl implements EssayPaperReportService {

    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    EssayPaperAnswerRepository essayPaperAnswerRepository;
    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;
    @Autowired
    EssaySimilarQuestionService essaySimilarQuestionService;
    @Autowired
    EssayPaperService essayPaperService;

    @Autowired
    EssayPaperLabelService essayPaperLabelService;

    @Autowired
    EssayTemplateService essayTemplateService;

    @Autowired
    CorrectFeedBackService correctFeedBackService;

    @Autowired
    BjyHandler bjyHandler;

    @Autowired
    QuestionCorrectDetailService correctDetailService;

    @Autowired
    IntelligenceConvertManualRecordRepository recordRepository;

    @Autowired
    IntelligenceConvertManualRecordService recordService;

    @Autowired
    EssayCommonReportService essayCommonReportService;

    /**
     * 根据答题卡id获取报告
     *
     * @param answerCardId
     * @return
     */
    @Override
    public EssayPaperReportVO getReport(Long answerCardId) {

        //1.从缓存中获取
        String paperReportKey = PaperReportRedisKeyConstant.getPaperReportKey(answerCardId);
        try {
            EssayPaperReportVO reportVO = (EssayPaperReportVO) redisTemplate.opsForValue().get(paperReportKey);
            if (null != reportVO) {
                if (reportVO.getCorrectMode() != CorrectModeEnum.INTELLIGENCE.getMode()) {
                    getPaperRemark(reportVO, answerCardId);
                }
                reportVO.setConvertCount(getConvertCount(reportVO.getCorrectMode(), answerCardId));
                return reportVO;
            }
        } catch (Exception e) {
            redisTemplate.delete(paperReportKey);
            log.error("get EssayPaperReportVO from redis error:{}", e);
        }
        //2.缓存没命中，查询组装数据
        EssayPaperAnswer paperAnswer = essayPaperAnswerRepository.findOne(answerCardId);
        //2.1校验答题卡信息(模考卷不出报告)
        if (null != paperAnswer && paperAnswer.getType() == AdminPaperConstant.TRUE_PAPER) {
            EssayPaperBase paperBase = essayPaperService.findPaperInfoById(paperAnswer.getPaperBaseId());
            //2.1.1 组装基础数据
            //批改报告列表
            LinkedList<EssayPaperReportQuestionVO> questionList = essayCommonReportService.getPaperQuestionList(paperAnswer.getId(), paperAnswer.getPaperBaseId());
            String correctDate = "";
            if (paperAnswer.getCorrectDate() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                correctDate = dateFormat.format(paperAnswer.getCorrectDate());
            }
            EssayPaperReportVO.EssayPaperReportVOBuilder reportVOBuilder = EssayPaperReportVO.builder()
                    .paperName(paperAnswer.getName())
                    .correctDate(correctDate)
                    .score(paperAnswer.getScore())
                    .examScore(paperAnswer.getExamScore())
                    .unfinishedCount(paperAnswer.getUnfinishedCount())
                    .spendTime(paperAnswer.getSpendTime())
                    .questionCount(questionList.size())
                    .questionVOList(questionList)
                    .paperId(paperAnswer.getPaperBaseId())
                    .correctMode(paperAnswer.getCorrectMode())
                    .feedBackStar(0)
                    .type(paperBase.getType())
                    .feedBackContent("");

            //2.1.2 组装排名数据
            //如果排名相关数据没有持久化，查询组装数据,否则直接从答题卡中获取
            //if (null == paperAnswer.getTotalRankChange()) {
            /**
             *  将学员成绩插入zSet，求出排名，总人数，平均分，最高分
             *      1.zSet为空，该试卷排名数据没有写入redis。从数据库中读取并进行初始化
             *      2.zSet不为空，将当前数据加入redis开始排名计算
             *
             */
            String paperReportScoreZsetKey = PaperReportRedisKeyConstant.getPaperReportScoreZsetKey(paperAnswer.getPaperBaseId());
            int totalCount = redisTemplate.opsForZSet().size(paperReportScoreZsetKey).intValue();
            String paperReportScoreSumKey = PaperReportRedisKeyConstant.getPaperReportScoreSumKey(paperAnswer.getPaperBaseId());
            log.info("试卷分数:{},试卷总分:{}", paperReportScoreZsetKey, paperReportScoreSumKey);
            //1.该试卷排名数据完全没有写入redis，初始化数据
            if (totalCount == 0) {
                //确保总分从0开始累加，先清掉的总分数据
                redisTemplate.delete(paperReportScoreSumKey);
                //查询该套题所有答题卡
                List<EssayPaperAnswer> paperAnswerList = essayPaperAnswerRepository.findByPaperBaseIdAndStatusAndAnswerCardTypeAndBizStatus
                        (paperAnswer.getPaperBaseId(), EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                                paperAnswer.getAnswerCardType(),
                                EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
                //遍历答题卡数据，初始化redis数据
                if (CollectionUtils.isNotEmpty(paperAnswerList)) {
                    totalCount = paperAnswerList.size();
                    paperAnswerList.forEach(essayPaperAnswer -> {
                        redisTemplate.opsForZSet().add(paperReportScoreZsetKey, essayPaperAnswer.getId(), essayPaperAnswer.getExamScore());
                        redisTemplate.opsForValue().increment(paperReportScoreSumKey, essayPaperAnswer.getExamScore());
                    });
                }
            } else {
                //2.zSet不为空，且没有当前答题卡数据，将用户数据加入redis
                Double score = redisTemplate.opsForZSet().score(paperReportScoreZsetKey, paperAnswer.getId());
                log.info("答题卡ID是：{},分数是:{}", answerCardId, score);
                if (null == score) {
                    redisTemplate.opsForZSet().add(paperReportScoreZsetKey, answerCardId, paperAnswer.getExamScore());
                    redisTemplate.opsForValue().increment(paperReportScoreSumKey, paperAnswer.getExamScore());
                    totalCount += 1;
                }
            }

            //总排名
            Object object = redisTemplate.opsForValue().get(paperReportScoreSumKey);
            Double scoreSum = (object == null ? 0 : Double.parseDouble(object.toString()));
            int totalRank;
            Long totalRankLong = redisTemplate.opsForZSet().reverseRank(paperReportScoreZsetKey, answerCardId);
            if (null == totalRankLong) {
                totalRank = totalCount;
            } else {
                totalRank = totalRankLong.intValue() + 1;
            }
            //最高分
            ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
            Set<ZSetOperations.TypedTuple<Object>> examScoreList =
                    zSetOperations.reverseRangeWithScores(paperReportScoreZsetKey, 0, 1);
            double maxScore = paperAnswer.getExamScore();
            double secondMaxScore = paperAnswer.getExamScore();

            if (CollectionUtils.isNotEmpty(examScoreList)) {
                maxScore = new ArrayList<>(examScoreList).get(0).getScore();
                if (examScoreList.size() > 1) {
                    secondMaxScore = new ArrayList<>(examScoreList).get(1).getScore();
                }
            }

            //计算平均分
            double avgScore = scoreSum / (totalCount == 0 ? 1L : totalCount);
            avgScore = keepTwoDecimal(avgScore);


            //查询学员上次答题记录
            EssayPaperAnswer lastCard = new EssayPaperAnswer();
            List<EssayPaperAnswer> lastCardList = essayPaperAnswerRepository.findLastCard(paperAnswer.getPaperBaseId(), paperAnswer.getUserId(), paperAnswer.getCorrectDate());
            if (CollectionUtils.isNotEmpty(lastCardList)) {
                lastCard = lastCardList.get(0);
            }
            //持久化排名数据
            paperAnswer.setTotalRank(totalRank);
            paperAnswer.setTotalCount(totalCount);
            paperAnswer.setMaxScore(maxScore);
            paperAnswer.setAvgScore(avgScore);
            if (null != lastCard && lastCard.getId() > 0) {
                paperAnswer.setTotalRankChange((lastCard.getTotalRank() == 0 ? totalRank : lastCard.getTotalRank()) - totalRank);
                paperAnswer.setExamScoreChange(paperAnswer.getExamScore() - lastCard.getExamScore());
                paperAnswer.setMaxScoreChange(maxScore - (lastCard.getMaxScore() == 0 ? secondMaxScore : lastCard.getMaxScore()));
            } else {
                paperAnswer.setTotalRankChange(0);
                paperAnswer.setExamScoreChange(0D);
                paperAnswer.setMaxScoreChange(0D);
            }

            essayPaperAnswerRepository.save(paperAnswer);
            //}
            EssayPaperReportVO paperReportVO = reportVOBuilder.totalRank(paperAnswer.getTotalRank())
                    .totalCount(paperAnswer.getTotalCount())
                    .totalRankChange(null == paperAnswer.getTotalRankChange() ? 0 : paperAnswer.getTotalRankChange())
                    .maxScore(paperAnswer.getMaxScore())
                    .avgScore(paperAnswer.getAvgScore())
                    .examScoreChange(null == paperAnswer.getExamScoreChange() ? 0 : paperAnswer.getExamScoreChange())
                    .maxScoreChange(null == paperAnswer.getMaxScoreChange() ? 0 : paperAnswer.getMaxScoreChange())
                    .build();
            if (paperAnswer.getCorrectMode() != CorrectModeEnum.INTELLIGENCE.getMode()) {
                //评语内容
                this.addRemarkList(paperReportVO, paperAnswer);
                //学员评价
                getPaperRemark(paperReportVO, answerCardId);
            }
            //智能转人工次数统计
            paperReportVO.setConvertCount(getConvertCount(paperAnswer.getCorrectMode(), answerCardId));
            redisTemplate.opsForValue().set(paperReportKey, paperReportVO, 30, TimeUnit.MINUTES);
            return paperReportVO;

        } else {
            log.error("答题卡id错误，answerCardId：{}", answerCardId);
            return null;
        }
    }


    public static Double keepTwoDecimal(Double aDouble) {
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        if (aDouble == null) {
            return 0x0.0p0;
        } else {
            return Double.valueOf(decimalFormat.format(aDouble));
        }
    }

    /**
     * 人工批改，添加名师之声 和 综合评语相关
     *
     * @param paperReportVO
     */
    private void addRemarkList(EssayPaperReportVO paperReportVO, EssayPaperAnswer paperAnswer) {
        if (null == paperAnswer) {
            return;
        }
        EssayPaperLabelTotalVo paperLabelMark = essayPaperLabelService.getPaperLabelMark(paperAnswer.getId(), LabelFlagEnum.STUDENT_LOOK);
        if (null == paperLabelMark) {
            return;
        }
        //名师之声
        paperReportVO.setAudioId(paperLabelMark.getAudioId());
        paperReportVO.setAudioToken(bjyHandler.getToken(paperLabelMark.getAudioId()));
        // 综合阅卷评语信息
        if (StringUtils.isNotEmpty(paperAnswer.getCorrectRemark())) {
            RemarkListVo remarkListVo = JsonUtil.toObject(paperAnswer.getCorrectRemark(), RemarkListVo.class);
            if (null != remarkListVo) {
                paperReportVO.setRemarkList(remarkListVo.getPaperRemarkList());
            }
        }
    }

    /**
     * 评语信息
     *
     * @param paperReportVO
     * @param answerCardId
     */
    public void getPaperRemark(EssayPaperReportVO paperReportVO, long answerCardId) {
        List<CorrectFeedBackVo> correctFeedBackVoList = correctFeedBackService.findByAnswerId(answerCardId, EssayAnswerCardEnum.TypeEnum.PAPER.getType());
        if (CollectionUtils.isNotEmpty(correctFeedBackVoList)) {
            paperReportVO.setFeedBackStatus(CorrectFeedBackEnum.YES.getCode());
            CorrectFeedBackVo correctFeedBackVo = correctFeedBackVoList.get(0);
            paperReportVO.setFeedBackStar(correctFeedBackVo.getStar());
            paperReportVO.setFeedBackContent(correctFeedBackVo.getContent());
        } else {
            paperReportVO.setFeedBackStatus(CorrectFeedBackEnum.NO.getCode());
        }
    }

    public Integer getConvertCount(int correctMode, long answerCardId) {
        if (correctMode == CorrectModeEnum.INTELLIGENCE.getMode()) {
            List<Long> convertOrderIds = recordService.getConvertOrderIds(answerCardId, QuestionTypeConstant.PAPER);
            if (CollectionUtils.isNotEmpty(convertOrderIds)) {
                return convertOrderIds.size();
            }
        }
        return 0;
    }
}
