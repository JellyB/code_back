package com.huatu.tiku.essay.service.impl.correct;

import com.google.common.collect.Maps;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.constant.cache.QuestionReportRedisKeyConstant;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.EssayAnswerConstant;
import com.huatu.tiku.essay.constant.status.EssayCorrectGoodsConstant;
import com.huatu.tiku.essay.constant.status.EssayCorrectGoodsConstant.GoodsTypeEnum;
import com.huatu.tiku.essay.constant.status.QuestionTypeConstant;
import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.entity.correct.IntelligenceConvertManualRecord;
import com.huatu.tiku.essay.essayEnum.*;
import com.huatu.tiku.essay.essayEnum.CorrectOrderStatusEnum.DelayStatusEnum;
import com.huatu.tiku.essay.repository.EssayPaperAnswerRepository;
import com.huatu.tiku.essay.repository.EssayQuestionAnswerRepository;
import com.huatu.tiku.essay.repository.EssayUserCorrectGoodsRepository;
import com.huatu.tiku.essay.repository.v2.CorrectOrderRepository;
import com.huatu.tiku.essay.repository.v2.IntelligenceConvertManualRecordRepository;
import com.huatu.tiku.essay.service.correct.CorrectOrderService;
import com.huatu.tiku.essay.service.correct.CorrectServiceV2;
import com.huatu.tiku.essay.service.correct.IntelligenceConvertManualRecordService;
import com.huatu.tiku.essay.service.correct.UserCorrectGoodsServiceV4;
import com.huatu.tiku.essay.service.v2.question.QuestionTypeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-07-11 7:57 PM
 **/
@Slf4j
@Service
public class CorrectServiceImplV2 implements CorrectServiceV2 {


    @Autowired
    private EssayQuestionAnswerRepository essayQuestionAnswerRepository;

    @Autowired
    private EssayPaperAnswerRepository essayPaperAnswerRepository;

    @Autowired
    private QuestionTypeService questionTypeService;

    @Autowired
    private CorrectOrderService correctOrderService;

    @Autowired
    private EssayUserCorrectGoodsRepository essayUserCorrectGoodsRepository;

    @Autowired
    private UserCorrectGoodsServiceV4 userCorrectGoodsServiceV4;

    @Autowired
    private CorrectOrderRepository correctOrderRepository;

    @Autowired
    private IntelligenceConvertManualRecordRepository recordRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IntelligenceConvertManualRecordService recordService;

    /**
     * 智能转人工批改
     *
     * @param answerCardId
     * @param type
     * @param modeTypeEnum
     * @return
     */
    @Override
    public Object convert(final Long answerCardId, final Integer type, final Integer delayStatus, UserSession userSession, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) throws BizException {
        Map<String, Object> data = Maps.newHashMap();
        TeacherOrderTypeEnum teacherOrderTypeEnum = null;
        try {
            Optional<ErrorResult> result = convertStep2(answerCardId, type, delayStatus, userSession,modeTypeEnum);
            if (result.isPresent()) {
                throw new BizException(result.get());
            } else {
                if (EssayAnswerCardEnum.TypeEnum.QUESTION.getType() == type.intValue()) {
                    EssayQuestionAnswer essayQuestionAnswer = essayQuestionAnswerRepository.findOne(answerCardId);
                    teacherOrderTypeEnum = TeacherOrderTypeEnum.convert(essayQuestionAnswer.getQuestionType());
                } else {
                    teacherOrderTypeEnum = TeacherOrderTypeEnum.SET_QUESTION;
                }
            }
        } catch (Exception e) {
            log.error("CorrectServiceImplV2.convert error,answerCardId:{}, type:{}, error:{}", answerCardId, type, e.getMessage());
            e.printStackTrace();
            if (e instanceof BizException) {
                throw (BizException) e;
            }
        }
        data.put("msg", TeacherOrderTypeEnum.submitContent(teacherOrderTypeEnum, delayStatus));
        return data;
    }


    /**
     * 异步线程处理
     *
     * @param answerCardId
     * @param type
     * @param delayStatus  顺延状态
     * @param modeTypeEnum
     * @return
     */
    private Optional<ErrorResult> convertStep2(final Long answerCardId, final Integer type, Integer delayStatus, UserSession userSession, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {

        if (EssayAnswerCardEnum.TypeEnum.QUESTION.getType() == type.intValue()) {
            EssayQuestionAnswer essayQuestionAnswer = essayQuestionAnswerRepository.findOne(answerCardId);
            if (null == essayQuestionAnswer) {
                return Optional.of(EssayErrors.ANSWER_CARD_ID_ERROR);
            } else {
                //校验是否存在未完成人工答题卡
                List<Long> orderIds = recordService.getConvertOrderIds(answerCardId, EssayAnswerCardEnum.TypeEnum.QUESTION.getType());
                if (CollectionUtils.isNotEmpty(orderIds)) {
                    if (checkExistUnFinishedAnswerId(orderIds) > 0) {
                        return Optional.of(EssayErrors.ERROR_EXIST_RECORRECT_ORDER);
                    }
                }
                //扣减批改次数
                GoodsTypeEnum goodsTypeEnum = EssayCorrectGoodsConstant.GoodsTypeEnum.getGoodsType(essayQuestionAnswer.getQuestionType(), CorrectModeEnum.INTELLIGENCE_2_MANUAL.getMode());
                Long goodsOrderDetailId = UserOrderUtil.reduceUserCorrectTimes(goodsTypeEnum, essayQuestionAnswer.getUserId(), essayUserCorrectGoodsRepository, userCorrectGoodsServiceV4, 0);
                EssayQuestionAnswer newCard = copyQuestionAnswer(essayQuestionAnswer);
                newCard.setSubmitTime(new Date());
                essayQuestionAnswerRepository.save(newCard);
                Long newAnswerId = newCard.getId();
                //保存任务订单
                int correctOrderType = questionTypeService.convertQuestionTypeToQuestionLabelType(essayQuestionAnswer.getQuestionType());
                int delay = delayStatus == DelayStatusEnum.NO.getCode() ? DelayStatusEnum.NO.getCode() : delayStatus;
                CorrectOrder correctOrder = CorrectOrder.builder().answerCardId(newCard.getId())
                        .answerCardType(QuestionTypeConstant.SINGLE_QUESTION).type(correctOrderType)
                        .correctMode(CorrectModeEnum.INTELLIGENCE_2_MANUAL.getMode()).delayStatus(delay)
                        .userId(essayQuestionAnswer.getUserId())
                        .userName(userSession.getUname())
                        .userPhoneNum(userSession.getMobile())
                        .gmtDeadLine(correctOrderService.calculateDeadLine(correctOrderType, delay)).build();
                correctOrder.setGoodsOrderDetailId(goodsOrderDetailId);
                correctOrder.setStatus(YesNoEnum.YES.getValue());
                correctOrderService.createOrder(correctOrder);
                Long newOrderId = correctOrder.getId();
                //保存智能转人工记录
                IntelligenceConvertManualRecord record = IntelligenceConvertManualRecord.builder()
                        .intelligenceAnswerId(answerCardId)
                        .answerType(EssayAnswerCardEnum.TypeEnum.QUESTION.getType())
                        .manualAnswerId(newAnswerId)
                        .orderId(newOrderId).build();
                recordRepository.save(record);
                addCountToCache(answerCardId, EssayAnswerCardEnum.TypeEnum.QUESTION.getType(), newOrderId);
                return Optional.ofNullable(null);
            }
        } else if (EssayAnswerCardEnum.TypeEnum.PAPER.getType() == type.intValue()) {
            EssayPaperAnswer essayPaperAnswer = essayPaperAnswerRepository.findOne(answerCardId);
            if (null == essayPaperAnswer) {
                return Optional.of(EssayErrors.ANSWER_CARD_ID_ERROR);
            } else {
                //校验是否存在未完成人工答题卡
                List<Long> records = recordService.getConvertOrderIds(answerCardId, EssayAnswerCardEnum.TypeEnum.PAPER.getType());
                if (CollectionUtils.isNotEmpty(records)) {
                    if (checkExistUnFinishedAnswerId(records) > 0) {
                        return Optional.of(EssayErrors.ANSWER_CARD_IS_CORRECT_NOW);
                    }
                }
                //扣减批改次数
                GoodsTypeEnum goodsTypeEnum = EssayCorrectGoodsConstant.GoodsTypeEnum.getGoodsType(0, CorrectModeEnum.INTELLIGENCE_2_MANUAL.getMode());
                Long goodsOrderDetailId = UserOrderUtil.reduceUserCorrectTimes(goodsTypeEnum, essayPaperAnswer.getUserId(), essayUserCorrectGoodsRepository, userCorrectGoodsServiceV4, 0);

                EssayPaperAnswer newCard = new EssayPaperAnswer();
                BeanUtils.copyProperties(essayPaperAnswer, newCard);
                newCard.setId(0L);
                newCard.setCorrectMode(CorrectModeEnum.INTELLIGENCE_2_MANUAL.getMode());
                newCard.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());
                newCard.setGmtCreate(new Date());
                newCard.setGmtModify(new Date());
                newCard.setExamScore(0D);
                newCard.setCorrectDate(null);
                newCard.setPdfPath(StringUtils.EMPTY);
                newCard.setPdfSize(StringUtils.EMPTY);
                newCard.setAreaRank(0);
                newCard.setTotalRank(0);
                newCard.setTotalCount(0);
                newCard.setTotalRankChange(0);
                newCard.setMaxScore(0D);
                newCard.setAvgScore(0D);
                newCard.setExamScoreChange(0D);
                newCard.setMaxScoreChange(0D);
                newCard.setSubmitTime(new Date());
                essayPaperAnswerRepository.save(newCard);
                Long newCardId = newCard.getId();

                List<EssayQuestionAnswer> questionAnswers = essayQuestionAnswerRepository.findByPaperAnswerIdAndUserIdAndStatus(answerCardId, essayPaperAnswer.getUserId(),
                        EssayStatusEnum.NORMAL.getCode());
                if (CollectionUtils.isNotEmpty(questionAnswers)) {
                    for (EssayQuestionAnswer questionAnswer : questionAnswers) {
                        EssayQuestionAnswer newQuestionAnswer = copyQuestionAnswer(questionAnswer);
                        newQuestionAnswer.setPaperAnswerId(newCard.getId());
                        essayQuestionAnswerRepository.save(newQuestionAnswer);
                    }
                }
                //保存任务订单
                int correctOrderType = questionTypeService.convertQuestionTypeToQuestionLabelType(0);
                int delay = delayStatus == DelayStatusEnum.NO.getCode() ? DelayStatusEnum.NO.getCode() : delayStatus;
                CorrectOrder correctOrder = CorrectOrder.builder().answerCardId(newCard.getId())
                        .answerCardType(QuestionTypeConstant.PAPER).type(correctOrderType)
                        .correctMode(CorrectModeEnum.INTELLIGENCE_2_MANUAL.getMode()).delayStatus(delay)
                        .userId(essayPaperAnswer.getUserId())
                        .userName(userSession.getUname())
                        .userPhoneNum(userSession.getMobile())
                        .gmtDeadLine(correctOrderService.calculateDeadLine(correctOrderType, delay)).build();
                correctOrder.setGoodsOrderDetailId(goodsOrderDetailId);
                correctOrder.setStatus(YesNoEnum.YES.getValue());
                correctOrderService.createOrder(correctOrder);
                Long newOrderId = correctOrder.getId();
                //保存智能转人工记录
                IntelligenceConvertManualRecord record = IntelligenceConvertManualRecord.builder()
                        .intelligenceAnswerId(answerCardId)
                        .answerType(EssayAnswerCardEnum.TypeEnum.PAPER.getType())
                        .manualAnswerId(newCardId)
                        .orderId(newOrderId).build();
                recordRepository.save(record);
                addCountToCache(answerCardId, EssayAnswerCardEnum.TypeEnum.PAPER.getType(), newOrderId);
                return Optional.ofNullable(null);
            }
        } else {
            return Optional.of(EssayErrors.ANSWER_CARD_CORRECT_MODE_ERROR);
        }

    }

    /**
     * 复制生成智能转人工批改答题卡
     *
     * @param essayQuestionAnswer
     * @return
     */
    private EssayQuestionAnswer copyQuestionAnswer(EssayQuestionAnswer essayQuestionAnswer) {
        EssayQuestionAnswer newCard = new EssayQuestionAnswer();
        BeanUtils.copyProperties(essayQuestionAnswer, newCard);
        newCard.setId(0L);
        newCard.setCorrectMode(CorrectModeEnum.INTELLIGENCE_2_MANUAL.getMode());
        newCard.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());
        newCard.setGmtCreate(new Date());
        newCard.setGmtModify(new Date());
        newCard.setExamScore(0D);
        newCard.setCorrectDate(null);
        newCard.setPdfPath(StringUtils.EMPTY);
        newCard.setFileName(StringUtils.EMPTY);
        newCard.setSubmitTime(new Date());
        return newCard;
    }

    /**
     * 校验是否有未完成的答题卡
     *
     * @param orderIds
     */
    public long checkExistUnFinishedAnswerId(List<Long> orderIds) {
        if (CollectionUtils.isNotEmpty(orderIds)) {
            return correctOrderRepository.countByIdInAndBizStatusIn(orderIds, CorrectOrderStatusEnum.getUnFinishedCorrectStatus());
        }
        return 0;
    }


    /**
     * 智能转人工次数,添加到cache
     *
     * @param answerCardId
     * @param answerType
     * @param newOrderId
     */
    public void addCountToCache(long answerCardId, int answerType, long newOrderId) {
        String key = QuestionReportRedisKeyConstant.getPaperConvertCount(answerType, answerCardId);
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object redisCount = valueOperations.get(key);
        if (null != redisCount) {
            if (redisCount.equals("-1")) {
                redisTemplate.delete(key);
                valueOperations.set(key, String.valueOf(newOrderId));
            } else {
                List<String> collect = Arrays.stream(redisCount.toString().split(",")).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(collect)) {
                    collect.add(String.valueOf(newOrderId));
                    String orderIds = collect.stream().collect(Collectors.joining(","));
                    valueOperations.set(key, orderIds);
                }
            }
            redisTemplate.expire(key, 15, TimeUnit.MINUTES);
        }
    }
}
