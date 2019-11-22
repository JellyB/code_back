package com.huatu.tiku.essay.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.huatu.tiku.essay.constant.status.QuestionTypeConstant;
import com.huatu.tiku.essay.entity.*;
import com.huatu.tiku.essay.entity.courseExercises.EssayExercisesAnswerMeta;
import com.huatu.tiku.essay.essayEnum.CourseWareTypeEnum;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.repository.*;
import com.huatu.tiku.essay.repository.courseExercises.EssayExercisesAnswerMetaRepository;
import com.huatu.tiku.essay.service.CorrectPushService;
import com.huatu.tiku.push.constant.CorrectCourseWorkPushInfo;
import com.huatu.tiku.push.constant.CorrectReportInfo;
import com.huatu.tiku.push.constant.CorrectReturnInfo;
import com.huatu.tiku.push.constant.RabbitMqKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-08-02 4:13 PM
 **/

@Slf4j
@Service
public class CorrectPushServiceImpl implements CorrectPushService{

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private EssayPaperAnswerRepository essayPaperAnswerRepository;

    @Autowired
    private EssayQuestionAnswerRepository essayQuestionAnswerRepository;

    @Autowired
    private EssayQuestionDetailRepository essayQuestionDetailRepository;

    @Autowired
    private EssayExercisesAnswerMetaRepository essayExercisesAnswerMetaRepository;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 答题卡被退回
     *
     * @param answerCard
     * @param answerCardType  1 普通答题卡 2 课后作业答题卡
     * @param returnContent
     * @param exercisesType
     */
    @Override
    public void correctReturn4Push(Object answerCard, Integer answerCardType, Integer exercisesType, String returnContent) {
        EssayQuestionAnswer essayQuestionAnswer = null;
        EssayPaperAnswer essayPaperAnswer = null;
        if(answerCardType == EssayAnswerCardEnum.TypeEnum.PAPER.getType()){
            essayPaperAnswer = (EssayPaperAnswer) answerCard;
            if(null == essayPaperAnswer){
                log.error("申论人工批改，paper 答题卡获取不到:{}", JSONObject.toJSONString(answerCard));
                return;
            }
        }else{
            essayQuestionAnswer = (EssayQuestionAnswer) answerCard;
            if(null == essayQuestionAnswer){
                log.error("申论人工批改，question 答题卡获取不到:{}", JSONObject.toJSONString(answerCard));
                return;
            }
        }
        //普通答题卡
        if(exercisesType == EssayAnswerCardEnum.ModeTypeEnum.NORMAL.getType()){
            CorrectReturnInfo correctReturnInfo = new CorrectReturnInfo();
            correctReturnInfo.setReturnContent(returnContent);
            correctReturnInfo.setDealDate(new Date());
            if(answerCard instanceof EssayQuestionAnswer){
                correctReturnInfo.setBizId(Long.parseLong(essayQuestionAnswer.getId() + "" + EssayAnswerCardEnum.TypeEnum.QUESTION.getType() + "" + System.currentTimeMillis() % 1000));
                correctReturnInfo.setUserId(essayQuestionAnswer.getUserId());
                correctReturnInfo.setSubmitTime(essayQuestionAnswer.getSubmitTime());
                correctReturnInfo.setAnswerCardId(essayQuestionAnswer.getId());
                correctReturnInfo.setTopicType(essayQuestionAnswer.getQuestionType() == 5 ? QuestionTypeConstant.ARGUMENTATION : QuestionTypeConstant.SINGLE_QUESTION);
                String message = JSONObject.toJSONString(correctReturnInfo);
                rabbitTemplate.convertAndSend("", RabbitMqKey.NOTICE_CORRECT_RETURN, message);
            }

            if(answerCard instanceof EssayPaperAnswer){
                correctReturnInfo.setBizId(Long.parseLong(essayPaperAnswer.getId() + "" + EssayAnswerCardEnum.TypeEnum.PAPER.getType() + "" + System.currentTimeMillis() % 1000));
                correctReturnInfo.setUserId(essayPaperAnswer.getUserId());
                correctReturnInfo.setSubmitTime(essayPaperAnswer.getSubmitTime());
                correctReturnInfo.setAnswerCardId(essayPaperAnswer.getId());
                correctReturnInfo.setTopicType(QuestionTypeConstant.PAPER);
                String message = JSONObject.toJSONString(correctReturnInfo);
                rabbitTemplate.convertAndSend("", RabbitMqKey.NOTICE_CORRECT_RETURN, message);
            }
        }
        //课后作业答题卡
        if(exercisesType == EssayAnswerCardEnum.ModeTypeEnum.COURSE.getType()){
            try{
                CorrectCourseWorkPushInfo correctCourseWorkPushInfo = CorrectCourseWorkPushInfo.builder().build();
                EssayExercisesAnswerMeta essayExercisesAnswerMeta = null;
                if(answerCard instanceof EssayQuestionAnswer){
                    EssayQuestionDetail essayQuestionDetail = essayQuestionDetailRepository.findById(essayQuestionAnswer.getQuestionDetailId());
                    List<EssayExercisesAnswerMeta> list = essayExercisesAnswerMetaRepository.findByAnswerIdAndAnswerTypeAndStatus(essayQuestionAnswer.getId(), EssayAnswerCardEnum.TypeEnum.QUESTION.getType(), EssayStatusEnum.NORMAL.getCode());
                    if(CollectionUtils.isEmpty(list)){
                        log.error("课后作业被退回，查询不到meta信息:{}", JSONObject.toJSONString(answerCard));
                        return;
                    }
                    essayExercisesAnswerMeta = list.get(0);
                    correctCourseWorkPushInfo.setBizId(Long.parseLong(essayExercisesAnswerMeta.getAnswerId() + "" + EssayAnswerCardEnum.TypeEnum.QUESTION.getType() + "" + System.currentTimeMillis() % 1000));
                    correctCourseWorkPushInfo.setStem(essayQuestionDetail.getStem());
                    correctCourseWorkPushInfo.setSubmitTime(essayQuestionAnswer.getSubmitTime());
                }

                if(answerCard instanceof EssayPaperAnswer){
                    List<EssayExercisesAnswerMeta> list = essayExercisesAnswerMetaRepository.findByAnswerIdAndAnswerTypeAndStatus(essayPaperAnswer.getId(), EssayAnswerCardEnum.TypeEnum.PAPER.getType(), EssayStatusEnum.NORMAL.getCode());
                    if(CollectionUtils.isEmpty(list)){
                        log.error("课后作业被退回，查询不到meta信息:{}", JSONObject.toJSONString(answerCard));
                        return;
                    }
                    essayExercisesAnswerMeta = list.get(0);
                    correctCourseWorkPushInfo.setBizId(Long.parseLong(essayExercisesAnswerMeta.getAnswerId() + "" + EssayAnswerCardEnum.TypeEnum.PAPER.getType() + "" + System.currentTimeMillis() % 1000));
                    correctCourseWorkPushInfo.setStem(essayPaperAnswer.getName());
                    correctCourseWorkPushInfo.setSubmitTime(essayPaperAnswer.getSubmitTime());
                }
                correctCourseWorkPushInfo.setReturnContent(returnContent);
                correctCourseWorkPushInfo.setType(CorrectCourseWorkPushInfo.RETURN);
                correctCourseWorkPushInfo.setNetClassId(essayExercisesAnswerMeta.getCourseId().longValue());
                correctCourseWorkPushInfo.setSyllabusId(essayExercisesAnswerMeta.getSyllabusId());
                correctCourseWorkPushInfo.setUserId(essayExercisesAnswerMeta.getUserId());
                correctCourseWorkPushInfo.setLessonId(essayExercisesAnswerMeta.getCourseWareId());
                correctCourseWorkPushInfo.setIsLive(essayExercisesAnswerMeta.getCourseType() == CourseWareTypeEnum.TableCourseTypeEnum.LIVE.getType() ? CorrectCourseWorkPushInfo.IS_LIVE : CorrectCourseWorkPushInfo.IS_NOT_LIVE);
                log.info("申论课后作业消息推送msg:{}", JSONObject.toJSONString(correctCourseWorkPushInfo));
                rabbitTemplate.convertAndSend("", RabbitMqKey.NOTICE_CORRECT_COURSE_WORK, JSONObject.toJSONString(correctCourseWorkPushInfo));
            }catch (Exception e){
                log.error("申论课后作业退回异常:cardInfo:{},cardType:{},returnContent:{},errorMsg:{}",JSONObject.toJSONString(answerCard), answerCardType,returnContent, e.getMessage());
            }
        }
    }

    /**
     * 答题卡被退回
     * @param answerCardId
     * @param answerCardType
     * @param returnContent
     */
    @Deprecated
    @Override
    public void correctReturn4Push(final long answerCardId, final int answerCardType, final String returnContent) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try{
            CorrectReturnInfo correctReturnInfo = new CorrectReturnInfo();
            correctReturnInfo.setReturnContent(returnContent);
            correctReturnInfo.setDealDate(new Date());
            correctReturnInfo.setBizId(Long.parseLong(answerCardId + "" + answerCardType + "" + System.currentTimeMillis() % 1000));
            if(answerCardType == EssayAnswerCardEnum.TypeEnum.PAPER.getType()){
                EssayPaperAnswer essayPaperAnswer = essayPaperAnswerRepository.findByIdAndStatus(answerCardId, EssayStatusEnum.NORMAL.getCode());
                if(null == essayPaperAnswer){
                    log.error("批改被退回试卷答题卡查询不到:{}", answerCardId);
                    return;
                }
                //课后作业答题卡不发送 push
                if(essayPaperAnswer.getAnswerCardType() == EssayAnswerCardEnum.ModeTypeEnum.COURSE.getType()){
                    return;
                }
                correctReturnInfo.setUserId(essayPaperAnswer.getUserId());
                correctReturnInfo.setSubmitTime(essayPaperAnswer.getSubmitTime());
                correctReturnInfo.setAnswerCardId(essayPaperAnswer.getId());
                correctReturnInfo.setTopicType(QuestionTypeConstant.PAPER);
            }else{
                EssayQuestionAnswer essayQuestionAnswer = essayQuestionAnswerRepository.findByIdAndStatus(answerCardId, EssayStatusEnum.NORMAL.getCode());
                if(null == essayQuestionAnswer){
                    log.error("批改被退回单题答题卡查询不到:{}", answerCardId);
                    return;
                }
                //课后作业答题卡不发送 push
                if(essayQuestionAnswer.getAnswerCardType() == EssayAnswerCardEnum.ModeTypeEnum.COURSE.getType()){
                    return;
                }
                    correctReturnInfo.setUserId(essayQuestionAnswer.getUserId());
                    correctReturnInfo.setSubmitTime(essayQuestionAnswer.getSubmitTime());
                    correctReturnInfo.setAnswerCardId(essayQuestionAnswer.getId());
                    correctReturnInfo.setTopicType(essayQuestionAnswer.getQuestionType() == 5 ? QuestionTypeConstant.ARGUMENTATION : QuestionTypeConstant.SINGLE_QUESTION);
            }
                    String message = JSONObject.toJSONString(correctReturnInfo);
                    rabbitTemplate.convertAndSend("", RabbitMqKey.NOTICE_CORRECT_RETURN, message);
        }catch (Exception e){
            e.printStackTrace();
            log.error("人工批改退回异常:{}", answerCardId);
        }finally {
            executorService.shutdown();
        }
    }

    /**
     * 生成报告
     * @param answerCardId
     * @param answerCardType
     */
    @Override
    public void correctReport4Push(final long answerCardId, final int answerCardType) {
        log.info("人工批改 - 报告已出 入口 answerCardId:{}, answerCardType:{}", answerCardId, answerCardType);
        String key = "correct.report:%s_%s";

        String key_ = String.format(key, answerCardId, answerCardType);
        if(redisTemplate.hasKey(key_)){
            log.debug("申论人工批改报告已经推送:id:{}, type:{}", answerCardId, answerCardType);
            return;
        }
        try{
            if(answerCardType == EssayAnswerCardEnum.TypeEnum.PAPER.getType()){
                EssayPaperAnswer essayPaperAnswer = essayPaperAnswerRepository.findByIdAndStatus(answerCardId, EssayStatusEnum.NORMAL.getCode());
                if(null == essayPaperAnswer){
                    log.error("批改已生成报告试卷答题卡查询不到:{}", answerCardId);
                    return;
                }
                log.debug("人工批改 - 报告已出 入口 试卷:{}", JSONObject.toJSONString(essayPaperAnswer));
                // 普通答题卡
                if(essayPaperAnswer.getAnswerCardType() == EssayAnswerCardEnum.ModeTypeEnum.NORMAL.getType()){
                    CorrectReportInfo correctReportInfo = new CorrectReportInfo();
                    correctReportInfo.setDealDate(new Date());
                    correctReportInfo.setBizId(Long.parseLong(answerCardId + "" + answerCardType + "" + System.currentTimeMillis() % 1000));
                    correctReportInfo.setUserId(essayPaperAnswer.getUserId());
                    correctReportInfo.setSubmitTime(essayPaperAnswer.getSubmitTime() == null ? new Date() : essayPaperAnswer.getSubmitTime());
                    correctReportInfo.setAnswerCardId(essayPaperAnswer.getId());
                    correctReportInfo.setTopicType(QuestionTypeConstant.PAPER);
                    correctReportInfo.setQuestionName("套题");
                    correctReportInfo.setAreaName(essayPaperAnswer.getAreaName());
                    correctReportInfo.setPaperName(essayPaperAnswer.getName());
                    correctReportInfo.setPaperId(essayPaperAnswer.getPaperBaseId());
                    String message = JSONObject.toJSONString(correctReportInfo);
                    rabbitTemplate.convertAndSend("", RabbitMqKey.NOTICE_CORRECT_REPORT, message);
                    ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
                    valueOperations.set(key_, String.valueOf( System.currentTimeMillis()));
                    redisTemplate.expire(key_, 7, TimeUnit.DAYS);
                }
                // 课后作业代替卡
                if(essayPaperAnswer.getAnswerCardType() == EssayAnswerCardEnum.ModeTypeEnum.COURSE.getType()){
                    try{
                        CorrectCourseWorkPushInfo correctCourseWorkPushInfo = CorrectCourseWorkPushInfo.builder().build();
                        List<EssayExercisesAnswerMeta> list = essayExercisesAnswerMetaRepository.findByAnswerIdAndAnswerTypeAndStatus(essayPaperAnswer.getId(), EssayAnswerCardEnum.TypeEnum.PAPER.getType(), EssayStatusEnum.NORMAL.getCode());
                        if(CollectionUtils.isEmpty(list)){
                            log.error("课后作业被退回，查询不到meta信息:cardId:{}, cardType:{}", answerCardId, answerCardType);
                            return;
                        }
                        EssayExercisesAnswerMeta essayExercisesAnswerMeta = list.get(0);
                        correctCourseWorkPushInfo.setType(CorrectCourseWorkPushInfo.REPORT);
                        correctCourseWorkPushInfo.setSubmitTime(essayPaperAnswer.getSubmitTime());
                        correctCourseWorkPushInfo.setStem(essayPaperAnswer.getName());
                        correctCourseWorkPushInfo.setBizId(Long.parseLong(essayExercisesAnswerMeta.getAnswerId() + "" + EssayAnswerCardEnum.TypeEnum.PAPER.getType() + "" + System.currentTimeMillis() % 1000));
                        correctCourseWorkPushInfo.setNetClassId(essayExercisesAnswerMeta.getCourseId().longValue());
                        correctCourseWorkPushInfo.setSyllabusId(essayExercisesAnswerMeta.getSyllabusId());
                        correctCourseWorkPushInfo.setUserId(essayExercisesAnswerMeta.getUserId());
                        correctCourseWorkPushInfo.setLessonId(essayExercisesAnswerMeta.getCourseWareId());
                        correctCourseWorkPushInfo.setIsLive(essayExercisesAnswerMeta.getCourseType() == CourseWareTypeEnum.TableCourseTypeEnum.LIVE.getType() ? CorrectCourseWorkPushInfo.IS_LIVE : CorrectCourseWorkPushInfo.IS_NOT_LIVE);
                        rabbitTemplate.convertAndSend("", RabbitMqKey.NOTICE_CORRECT_COURSE_WORK, JSONObject.toJSONString(correctCourseWorkPushInfo));
                        log.info("申论课后作业paper出报告msg:{}", JSONObject.toJSONString(correctCourseWorkPushInfo));
                    }catch (Exception e){
                        log.error("申论课后作业paper出报告异常:cardId:{},cardType:{},errorMsg:{}", answerCardId, answerCardType, e.getMessage());
                    }
                }
            }else{
                EssayQuestionAnswer essayQuestionAnswer = essayQuestionAnswerRepository.findByIdAndStatus(answerCardId, EssayStatusEnum.NORMAL.getCode());
                if(null == essayQuestionAnswer){
                    log.error("批改已生成报告单题答题卡查询不到:{}", answerCardId);
                    return;
                }
                log.debug("人工批改 - 报告已出 入口 单题:{}", JSONObject.toJSONString(essayQuestionAnswer));
                EssayQuestionDetail essayQuestionDetail = essayQuestionDetailRepository.findById(essayQuestionAnswer.getQuestionDetailId());
                //普通答题卡
                if(essayQuestionAnswer.getAnswerCardType() == EssayAnswerCardEnum.ModeTypeEnum.NORMAL.getType()){
                    CorrectReportInfo correctReportInfo = new CorrectReportInfo();
                    correctReportInfo.setDealDate(new Date());
                    correctReportInfo.setBizId(Long.parseLong(answerCardId + "" + answerCardType + "" + System.currentTimeMillis() % 1000));
                    correctReportInfo.setUserId(essayQuestionAnswer.getUserId());
                    correctReportInfo.setSubmitTime(essayQuestionAnswer.getSubmitTime() == null ? new Date() : essayQuestionAnswer.getSubmitTime());
                    correctReportInfo.setAnswerCardId(essayQuestionAnswer.getId());
                    correctReportInfo.setTopicType(essayQuestionAnswer.getQuestionType() == 5 ? QuestionTypeConstant.ARGUMENTATION : QuestionTypeConstant.SINGLE_QUESTION);
                    correctReportInfo.setQuestionName(essayQuestionAnswer.getQuestionType() == 5 ? "文章写作" : "标准答案");
                    correctReportInfo.setAreaName(essayQuestionAnswer.getAreaName());
                    correctReportInfo.setQuestionName(essayQuestionDetail.getStem());
                    correctReportInfo.setQuestionBaseId(essayQuestionAnswer.getQuestionBaseId());
                    String message = JSONObject.toJSONString(correctReportInfo);
                    rabbitTemplate.convertAndSend("", RabbitMqKey.NOTICE_CORRECT_REPORT, message);
                    ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
                    valueOperations.set(key_, String.valueOf( System.currentTimeMillis()));
                    redisTemplate.expire(key_, 7, TimeUnit.DAYS);
                }
                // 课后作业代替卡
                if(essayQuestionAnswer.getAnswerCardType() == EssayAnswerCardEnum.ModeTypeEnum.COURSE.getType()){
                    try{
                        CorrectCourseWorkPushInfo correctCourseWorkPushInfo = CorrectCourseWorkPushInfo.builder().build();
                        List<EssayExercisesAnswerMeta> list = essayExercisesAnswerMetaRepository.findByAnswerIdAndAnswerTypeAndStatus(answerCardId, EssayAnswerCardEnum.TypeEnum.QUESTION.getType(), EssayStatusEnum.NORMAL.getCode());
                        if(CollectionUtils.isEmpty(list)){
                            log.error("课后作业被退回，查询不到meta信息:cardId:{}, cardType:{}", answerCardId, answerCardType);
                            return;
                        }
                        EssayExercisesAnswerMeta essayExercisesAnswerMeta = list.get(0);
                        correctCourseWorkPushInfo.setType(CorrectCourseWorkPushInfo.REPORT);
                        correctCourseWorkPushInfo.setStem(essayQuestionDetail.getStem());
                        correctCourseWorkPushInfo.setSubmitTime(essayQuestionAnswer.getSubmitTime());
                        correctCourseWorkPushInfo.setBizId(Long.parseLong(essayExercisesAnswerMeta.getAnswerId() + "" + EssayAnswerCardEnum.TypeEnum.QUESTION.getType() + "" + System.currentTimeMillis() % 1000));
                        correctCourseWorkPushInfo.setNetClassId(essayExercisesAnswerMeta.getCourseId().longValue());
                        correctCourseWorkPushInfo.setSyllabusId(essayExercisesAnswerMeta.getSyllabusId());
                        correctCourseWorkPushInfo.setUserId(essayExercisesAnswerMeta.getUserId());
                        correctCourseWorkPushInfo.setLessonId(essayExercisesAnswerMeta.getCourseWareId());
                        correctCourseWorkPushInfo.setIsLive(essayExercisesAnswerMeta.getCourseType() == CourseWareTypeEnum.TableCourseTypeEnum.LIVE.getType() ? CorrectCourseWorkPushInfo.IS_LIVE : CorrectCourseWorkPushInfo.IS_NOT_LIVE);
                        rabbitTemplate.convertAndSend("", RabbitMqKey.NOTICE_CORRECT_COURSE_WORK, JSONObject.toJSONString(correctCourseWorkPushInfo));
                        log.info("申论课后作业question出报告msg:{}", JSONObject.toJSONString(correctCourseWorkPushInfo));
                    }catch (Exception e){
                        log.error("申论课后作业question出报告异常:cardId:{},cardType:{},errorMsg:{}", answerCardId, answerCardType, e.getMessage());
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("申论人工批改生成报告异常: id:{}, type:{}", answerCardId, answerCardType);
        }
    }
}
