package com.huatu.ztk.paper.task;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.common.AnswerCardStatus;
import com.huatu.ztk.paper.dao.AnswerCardDao;
import com.huatu.ztk.paper.dao.PaperDao;
import com.huatu.ztk.paper.service.PaperAnswerCardService;
import com.huatu.ztk.paper.service.v4.impl.ComputeScoreUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author huangqingpeng
 * @title: ReCountAnswerCardScoreTask
 * @description: TODO
 * @date 2019-08-1615:15
 */
public class ReCountAnswerCardScoreTask implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(ReCountAnswerCardScoreTask.class);

    @Autowired
    private AnswerCardDao answerCardDao;
    @Autowired
    private PaperDao paperDao;

    @Autowired
    PaperAnswerCardService paperAnswerCardService;

    @Override
    public void onMessage(Message message) {
        String text = new String(message.getBody());
        logger.info("receive message={}", text);
        if (StringUtils.isBlank(text)) {
            return;
        }
        Consumer<String> errorInfo = (str -> {
            logger.error("ReCountAnswerCardScoreTask: submit error,{}", str);
        });
        try {
            Map map = JsonUtil.toMap(text);
            long id = MapUtils.getLong(map, "id", -1L);
            if (id < 0) {
                errorInfo.accept(text);
                return;
            }
            AnswerCard answerCard = answerCardDao.findById(id);
            if (null == answerCard || answerCard.getStatus() != AnswerCardStatus.FINISH) {
                errorInfo.accept(text + "， null == answerCard || answerCard.getStatus() != AnswerCardStatus.FINISH");
                return;
            }
            if (answerCard instanceof StandardCard) {
                Paper paper = ((StandardCard) answerCard).getPaper();   //创建答题卡阶段试卷信息
                int paperId = paper.getId();
                Paper paperInfo = paperDao.findById(paperId);       //现阶段试卷信息
                if (null == paperInfo) {        //现阶段试卷不存在，不做处理
                    errorInfo.accept(text + ", null == paperInfo");
                    return;
                }
                boolean isCommonQuestion = true;
                for (int i = 0; i < paper.getQcount(); i++) {
                    if (!paper.getQuestions().get(i).equals(paperInfo.getQuestions().get(i))) {
                        isCommonQuestion = false;
                        logger.error("questionId is different : " + paper.getQuestions().get(i) + "!=" + paperInfo.getQuestions().get(i));
                        break;
                    }
                }
                if (!isCommonQuestion) {
                    errorInfo.accept(text + ", has different questions.");
                    return;
                }
                if (paper.getScore() == paperInfo.getScore() &&
                        null != paper.getScoreFlag() || null != paperInfo.getScoreFlag() &&
                        paper.getScoreFlag().equals(paperInfo.getScoreFlag())) {
                    errorInfo.accept(text + "算分方式一致，无需重新算分");
                    return;
                }
                ((StandardCard) answerCard).setPaper(paperInfo);
                //答题卡分数设定
                answerCard.setScore(ComputeScoreUtil.computeScore(answerCard));
                answerCardDao.save(answerCard);

                paperAnswerCardService.setCardMeta(answerCard);
            }


        } catch (Exception e) {
            logger.error("ReCountAnswerCardScoreTask: submit error,{}", text);
            e.printStackTrace();
        }
    }
}
