package com.huatu.tiku.essay.service.impl.courseExercises;

import com.google.common.collect.Maps;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.cache.PaperReportRedisKeyConstant;
import com.huatu.tiku.essay.constant.cache.QuestionReportRedisKeyConstant;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.EssayAnswerConstant;
import com.huatu.tiku.essay.entity.EssayLabelTotal;
import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.entity.courseExercises.EssayExercisesAnswerMeta;
import com.huatu.tiku.essay.essayEnum.CorrectFeedBackEnum;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.repository.EssayLabelTotalRepository;
import com.huatu.tiku.essay.repository.EssayPaperAnswerRepository;
import com.huatu.tiku.essay.repository.EssayQuestionAnswerRepository;
import com.huatu.tiku.essay.repository.courseExercises.EssayExercisesAnswerMetaRepository;
import com.huatu.tiku.essay.service.EssayCommonReportService;
import com.huatu.tiku.essay.service.EssaySimilarQuestionService;
import com.huatu.tiku.essay.service.ZtkUserService;
import com.huatu.tiku.essay.service.courseExercises.CourseExercisesReportService;
import com.huatu.tiku.essay.service.courseExercises.EssayExercisesAnswerMetaService;
import com.huatu.tiku.essay.service.v2.CorrectFeedBackService;
import com.huatu.tiku.essay.util.video.BjyHandler;
import com.huatu.tiku.essay.vo.admin.correct.CorrectFeedBackVo;
import com.huatu.tiku.essay.vo.resp.EssayPaperReportQuestionVO;
import com.huatu.tiku.essay.vo.resp.ManualCorrectReportVo;
import com.huatu.tiku.essay.vo.resp.correct.report.RemarkListVo;
import com.huatu.tiku.essay.vo.resp.correct.report.RemarkVo;
import com.huatu.tiku.essay.vo.resp.courseExercises.CourseExercisesCommonReportVo;
import com.huatu.tiku.essay.vo.resp.courseExercises.CourseExercisesPaperReportVo;
import com.huatu.tiku.essay.vo.resp.courseExercises.CourseExercisesQuestionReportVo;
import com.huatu.tiku.essay.vo.resp.courseExercises.UserScoreRankVo;
import com.huatu.tiku.essay.vo.user.ZtkUserVO;
import com.huatu.ztk.commons.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.huatu.tiku.essay.service.impl.EssayLabelServiceImpl.keepTwoDecimal;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/26
 * @描述
 */
@Service
@Slf4j
public class CourseExercisesReportServiceImpl implements CourseExercisesReportService {
    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;

    @Autowired
    EssaySimilarQuestionService essaySimilarQuestionService;

    @Autowired
    EssayLabelTotalRepository essayLabelTotalRepository;

    @Autowired
    BjyHandler bjyHandler;

    @Autowired
    private CorrectFeedBackService correctFeedBackService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ZtkUserService ztkUserService;

    @Autowired
    EssayExercisesAnswerMetaService essayExercisesAnswerMetaService;

    @Autowired
    EssayPaperAnswerRepository essayPaperAnswerRepository;

    @Autowired
    EssayCommonReportService essayCommonReportService;

    @Autowired
    EssayExercisesAnswerMetaRepository essayExercisesAnswerMetaRepository;


    @Override
    public CourseExercisesQuestionReportVo getQuestionReportMock(Long answerId, Long syllabusId) {
        CourseExercisesQuestionReportVo questionReportVo = CourseExercisesQuestionReportVo.builder().questionBaseId(51L).questionDetailId(51L).build();
        //批改基本信息
        String date = "2019.05.21 18:20";
        questionReportVo.setSubmitTime(date);
        questionReportVo.setCorrectDate(date);
        questionReportVo.setExamScore(60D);
        questionReportVo.setScore(100D);
        questionReportVo.setMaxScore(99);
        questionReportVo.setAvgScore(60);
        questionReportVo.setAvgSpendTime(10);
        questionReportVo.setAreaName("北京");

        //优秀成绩排名信息
        List<UserScoreRankVo> userScoreRankVos = new ArrayList<>();
        UserScoreRankVo userScoreRankVo = UserScoreRankVo.builder().rank(1)
                .userName("西小米")
                .examScore(40D)
                .spendTime(1000L)
                .avatar("http://tiku.huatu.com/cdn/images/vhuatu/avatars/default2.png")
                .submitTime("01月06日 20:10").build();
        userScoreRankVos.add(userScoreRankVo);
        questionReportVo.setUserScoreRankList(userScoreRankVos);

        //综合阅卷
        List<RemarkVo> remarkVoList = new ArrayList<>();
        RemarkVo remarkVo = RemarkVo.builder().sort(1)
                .content("基本功方面，卷面整洁，文笔流畅，字数够，总体来说基本功扎实")
                .build();
        remarkVoList.add(remarkVo);
        questionReportVo.setRemarkList(remarkVoList);

        //名师之声
        questionReportVo.setAudioId(24181098);
        questionReportVo.setAudioToken("l2iTp2pHYvI7H_Jmfnz5AhMCKip_LB1b4-SOyvppLCEJZ2jEH2OO3zTSEzZrILF4");

        //批改评价
        questionReportVo.setFeedBackStatus(1);
        questionReportVo.setFeedBackContent("老师批改的很好");
        questionReportVo.setFeedBackStar(5);

        //是否再次批改,查看报告
        questionReportVo.setCorrectNum(1);
        questionReportVo.setOtherAnswerCardId(2L);

        return questionReportVo;
    }

    @Override
    public void addQuestionCourseReport(EssayQuestionAnswer questionAnswer) {
        long answerId = questionAnswer.getId();
        if (null == questionAnswer ||
                null == questionAnswer.getAnswerCardType() ||
                EssayAnswerCardEnum.ModeTypeEnum.COURSE.getType() != questionAnswer.getAnswerCardType().intValue()) {
            return;
        }
        List<EssayExercisesAnswerMeta> metas = essayExercisesAnswerMetaService.findByAnswerIdAndType(answerId, EssayAnswerCardEnum.TypeEnum.QUESTION);
        if (CollectionUtils.isEmpty(metas)) {
            return;
        }
        EssayExercisesAnswerMeta essayExercisesAnswerMeta = metas.get(0);
        essayExercisesAnswerMeta.setExamScore(questionAnswer.getExamScore());
        essayExercisesAnswerMeta.setBizStatus(questionAnswer.getBizStatus());
        essayExercisesAnswerMetaService.save(essayExercisesAnswerMeta);

        long questionBaseId = questionAnswer.getQuestionBaseId();
        Long syllabusId = essayExercisesAnswerMeta.getSyllabusId();
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String questionSumScoreKey = QuestionReportRedisKeyConstant
                .getExercisesQuestionReportScoreSumKey(questionBaseId, syllabusId);
        String questionSumSpendTimeKey = QuestionReportRedisKeyConstant
                .getExercisesQuestionReportSpendTimeSumKey(questionBaseId, syllabusId);
        String questionReportScoreZsetKey = QuestionReportRedisKeyConstant
                .getExercisesQuestionReportScoreZsetKey(questionBaseId, syllabusId);
        log.info("课后作业总分key是:{},成绩排名:{},总耗时key:{}", questionSumScoreKey, questionReportScoreZsetKey,
                questionSumSpendTimeKey);
        Double score = zSetOperations.score(questionReportScoreZsetKey, answerId);
        if (null == score) {
            // 总分
            valueOperations.increment(questionSumScoreKey, questionAnswer.getExamScore());
            // 总耗时
            valueOperations.increment(questionSumSpendTimeKey, questionAnswer.getSpendTime());
        } else {
            zSetOperations.add(questionReportScoreZsetKey, answerId, questionAnswer.getExamScore() - score);
        }
        // 排名
        zSetOperations.add(questionReportScoreZsetKey, answerId,
                questionAnswer.getExamScore());

    }

    @Override
    public void addPaperCourseReport(EssayPaperAnswer paperAnswer) {
        long answerId = paperAnswer.getId();
        if (null == paperAnswer ||
                null == paperAnswer.getAnswerCardType() ||
                EssayAnswerCardEnum.ModeTypeEnum.COURSE.getType() != paperAnswer.getAnswerCardType().intValue()) {
            return;
        }
        List<EssayExercisesAnswerMeta> metas = essayExercisesAnswerMetaService.findByAnswerIdAndType(answerId, EssayAnswerCardEnum.TypeEnum.PAPER);
        if (CollectionUtils.isEmpty(metas)) {
            return;
        }
        EssayExercisesAnswerMeta essayExercisesAnswerMeta = metas.get(0);
        essayExercisesAnswerMeta.setExamScore(paperAnswer.getExamScore());
        essayExercisesAnswerMeta.setBizStatus(paperAnswer.getBizStatus());
        essayExercisesAnswerMetaService.save(essayExercisesAnswerMeta);

        long paperBaseId = paperAnswer.getPaperBaseId();
        Long syllabusId = essayExercisesAnswerMeta.getSyllabusId();
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String paperReportScoreSumKey = PaperReportRedisKeyConstant
                .getExercisesPaperReportScoreSumKey(paperBaseId, syllabusId);
        String paperReportSpendTimeSumKey = PaperReportRedisKeyConstant
                .getExercisesPaperReportSpendTimeSumKey(paperBaseId, syllabusId);
        String paperReportScoreZsetKey = PaperReportRedisKeyConstant
                .getExercisesPaperReportScoreZsetKey(paperBaseId, syllabusId);
        log.info("课后作业总分key是:{},成绩排名:{},总耗时key:{}", paperReportScoreSumKey, paperReportScoreZsetKey,
                paperReportSpendTimeSumKey);
        Double score = zSetOperations.score(paperReportScoreZsetKey, answerId);
        if (null == score) {
            // 总分
            valueOperations.increment(paperReportScoreSumKey, paperAnswer.getExamScore());
            // 总耗时
            valueOperations.increment(paperReportSpendTimeSumKey, paperAnswer.getSpendTime());
        } else {
            zSetOperations.add(paperReportScoreZsetKey, answerId, paperAnswer.getExamScore() - score);
        }
        // 排名
        zSetOperations.add(paperReportScoreZsetKey, answerId,
                paperAnswer.getExamScore());

    }

    @Override
    public CourseExercisesQuestionReportVo getQuestionReport(Long answerId, Long syllabusId) {
        // 2.1 答题卡信息
        EssayQuestionAnswer questionAnswer = essayQuestionAnswerRepository.findByIdAndStatus(answerId,
                EssayStatusEnum.NORMAL.getCode());
        if (null == questionAnswer) {
            throw new BizException(EssayErrors.ANSWER_CARD_ID_ERROR);
        }
        if (questionAnswer.getBizStatus() != EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus()) {
            throw new BizException(EssayErrors.ANSWER_CARD_NOT_CORRECTED);
        }
        if (questionAnswer.getCorrectMode() != CorrectModeEnum.MANUAL.getMode()) {
            throw new BizException(EssayErrors.ONlY_SUPPORT_MANUAL_CORRECT_MODE);
        }
        // 报告名称
        String correctDate = "";
        String submitTime = "";
        String reportName = essaySimilarQuestionService.getSimilarNameByQuestionId(questionAnswer.getQuestionBaseId());

        // 批改日期
        if (null != questionAnswer.getCorrectDate()) {
            correctDate = DateFormatUtils.format(questionAnswer.getCorrectDate(), "yyyy年MM月dd日 HH:mm:ss");
            submitTime = DateFormatUtils.format(questionAnswer.getSubmitTime(), "yyyy年MM月dd日 HH:mm:ss");
        }

        CourseExercisesQuestionReportVo reportVO = CourseExercisesQuestionReportVo.builder().questionBaseId(questionAnswer.getQuestionBaseId()).questionDetailId(questionAnswer.getQuestionDetailId()).build();
        reportVO.setScore(questionAnswer.getScore());
        reportVO.setExamScore(questionAnswer.getExamScore());
        reportVO.setCorrectDate(correctDate);
        reportVO.setCorrectMode(questionAnswer.getCorrectMode());
        reportVO.setSpendTime(questionAnswer.getSpendTime());
        reportVO.setReportName(reportName);
        reportVO.setAreaName(questionAnswer.getAreaName());
        reportVO.setPaperName(reportName);
        reportVO.setSubmitTime(submitTime);


        // 2.2 排名信息
        addReportRankInfo(questionAnswer, reportVO, syllabusId);

        // 2.3 名师之声
        List<EssayLabelTotal> essayLabelTotalList = essayLabelTotalRepository.findByAnswerIdAndStatus(answerId,
                EssayStatusEnum.NORMAL.getCode());
        if (CollectionUtils.isNotEmpty(essayLabelTotalList)) {
            EssayLabelTotal essayLabelTotal = essayLabelTotalList.get(0);
            int audioId = essayLabelTotal.getAudioId() == null ? -1 : essayLabelTotal.getAudioId();
            //设置视频信息
            reportVO.setAudioId(audioId);
            reportVO.setAudioToken(bjyHandler.getToken(audioId));

            // 2.4 本题阅卷
            String correctRemark = questionAnswer.getCorrectRemark();
            if (StringUtils.isNotEmpty(correctRemark)) {
                RemarkListVo remarkListVo = JsonUtil.toObject(correctRemark, RemarkListVo.class);
                reportVO.setRemarkList(remarkListVo.getQuestionRemarkList());
            }
        }
        List<CorrectFeedBackVo> feedBackList = correctFeedBackService.findByAnswerId(answerId, EssayAnswerCardEnum.TypeEnum.QUESTION.getType());
        // 2.5 批注评价内容
        if (CollectionUtils.isNotEmpty(feedBackList)) {
            CorrectFeedBackVo correctFeedBackVo = feedBackList.get(0);
            reportVO.setFeedBackStatus(CorrectFeedBackEnum.YES.getCode());
            reportVO.setFeedBackStar(correctFeedBackVo.getStar());
            reportVO.setFeedBackContent(correctFeedBackVo.getContent());
        } else {
            reportVO.setFeedBackStatus(CorrectFeedBackEnum.NO.getCode());
        }
        //查询另外一种答题卡状态
        List<EssayExercisesAnswerMeta> metas = essayExercisesAnswerMetaService.findByAnswerIdAndType(answerId, EssayAnswerCardEnum.TypeEnum.QUESTION);
        if (!CollectionUtils.isEmpty(metas)) {
            EssayExercisesAnswerMeta essayExercisesAnswerMeta = metas.get(0);
            reportVO.setCorrectNum(essayExercisesAnswerMeta.getCorrectNum());
            List<EssayExercisesAnswerMeta> allMetas = essayExercisesAnswerMetaService
                    .findByPQidAndAnswerTypeAndSyllabusIdAndUserIdAndStatus(essayExercisesAnswerMeta.getPQid(),
                            essayExercisesAnswerMeta.getAnswerType(), syllabusId, essayExercisesAnswerMeta.getUserId(), EssayStatusEnum.NORMAL.getCode());

            Optional<EssayExercisesAnswerMeta> otherMeta = allMetas.stream()
                    .filter(meta -> meta.getCorrectNum() != essayExercisesAnswerMeta.getCorrectNum()).findFirst();
            if (otherMeta.isPresent()) {
                reportVO.setOtherAnswerCardId((otherMeta.get().getAnswerId()));
                reportVO.setOtherAnswerBizStatus(otherMeta.get().getBizStatus());

            }
            essayExercisesAnswerMeta.getSyllabusId();
        }
        return reportVO;

    }

    /**
     * 组装排名信息
     */

    public void addReportRankInfo(EssayQuestionAnswer essayQuestionAnswer, CourseExercisesQuestionReportVo reportVo,
                                  Long syllabusId) {
        if (null == essayQuestionAnswer || null == reportVo) {
            return;
        }

        long questionBaseId = essayQuestionAnswer.getQuestionBaseId();
        long answerCardId = essayQuestionAnswer.getId();

        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String questionSumScoreKey = QuestionReportRedisKeyConstant
                .getExercisesQuestionReportScoreSumKey(questionBaseId, syllabusId);
        String questionSumSpendTimeKey = QuestionReportRedisKeyConstant
                .getExercisesQuestionReportSpendTimeSumKey(questionBaseId, syllabusId);
        String questionReportScoreZsetKey = QuestionReportRedisKeyConstant
                .getExercisesQuestionReportScoreZsetKey(questionBaseId, syllabusId);
        log.info("课后作业总分key是:{},成绩排名:{},总耗时key:{}", questionSumScoreKey, questionReportScoreZsetKey,
                questionSumSpendTimeKey);

        int totalCount = zSetOperations.size(questionReportScoreZsetKey).intValue();
        if (totalCount > 0) {
            // zSet不为空，且没有当前答题卡数据，将用户数据加入redis
            Double score = zSetOperations.score(questionReportScoreZsetKey, answerCardId);
            if (null == score) {
                // 排名
                zSetOperations.add(questionReportScoreZsetKey, essayQuestionAnswer.getId(),
                        essayQuestionAnswer.getExamScore());
                // 总分
                valueOperations.increment(questionSumScoreKey, essayQuestionAnswer.getExamScore());
                // 总耗时
                valueOperations.increment(questionSumSpendTimeKey, essayQuestionAnswer.getSpendTime());
            }
        } else {
            Set<String> keySet = Stream.of(questionReportScoreZsetKey, questionSumScoreKey, questionSumSpendTimeKey)
                    .collect(Collectors.toSet());
            redisTemplate.delete(keySet);
            // 从mysql中查询,查询所有答题记录
            List<EssayExercisesAnswerMeta> essayQuestionAnswerMetaList = essayExercisesAnswerMetaRepository
                    .findByPQidAndAnswerTypeAndSyllabusIdAndStatusAndBizStatus(questionBaseId,
                            EssayAnswerCardEnum.TypeEnum.QUESTION.getType(), syllabusId,
                            EssayStatusEnum.NORMAL.getCode(),
                            EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
            if (CollectionUtils.isNotEmpty(essayQuestionAnswerMetaList)) {
                totalCount = essayQuestionAnswerMetaList.size();
                essayQuestionAnswerMetaList.forEach(questionAnswer -> {
                    zSetOperations.add(questionReportScoreZsetKey, questionAnswer.getId(),
                            questionAnswer.getExamScore());
                    valueOperations.increment(questionSumSpendTimeKey, questionAnswer.getSpendTime());
                    valueOperations.increment(questionSumScoreKey, questionAnswer.getExamScore());
                });
            }
        }

        // 班级总分
        Object object = redisTemplate.opsForValue().get(questionSumScoreKey);
        Double scoreSum = (object == null ? 0 : Double.parseDouble(object.toString()));
        int totalRank;
        Long totalRankLong = zSetOperations.reverseRank(questionReportScoreZsetKey, answerCardId);
        if (null == totalRankLong) {
            totalRank = totalCount;
        } else {
            totalRank = totalRankLong.intValue() + 1;
        }
        // 最高分&& top10
        List<UserScoreRankVo> userRankList = new ArrayList<>();
        List<Long> answerIdList = new ArrayList<>();
        Double maxScore = 0D;
        Double examScore = 0D;
        Set<ZSetOperations.TypedTuple<Object>> examScoreList = zSetOperations
                .reverseRangeWithScores(questionReportScoreZsetKey, 0, 9);
        if (CollectionUtils.isNotEmpty(examScoreList)) {
            Integer rank = 0;
            maxScore = new ArrayList<>(examScoreList).get(0).getScore();
            for (TypedTuple<Object> typedTuple : examScoreList) {
                rank++;
                long answerId = Long.parseLong(typedTuple.getValue().toString());
                examScore = typedTuple.getScore();
                UserScoreRankVo userRank = UserScoreRankVo.builder().answerId(answerId).rank(rank).examScore(examScore)
                        .build();
                log.info("rank:{},score:{}", rank, examScore);
                userRankList.add(userRank);
                answerIdList.add(answerId);
            }
        }

        // 计算班级平均分
        double avgScore = scoreSum / (totalCount == 0 ? 1L : totalCount);
        avgScore = keepTwoDecimal(avgScore);
        // 计算班级平均用时
        Object spendTimeTotalObj = redisTemplate.opsForValue().get(questionSumSpendTimeKey);
        Integer spendTimeTotal = (spendTimeTotalObj == null ? 0 : Integer.parseInt(spendTimeTotalObj.toString()));
        double avgSpendTime = spendTimeTotal / (totalCount == 0 ? 1L : totalCount);
        avgSpendTime = keepTwoDecimal(avgSpendTime);
        reportVo.setAvgSpendTime(avgSpendTime);
        reportVo.setAvgScore(avgScore);
        reportVo.setMaxScore(maxScore);
        reportVo.setTotalRank(totalRank);

        List<EssayExercisesAnswerMeta> answerMetaList = essayExercisesAnswerMetaService
                .findByAnswerIdInAndTypeAndStatus(answerIdList, EssayAnswerCardEnum.TypeEnum.QUESTION.getType(),
                        EssayStatusEnum.NORMAL.getCode());

        List<Integer> uIds = new ArrayList<>();
        // 答题卡和用用信息map
        Map<Long, EssayExercisesAnswerMeta> answerAndMetaMap = Maps.newHashMap();
        answerMetaList.forEach(meta -> {
            Integer userId = meta.getUserId();
            Long answerId = meta.getAnswerId();
            uIds.add(userId);
            answerAndMetaMap.put(answerId, meta);
        });
        List<ZtkUserVO> userList = ztkUserService.getByIds(uIds);
        Map<Integer, ZtkUserVO> userMap = userList.stream()
                .collect(Collectors.toMap(user -> user.getId().intValue(), user -> user, (e1, e2) -> e1));
        userRankList.forEach(userRank -> {
            EssayExercisesAnswerMeta answerRankVO = answerAndMetaMap.get(userRank.getAnswerId());
            ZtkUserVO ztkUserVO = userMap.get(answerRankVO.getUserId());
            if (ztkUserVO != null) {
                userRank.setAvatar(ztkUserVO.getAvatar());
                userRank.setUserName(ztkUserVO.getNick());
                userRank.setSpendTime(answerRankVO.getSpendTime());
                Date submitTime = answerRankVO.getSubmitTime();
                if (null != submitTime) {
                    userRank.setSubmitTime(DateFormatUtils.format(submitTime, "MM月dd日 HH:mm"));
                }
            }
        });
        reportVo.setUserScoreRankList(getSortUserList.apply(userRankList));
    }

    /**
     * 套卷报告
     *
     * @param answerId
     * @return
     */
    public CourseExercisesPaperReportVo getRealPaperReport(Long answerId, Long courseWareId) {
        EssayPaperAnswer answerCard = essayPaperAnswerRepository.findByIdAndStatus(answerId, EssayStatusEnum.NORMAL.getCode());
        checkAnswerCardStatus(answerCard);

        CourseExercisesCommonReportVo reportVo = new CourseExercisesCommonReportVo();
        reportVo.setCorrectDate(answerCard.getCorrectDate() == null ? null : DateFormatUtils.format(answerCard.getCorrectDate(), "yyyy年MM月dd日 HH:mm:ss"));//需修改
        reportVo.setScore(answerCard.getScore());
        reportVo.setExamScore(answerCard.getExamScore());
        reportVo.setCorrectMode(answerCard.getCorrectMode());
        reportVo.setSpendTime(answerCard.getSpendTime());
        reportVo.setAreaName(answerCard.getAreaName());
        reportVo.setSubmitTime(answerCard.getSubmitTime() == null ? null : DateFormatUtils.format(answerCard.getSubmitTime(), "yyyy年MM月dd日 HH:mm:ss"));
        reportVo.setReportName(answerCard.getName()); //报告名称
        reportVo.setPaperName(answerCard.getName());//试卷名称

        //排名信息
        addCommonRankInfo(answerCard.getPaperBaseId(), answerId, EssayAnswerCardEnum.TypeEnum.PAPER.getType(),
                courseWareId, reportVo);
        //名师之声&&综合阅卷
        ManualCorrectReportVo remarkVo = essayCommonReportService.addPaperRemarkList(answerId, EssayAnswerCardEnum.TypeEnum.PAPER.getType(),
                answerCard.getCorrectRemark());
        if (null != remarkVo) {
            reportVo.setAudioId(remarkVo.getAudioId());
            reportVo.setAudioToken(remarkVo.getAudioToken());
            reportVo.setRemarkList(remarkVo.getRemarkList());
        }

        //学员批改评价
        ManualCorrectReportVo feeBackVo = essayCommonReportService.addFeedBack(answerId, EssayAnswerCardEnum.TypeEnum.PAPER.getType());
        if (null != feeBackVo) {
            reportVo.setFeedBackStar(feeBackVo.getFeedBackStar());
            reportVo.setFeedBackStatus(feeBackVo.getFeedBackStatus());
            reportVo.setFeedBackContent(feeBackVo.getFeedBackContent());
        }
        CourseExercisesPaperReportVo paperReportVo = new CourseExercisesPaperReportVo();
        BeanUtils.copyProperties(reportVo, paperReportVo);
        paperReportVo.setPaperId(answerCard.getPaperBaseId());

        //考试情况
        LinkedList<EssayPaperReportQuestionVO> paperQuestionList = essayCommonReportService.getPaperQuestionList(answerId, answerCard.getPaperBaseId());
        paperReportVo.setQuestionVOList(paperQuestionList);
        paperReportVo.setQuestionCount(paperQuestionList.size());

        //再次批改,返回另一张答题卡(是否有同样的答题卡)
        HashMap<String, Object> otherCardInfo = getOtherCardInfo(answerCard.getId(), answerCard.getPaperBaseId(), EssayAnswerCardEnum.TypeEnum.PAPER);
        Integer correctNum = (Integer) otherCardInfo.getOrDefault("correctNum", 1L);
        Long otherAnswerCard = (Long) otherCardInfo.get("otherAnswerCard");
        Integer otherAnswerBizStatus = (Integer) otherCardInfo.get("otherAnswerBizStatus");

        paperReportVo.setCorrectNum(correctNum);
        paperReportVo.setOtherAnswerCardId(otherAnswerCard);
        paperReportVo.setOtherAnswerBizStatus(otherAnswerBizStatus);

        return paperReportVo;
    }

    /**
     * 再次批改,查询另一张答题卡信息
     *
     * @param answerId
     * @param paperOrQuestionId
     * @param typeEnum
     * @return
     */
    public HashMap<String, Object> getOtherCardInfo(Long answerId, Long paperOrQuestionId, EssayAnswerCardEnum.TypeEnum typeEnum) {
        //再次批改,返回另一张答题卡(是否有同样的答题卡)
        HashMap map = Maps.newHashMap();
        List<EssayExercisesAnswerMeta> answerMetas = essayExercisesAnswerMetaService.findByAnswerIdAndType(answerId, typeEnum);
        if (CollectionUtils.isNotEmpty(answerMetas)) {
            EssayExercisesAnswerMeta userMeta = answerMetas.get(0);
            if (null != userMeta) {
                Integer correctNum = answerMetas.get(0).getCorrectNum();
                map.put("correctNum", correctNum);
                //返回另一个答题卡
                List<EssayExercisesAnswerMeta> otherAnswerCards = essayExercisesAnswerMetaRepository.findByUserIdAndPQidAndAnswerTypeAndSyllabusIdAndStatus(userMeta.getUserId(), paperOrQuestionId,
                        typeEnum.getType(), userMeta.getSyllabusId(), EssayStatusEnum.NORMAL.getCode());
                if (CollectionUtils.isNotEmpty(otherAnswerCards)) {
                    Optional<EssayExercisesAnswerMeta> otherMeta = otherAnswerCards.stream().filter(answer -> answer.getCorrectNum() != correctNum).findFirst();
                    if (otherMeta.isPresent()) {
                        map.put("otherAnswerCard", otherMeta.get().getAnswerId());
                        map.put("otherAnswerBizStatus", otherMeta.get().getBizStatus());
                    }
                }
            }
        }
        return map;
    }

    /**
     * 组装排名信息
     */

    public void addCommonRankInfo(Long paperOrQuestionId, Long answerCardId, Integer answerType, Long syllabusId,
                                  CourseExercisesCommonReportVo reportVo) {
        Double userExamScore = reportVo.getExamScore();
        Integer spendTime = reportVo.getSpendTime();

        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String questionSumScoreKey = QuestionReportRedisKeyConstant
                .getExercisesPaperReportScoreSumKey(paperOrQuestionId, syllabusId, EssayAnswerCardEnum.TypeEnum.PAPER.getType());
        String questionSumSpendTimeKey = QuestionReportRedisKeyConstant
                .getExercisesPaperSpendTimeSumKey(paperOrQuestionId, syllabusId, EssayAnswerCardEnum.TypeEnum.PAPER.getType());
        String questionReportScoreZsetKey = QuestionReportRedisKeyConstant
                .getExercisesPaperReportScoreZsetKey(paperOrQuestionId, syllabusId, EssayAnswerCardEnum.TypeEnum.PAPER.getType());

        log.info("课后作业总分key是:{},成绩排名key:{},总耗时key:{}", questionSumScoreKey, questionReportScoreZsetKey,
                questionSumSpendTimeKey);

        int totalCount = zSetOperations.size(questionReportScoreZsetKey).intValue();
        if (totalCount > 0) {
            // zSet不为空，且没有当前答题卡数据，将用户数据加入redis
            Double score = zSetOperations.score(questionReportScoreZsetKey, answerCardId);
            if (null == score) {
                // 排名
                zSetOperations.add(questionReportScoreZsetKey, answerCardId,
                        userExamScore);
                // 总分
                valueOperations.increment(questionSumScoreKey, userExamScore);
                // 总耗时
                valueOperations.increment(questionSumSpendTimeKey, spendTime);
            }
        } else {
            Set<String> keySet = Stream.of(questionReportScoreZsetKey, questionSumScoreKey, questionSumSpendTimeKey)
                    .collect(Collectors.toSet());
            redisTemplate.delete(keySet);

            //从mysql中查询,查询所有答题卡
            if (answerType == EssayAnswerCardEnum.TypeEnum.QUESTION.getType()) {
                List<EssayExercisesAnswerMeta> essayQuestionAnswerList = essayExercisesAnswerMetaRepository
                        .findByPQidAndAnswerTypeAndSyllabusIdAndStatusAndBizStatus(paperOrQuestionId, EssayAnswerCardEnum.TypeEnum.QUESTION.getType(),
                                syllabusId, EssayStatusEnum.NORMAL.getCode(), EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
                totalCount = essayQuestionAnswerList.size();
                if (CollectionUtils.isNotEmpty(essayQuestionAnswerList)) {
                    essayQuestionAnswerList.forEach(questionAnswer -> {
                        zSetOperations.add(questionReportScoreZsetKey, questionAnswer.getId(), questionAnswer.getExamScore());
                        valueOperations.increment(questionSumSpendTimeKey, questionAnswer.getSpendTime());
                        valueOperations.increment(questionSumScoreKey, questionAnswer.getExamScore());
                    });
                }
            } else {
                List<EssayExercisesAnswerMeta> essayPaperAnswerList = essayExercisesAnswerMetaRepository
                        .findByPQidAndAnswerTypeAndSyllabusIdAndStatusAndBizStatus(paperOrQuestionId, EssayAnswerCardEnum.TypeEnum.PAPER.getType(),
                                syllabusId, EssayStatusEnum.NORMAL.getCode(), EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());

                totalCount = essayPaperAnswerList.size();
                if (CollectionUtils.isNotEmpty(essayPaperAnswerList)) {
                    essayPaperAnswerList.forEach(paperAnswer -> {
                        log.info("试卷ID:{}", paperAnswer.getId());
                        zSetOperations.add(questionReportScoreZsetKey, paperAnswer.getId(), paperAnswer.getExamScore());
                        valueOperations.increment(questionSumSpendTimeKey, paperAnswer.getSpendTime());
                        valueOperations.increment(questionSumScoreKey, paperAnswer.getExamScore());
                    });
                }
            }
        }
        //
        // 班级总分
        Object object = redisTemplate.opsForValue().get(questionSumScoreKey);
        Double scoreSum = (object == null ? 0 : Double.parseDouble(object.toString()));
        int totalRank;
        Long totalRankLong = zSetOperations.reverseRank(questionReportScoreZsetKey, answerCardId);
        if (null == totalRankLong) {
            totalRank = totalCount;
        } else {
            totalRank = totalRankLong.intValue() + 1;
        }
        // 最高分&& top10
        List<UserScoreRankVo> userRankList = new ArrayList<>();
        List<Long> answerIdList = new ArrayList<>();
        Double maxScore = 0D;
        Double examScore = 0D;
        Set<ZSetOperations.TypedTuple<Object>> examScoreList = zSetOperations
                .reverseRangeWithScores(questionReportScoreZsetKey, 0, 9);
        if (CollectionUtils.isNotEmpty(examScoreList)) {
            Integer rank = 0;
            maxScore = new ArrayList<>(examScoreList).get(0).getScore();
            for (TypedTuple<Object> typedTuple : examScoreList) {
                rank++;
                long answerId = Long.parseLong(typedTuple.getValue().toString());
                examScore = typedTuple.getScore();
                UserScoreRankVo userRank = UserScoreRankVo.builder().answerId(answerId).rank(rank).examScore(examScore)
                        .build();
                log.info("套卷排名rank:{},score:{}", rank, examScore);
                userRankList.add(userRank);
                answerIdList.add(answerId);
            }
        }

        // 计算班级平均分
        double avgScore = scoreSum / (totalCount == 0 ? 1L : totalCount);
        avgScore = keepTwoDecimal(avgScore);
        // 计算班级平均用时
        Object spendTimeTotalObj = redisTemplate.opsForValue().get(questionSumSpendTimeKey);
        Integer spendTimeTotal = (spendTimeTotalObj == null ? 0 : Integer.parseInt(spendTimeTotalObj.toString()));
        double avgSpendTime = spendTimeTotal / (totalCount == 0 ? 1L : totalCount);
        avgSpendTime = keepTwoDecimal(avgSpendTime);
        reportVo.setAvgSpendTime(avgSpendTime);
        reportVo.setAvgScore(avgScore);
        reportVo.setMaxScore(maxScore);
        reportVo.setTotalRank(totalRank);
        reportVo.setUserScoreRankList(getUserScoreRankList(answerIdList, paperOrQuestionId, syllabusId));
    }

    /**
     * 根据答题卡ID批量获取用户排名信息
     *
     * @param answerIdList
     * @return
     */
    public List<UserScoreRankVo> getUserScoreRankList(List<Long> answerIdList, Long paperId, Long syllabusId) {

        List<UserScoreRankVo> userRankList = new ArrayList<>();
        if (CollectionUtils.isEmpty(answerIdList)) {
            return userRankList;
        }

        //获取用户答题卡信息
        /*log.info("参数是,试卷ID是:{},答题卡类型:{},大纲ID:{},批改状态:{},normal状态:{}",
                paperId, EssayAnswerCardEnum.TypeEnum.PAPER.getType(), syllabusId, EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(), EssayStatusEnum.NORMAL.getCode());
*/
        List<EssayExercisesAnswerMeta> answerMetaList = essayExercisesAnswerMetaRepository
                .findByPQidAndAnswerTypeAndSyllabusIdAndStatusAndBizStatus(paperId, EssayAnswerCardEnum.TypeEnum.PAPER.getType(),
                        syllabusId, EssayStatusEnum.NORMAL.getCode(), EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());

        List<Integer> userIds = answerMetaList.stream().map(EssayExercisesAnswerMeta::getUserId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIds)) {
            return userRankList;
        }
        List<ZtkUserVO> userList = ztkUserService.getByIds(userIds);
        if (CollectionUtils.isEmpty(userList)) {
            return userRankList;
        }
        userRankList = answerMetaList.stream().map(meta -> {
            UserScoreRankVo userScoreRankVo = new UserScoreRankVo();
            List<ZtkUserVO> ztkUserVOList = userList.stream().filter(user -> user.getId().intValue() == meta.getUserId())
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(ztkUserVOList)) {
                ZtkUserVO ztkUserVO = ztkUserVOList.get(0);
                userScoreRankVo.setAvatar(ztkUserVO.getAvatar());
                userScoreRankVo.setUserName(ztkUserVO.getNick());
                userScoreRankVo.setSpendTime(meta.getSpendTime());
                userScoreRankVo.setExamScore(meta.getExamScore());
                userScoreRankVo.setSubmitTime(meta.getSubmitTime() == null ? null : DateFormatUtils.format(meta.getSubmitTime(), "MM月dd日 HH:mm"));
            }
            return userScoreRankVo;
        }).collect(Collectors.toList());
        //排序规则
        return getSortUserList.apply(userRankList);
    }

    /**
     * 优秀成绩排名按照规则排序
     * 分数由高至低，分数相同用时有少之多。用时相同提交日期由远近
     */
    Function<List<UserScoreRankVo>, List<UserScoreRankVo>> getSortUserList = (userRankList -> {
        log.info("用户IDList是:{}", JsonUtil.toJson(userRankList));
        List<UserScoreRankVo> userScoreSortList = userRankList.stream()
                .filter(userScoreRankVo -> null != userScoreRankVo.getExamScore())
                .filter(userScoreRankVo -> null != userScoreRankVo.getSpendTime())
                .filter(userScoreRankVo -> null != userScoreRankVo.getSubmitTime())
                .sorted(Comparator.comparing(UserScoreRankVo::getExamScore).reversed()
                        .thenComparing(UserScoreRankVo::getSpendTime)
                        .thenComparing(UserScoreRankVo::getSubmitTime))
                .collect(Collectors.toList());
        int rank = 1;
        List<UserScoreRankVo> userScoreSortResult = new ArrayList<>();
        for (UserScoreRankVo vo : userScoreSortList) {
            vo.setRank(rank);
            rank++;
            userScoreSortResult.add(vo);
        }
        log.info("排序之后:{}", JsonUtil.toJson(userScoreSortResult));
        return userScoreSortList;
    });


    /**
     * 校验答题卡状态
     */
    private void checkAnswerCardStatus(EssayPaperAnswer answerCard) {
        if (null == answerCard) {
            throw new BizException(EssayErrors.ANSWER_CARD_ID_ERROR);
        }
        if (answerCard.getBizStatus() != EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus()) {
            throw new BizException(EssayErrors.ANSWER_CARD_NOT_CORRECTED);
        }
        if (answerCard.getCorrectMode() != CorrectModeEnum.MANUAL.getMode()) {
            throw new BizException(EssayErrors.ONlY_SUPPORT_MANUAL_CORRECT_MODE);
        }
    }


}
