package com.huatu.tiku.match.listener.impl;

import com.huatu.tiku.match.dao.document.AnswerCardDao;
import com.huatu.tiku.match.listener.ListenerService;
import com.huatu.tiku.match.service.v1.meta.MatchQuestionMetaService;
import com.huatu.ztk.paper.bean.AnswerCard;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class MatchQuestionMetaSyncListener implements ListenerService {

    @Autowired
    AnswerCardDao answerCardDao;
    @Autowired
    MatchQuestionMetaService matchQuestionMetaService;

    @Override
    public void onMessage(Map message) {
        try{
            Long practiceId = MapUtils.getLong(message, "practiceId");
            log.info("sync matchQuestionMeta info ,practiceId={}",practiceId);
            AnswerCard answerCard = answerCardDao.findById(practiceId);
            matchQuestionMetaService.handlerQuestionMeta(answerCard);
        }catch (Exception e){
            log.error("MatchQuestionMetaSyncListener handler message error,message={}",message);
            e.printStackTrace();
        }

    }
}
