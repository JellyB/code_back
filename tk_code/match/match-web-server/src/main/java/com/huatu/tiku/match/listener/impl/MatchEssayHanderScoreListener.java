//package com.huatu.tiku.match.listener.impl;
//
//import com.huatu.tiku.match.listener.ListenerService;
//import com.huatu.tiku.match.service.v1.meta.MatchEssayUserMetaService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections4.MapUtils;
//import org.springframework.amqp.rabbit.annotation.RabbitHandler;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//
///**
// * 申论分数同步
// * Created by huangqingpeng on 2018/12/20.
// */
//@Slf4j
//@Component
//public class MatchEssayHanderScoreListener implements ListenerService {
//    @Autowired
//    private MatchEssayUserMetaService matchEssayUserMetaService;
//
//    @RabbitHandler
//    @Override
//    public void onMessage(Map message) {
//        try {
//            log.info("==========开始互通用户提交答题卡数据===========");
//            log.info("message={}", message);
//            int userId = MapUtils.getInteger(message, "userId");
//            long essayPaperId = MapUtils.getLong(message, "paperId");
//            Double score = MapUtils.getDouble(message, "score");
////            matchEssayUserMetaService.saveEssayScore(essayPaperId,userId,score);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
