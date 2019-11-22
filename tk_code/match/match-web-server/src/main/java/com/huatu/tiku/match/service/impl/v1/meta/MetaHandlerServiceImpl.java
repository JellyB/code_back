package com.huatu.tiku.match.service.impl.v1.meta;

import com.google.common.collect.Maps;
import com.huatu.tiku.match.constant.RabbitMatchKeyConstant;
import com.huatu.tiku.match.dto.enroll.EnrollDTO;
import com.huatu.tiku.match.enums.AnswerCardInfoEnum;
import com.huatu.tiku.match.enums.MatchInfoEnum;
import com.huatu.tiku.match.service.impl.v1.paper.AnswerCardUtil;
import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
import com.huatu.tiku.match.service.v1.meta.MetaHandlerService;
import com.huatu.tiku.match.service.v1.paper.AnswerCardDBService;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.PracticeCard;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.paper.bean.UserAnswers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * Created by huangqingpeng on 2019/1/9.
 */
@Slf4j
@Service
public class MetaHandlerServiceImpl implements MetaHandlerService {

    @Autowired
    MatchUserMetaService matchUserMetaService;

    @Autowired
    AnswerCardDBService answerCardDBService;

    @Autowired
    RabbitTemplate rabbitTemplate;


    @Override
    @Async
    public void saveEnrollInfo(EnrollDTO enrollDTO) {
        if (null == enrollDTO) {
            return;
        }
        Long time = null == enrollDTO.getEnrollTime() || enrollDTO.getEnrollTime() <= 0 ? System.currentTimeMillis() : enrollDTO.getEnrollTime();
        Integer schoolId = null == enrollDTO.getSchoolId() || enrollDTO.getSchoolId() <= 0 ? -1 : enrollDTO.getSchoolId();
        String schoolName = StringUtils.isBlank(enrollDTO.getSchoolName()) ? "" : enrollDTO.getSchoolName();
        matchUserMetaService.saveMatchEnrollInfo(enrollDTO.getUserId(), enrollDTO.getPaperId(),
                enrollDTO.getPositionId(),
                schoolId, schoolName,
                time, enrollDTO.getEssayPaperId());
    }



    @Override
    @Async
    public void savePracticeId(long practiceId) {
        AnswerCard answerCard = answerCardDBService.findById(practiceId);
        if (null == answerCard) {
            log.error("savePracticeId error,practiceId = {} is not existed", practiceId);
            return;
        }
        if (answerCard instanceof StandardCard) {
            try {
                matchUserMetaService.savePracticeId(((StandardCard) answerCard).getPaper().getId(), new Long(answerCard.getUserId()).intValue(), practiceId, answerCard.getCardCreateTime());
            } catch (Exception e) {
                log.error("savePracticeId error,practiceId = {}", practiceId);
            }
        }
    }


    @Override
    @Async
    public void saveScore(long practiceId) {
        AnswerCard answerCard = answerCardDBService.findById(practiceId);
        if (null == answerCard) {
            log.error("saveScore error,practiceId = {} is not existed", practiceId);
            return;
        }
        if (answerCard.getStatus() != AnswerCardInfoEnum.Status.FINISH.getCode()) {
            log.error("saveScore error,practiceId = {} is not FINISH", practiceId);
            return;
        }
        if (answerCard instanceof StandardCard) {
            try {
                matchUserMetaService.saveMatchScore(((StandardCard) answerCard).getPaper().getId(),
                        new Long(answerCard.getUserId()).intValue(),
                        MatchInfoEnum.SubmitTypeEnum.MANUAL_SUBMIT,
                        answerCard.getScore(),
                        answerCard.getCreateTime());
            } catch (Exception e) {
                log.error("saveScore error,practiceId = {}", practiceId);
            }
            sendAnswersMsg(answerCard);
            sendUserAnswerCardInfo(answerCard);
        }
    }

    /**
     * 用户交卷试题信息队列消息发送
     * @param answerCard
     */
    private void sendAnswersMsg(AnswerCard answerCard) {
        UserAnswers userAnswersInfo = AnswerCardUtil.getUserAnswersInfo((StandardCard) answerCard);
        rabbitTemplate.convertAndSend(RabbitMatchKeyConstant.SUBMIT_ANSWERS, "", userAnswersInfo);
    }
    /**
     * 大数据消费（用户的答题行为数据）
     *
     * @param answerCard
     */
    private void sendUserAnswerCardInfo(AnswerCard answerCard) {
        int status = answerCard.getStatus();
        if (status != AnswerCardInfoEnum.Status.FINISH.getCode()) {
            return;
        }
        int[] times = answerCard.getTimes();
        int[] corrects = answerCard.getCorrects();
        HashMap<Object, Object> mapData = Maps.newHashMap();
        mapData.put("times", times);
        if (answerCard instanceof StandardCard) {
            List<Integer> ids = ((StandardCard) answerCard).getPaper().getQuestions();
            mapData.put("questions", ids.toArray());
        } else if (answerCard instanceof PracticeCard) {
            List<Integer> ids = ((PracticeCard) answerCard).getPaper().getQuestions();
            mapData.put("questions", ids.toArray());
        }
        mapData.put("corrects", corrects);
        mapData.put("userId", answerCard.getUserId());
        mapData.put("subject", answerCard.getSubject());
        mapData.put("createTime", answerCard.getCreateTime());
        log.info("send answer-card message:{}", JsonUtil.toJson(mapData));
        rabbitTemplate.convertAndSend("answer-card", "", mapData);
    }

}
