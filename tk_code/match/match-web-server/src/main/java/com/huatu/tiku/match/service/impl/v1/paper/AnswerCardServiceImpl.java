package com.huatu.tiku.match.service.impl.v1.paper;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.match.bo.paper.*;
import com.huatu.tiku.match.common.MatchInfoRedisKeys;
import com.huatu.tiku.match.common.PaperErrorInfo;
import com.huatu.tiku.match.dto.paper.AnswerDTO;
import com.huatu.tiku.match.enums.AnswerCardInfoEnum;
import com.huatu.tiku.match.enums.PaperInfoEnum;
import com.huatu.tiku.match.listener.enums.RabbitMatchKeyEnum;
import com.huatu.tiku.match.service.v1.meta.MatchQuestionMetaService;
import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
import com.huatu.tiku.match.service.v1.meta.MetaHandlerService;
import com.huatu.tiku.match.service.v1.paper.*;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionMeta;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.RabbitHealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by lijun on 2018/10/19
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class AnswerCardServiceImpl implements AnswerCardService {

    final AnswerCardDBService answerCardDBService;

    final MatchUserMetaService matchUserMetaService;
    final MetaHandlerService metaHandlerService;
    final MatchQuestionMetaService matchQuestionMetaService;

    final QuestionService questionService;

    final PaperService paperService;
    final PaperMatchComponent paperMatchComponent;
    final PaperUserMetaService paperUserMetaService;
    @Value("${spring.profiles}")
    public String env;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    @Qualifier("GenericAnswerHandleServiceImpl")
    private AnswerHandleService answerHandleService;

    @Override
    public AnswerCardSimpleBo createAnswerCard(UserSession userSession, Integer paperId, int terminal) {
        //1.查询答题卡是否存已经创建,已经存在直接返回
        MatchUserMeta matchUserEnrollInfo = matchUserMetaService.findMatchUserEnrollInfo(userSession.getId(), paperId);
        if (null != matchUserEnrollInfo && null != matchUserEnrollInfo.getPracticeId() && matchUserEnrollInfo.getPracticeId() > 0) {
            //查询答题卡
            AnswerCard answerCard = findAnswerCard(matchUserEnrollInfo.getPracticeId());
            if (null != answerCard) {
                if (answerCard.getStatus() != AnswerCardInfoEnum.Status.FINISH.getCode()) {
                    return AnswerCardUtil.buildStandAnswerCardSimpleBo(answerCard);
                }
                PaperErrorInfo.AnswerCard.ANSWER_CARD_HAS_FINISHED.exception();
            }
        }
        //不存在答题卡，新建答题卡
        PaperInfoEnum.PaperTypeEnum paperTypeEnum = paperService.getPaperTypeById(paperId);
        switch (paperTypeEnum) {
            case MATCH:
                AnswerCard answerCard = paperMatchComponent.createAnswerCard(userSession, paperId, terminal);
                paperUserMetaService.addUndoPractice(userSession.getId(), paperId, answerCard.getId());
                //更新用户的报名数据
                //metaHandlerService.savePracticeId(answerCard.getId());
                return AnswerCardUtil.buildStandAnswerCardSimpleBo(answerCard);
            default:
                PaperErrorInfo.AnswerCard.CREATE_ERROR.exception();
        }
        return null;
    }

    @Override
    public AnswerCard findAnswerCard(Long answerCardId) {
        return answerCardDBService.findById(answerCardId);
    }

    @Override
    public List<Integer> getAnswerCardQuestionIdList(Long answerCardId) {
        AnswerCard answerCard = findAnswerCard(answerCardId);
        return getAnswerCardQuestionIdList(answerCard);
    }

    private List<Integer> getAnswerCardQuestionIdList(AnswerCard answerCard) {
        if (null == answerCard) {
            return Lists.newArrayList();
        }
        if (answerCard instanceof StandardCard) {
            Paper paper = ((StandardCard) answerCard).getPaper();
            if (null != paper) {
                return paperService.getPaperQuestionIdList(paper.getId());
            }
        }
        if (answerCard instanceof PracticeCard) {
            PracticePaper paper = ((PracticeCard) answerCard).getPaper();
            if (null != paper) {
                return paper.getQuestions();
            }
        }
        return Lists.newArrayList();
    }

    @Override
    public List<QuestionSimpleBo> getWrongQuestionAnalysis(Long answerCardId) {
        AnswerCard answerCard = findAnswerCard(answerCardId);
        validateGetAnalysisInfo(answerCard);
        Predicate<Integer> isUseFul = (correct) -> AnswerCardInfoEnum.Result.WRONG.valueEquals(correct);
        return buildAnswerAnalysisBo(answerCard, isUseFul);
    }

    @Override
    public List<QuestionSimpleBo> getAllQuestionAnalysis(Long answerCardId) {
        AnswerCard answerCard = findAnswerCard(answerCardId);
        validateGetAnalysisInfo(answerCard);
        Predicate<Integer> isUseFul = (correct) -> true;
        return buildAnswerAnalysisBo(answerCard, isUseFul);
    }

    /**
     * 获取答题卡-试题 解析
     *
     * @param answerCard 答题卡
     * @param isUseful   过滤 param correct[i]
     * @return
     */
    private List<QuestionSimpleBo> buildAnswerAnalysisBo(AnswerCard answerCard, Predicate<Integer> isUseful) {
        List<Integer> answerCardQuestionIdList = getAnswerCardQuestionIdList(answerCard);
        final int[] corrects = answerCard.getCorrects();
        final String[] answers = answerCard.getAnswers();
        final int[] doubts = answerCard.getDoubts();
        final int[] times = answerCard.getTimes();

        //构建答题卡统计信息
        final Consumer<QuestionSimpleBo> buildMetaInfo = questionSimpleBo -> {
            if (questionSimpleBo instanceof GenericQuestionAnalysisBo) {
                QuestionMeta meta = matchQuestionMetaService.getQuestionMeta(questionSimpleBo.getId());
                ((GenericQuestionAnalysisBo) questionSimpleBo).setMeta(meta);
            }
        };
        List<QuestionSimpleBo> result = IntStream.rangeClosed(0, answerCardQuestionIdList.size() - 1)
                .filter(index -> isUseful.test(corrects[index]))
                .mapToObj(index -> {
                    Question question = questionService.findQuestionCacheById(answerCardQuestionIdList.get(index));
                    QuestionSimpleBo questionSimpleBo = QuestionUtil.transQuestionInfoToAnalysisBo(question, buildMetaInfo);
                    if (questionSimpleBo instanceof GenericQuestionAnalysisBo) {
                        GenericAnswerAnalysisBo genericAnswerAnalysisBo = new GenericAnswerAnalysisBo();
                        BeanUtils.copyProperties(questionSimpleBo, genericAnswerAnalysisBo);
                        genericAnswerAnalysisBo.setAnswerCardIndex(index);
                        genericAnswerAnalysisBo.setCorrect(corrects[index]);
                        genericAnswerAnalysisBo.setExpireTime(times[index]);
                        genericAnswerAnalysisBo.setDoubt(doubts[index]);
                        genericAnswerAnalysisBo.setUserAnswer(answers[index]);
                        return genericAnswerAnalysisBo;
                    } else if (questionSimpleBo instanceof GenericSubjectiveQuestionAnalysisBo) {
                        ((GenericSubjectiveQuestionAnalysisBo) questionSimpleBo).setAnswerCardIndex(index);
                        return questionSimpleBo;
                    } else {
                        return null;
                    }
                })
                .filter(questionSimpleBo -> null != questionSimpleBo)
                .collect(Collectors.toList());
        //判断是否需要构建模块信息
        if (answerCard instanceof StandardCard) {
            final Paper paper = ((StandardCard) answerCard).getPaper();
            result.forEach(genericAnswerAnalysisBo -> QuestionUtil.buildModuleInfo(genericAnswerAnalysisBo, paper));
        }
        return result;
    }

    @Override
    public List<AnswerResultBo> save(Integer userId, Long practiceId, List<AnswerDTO> answerList) {
        if(CollectionUtils.isNotEmpty(answerList)){
            answerList.removeIf(i-> (StringUtils.isBlank(i.getAnswer()) || "0".equals(i.getAnswer()) || !NumberUtils.isDigits(i.getAnswer())));
        }
        AnswerCard answerCard = findAnswerCard(practiceId);
		log.info("save:practice {} status is {}", practiceId, answerCard == null ? null : answerCard.getStatus());
        validateUserAnswerCardInfo(userId, answerCard);
        if (!AnswerCardUtil.isEnabledSaveAnswerCard(answerCard)) {
            PaperErrorInfo.AnswerCard.SAVE_ANSWER_CARD_ERROR.exception();
        }
        List<AnswerResultBo> answerResultBos = answerHandleService.handleQuestionAnswer(answerList);
        //答题卡状态设置成已答
        if(answerCard.getStatus() == AnswerCardInfoEnum.Status.CREATE.getCode()){
            answerCard.setStatus(AnswerCardInfoEnum.Status.UNDONE.getCode());
        }
        saveAnswerInfoToAnswerCard(answerCard, answerResultBos);
        answerCardDBService.save(answerCard);
        return answerResultBos;
    }

    @Override
    public void submit(Integer userId, Long practiceId, List<AnswerDTO> answerList) {
        AnswerCard answerCard = findAnswerCard(practiceId);
        log.info("submit:practice {} status is {}", practiceId, answerCard.getStatus());
        if(CollectionUtils.isNotEmpty(answerList)){
            answerList.removeIf(i-> (StringUtils.isBlank(i.getAnswer()) || "0".equals(i.getAnswer()) || !NumberUtils.isDigits(i.getAnswer())));
        }
        save(userId, practiceId, answerList);
        if (!AnswerCardUtil.isEnabledSubmitAnswerCard(answerCard)) {
            PaperErrorInfo.AnswerCard.SUBMIT_ANSWER_CARD_ERROR.exception();
        }
        List<AnswerResultBo> answerResultBos = answerHandleService.handleQuestionAnswer(answerList);
        //答题卡状态设置成已完成
        answerCard.setStatus(AnswerCardInfoEnum.Status.FINISH.getCode());
        saveAnswerInfoToAnswerCard(answerCard, answerResultBos);
        answerCardDBService.saveToDB(answerCard);
        metaHandlerService.saveScore(answerCard.getId());
        if (answerCard instanceof StandardCard) {
            paperUserMetaService.addFinishPractice(answerCard.getUserId(), ((StandardCard) answerCard).getPaper().getId(), practiceId);
        }
        matchQuestionMetaService.handlerQuestionMeta(answerCard);
    }

    @Override
    public void submit2Queue(Integer userId, Long practiceId, List<AnswerDTO> answerList) {
        if(practiceId <= 0){
            return;
        }
        HashMap<Object, Object> map = Maps.newHashMap();
        map.put("userId", userId);
        map.put("practiceId", practiceId);
        map.put("answerList", JsonUtil.toJson(answerList));
        String answerCardKey = MatchInfoRedisKeys.getMatchSubmitAnswerCardIdSetKey();
        redisTemplate.opsForSet().add(answerCardKey,practiceId+"");
        rabbitTemplate.convertAndSend("", RabbitMatchKeyEnum.getQueue(RabbitMatchKeyEnum.AnswerCardSubmitAsync, env), map);
        redisTemplate.expire(answerCardKey,5, TimeUnit.MINUTES);       //5分钟缓存确保如果出现部分答题卡消费异常，跳过异常，去处理定时自动交卷逻辑
    }

    /**
     * 判断算分规则，并将答题信息存储到答题卡对象中
     *
     * @param answerCard
     * @param answerResultBos
     */
    private void saveAnswerInfoToAnswerCard(AnswerCard answerCard, List<AnswerResultBo> answerResultBos) {
        if (answerCard instanceof StandardCard) {
            Integer scoreFlag = 0;
            try {
                scoreFlag = ((StandardCard) answerCard).getPaper().getScoreFlag();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (null == scoreFlag || scoreFlag.intValue() == 0) {
                answerHandleService.saveAnswerInfoToAnswerCard(answerCard, answerResultBos, AnswerCardScoreFunction.getQuestionAverage());
            } else {
                answerHandleService.saveAnswerInfoToAnswerCard(answerCard, answerResultBos, AnswerCardScoreFunction.getQuestionScoreSum());
            }
        }
    }

    /**
     * 保存、提交 验证
     */
    private static void validateUserAnswerCardInfo(Integer userId, AnswerCard answerCard) {
        //答题卡不存在
        if (null == answerCard) {
            PaperErrorInfo.AnswerCard.PAPER_INFO_NOT_EXIT.exception();
        }
        //用户信息不匹配
        if (answerCard.getUserId() != userId.intValue()) {
            PaperErrorInfo.AnswerCard.USER_NOT_EXIT_IN_PAPER.exception();
        }
        //答题卡状态不匹配
//        if (AnswerCardInfoEnum.Status.CREATE.getCode() != answerCard.getStatus() && AnswerCardInfoEnum.Status.UNDONE.getCode() != answerCard.getStatus()) {
//            log.error("answerCard status error,id ={},message={}", answerCard.getId(), PaperErrorInfo.AnswerCard.ANSWER_CARD_HAS_FINISHED.getMessage());
//            PaperErrorInfo.AnswerCard.ANSWER_CARD_HAS_FINISHED.exception();
//        }
    }

    /**
     * 获取试题解析 验证
     */
    private static void validateGetAnalysisInfo(AnswerCard answerCard) {
        //答题卡不存在
        if (null == answerCard) {
            PaperErrorInfo.AnswerCard.PAPER_INFO_NOT_EXIT.exception();
        }
        //答题卡还未完成
//        if (AnswerCardInfoEnum.Status.FINISH.getCode() != answerCard.getStatus()) {
//            PaperErrorInfo.AnswerCard.ANSWER_CARD_NOT_FINISHED.exception();
//        }
    }
}
