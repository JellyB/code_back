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
// * 申论报名数据同步过来
// * Created by huangqingpeng on 2018/10/26.
// */
//@Slf4j
//@Component
//public class MatchEssayEnrollInfoListener implements ListenerService {
//
//    @Autowired
//    private MatchEssayUserMetaService matchEssayUserMetaService;
//
//    @RabbitHandler
//    @Override
//    public void onMessage(Map message) {
//        try {
//            log.info("==========开始互通用户申论报名数据===========");
//            log.info("message={}", message);
//            int userId = MapUtils.getInteger(message, "userId");
//            int paperId = MapUtils.getInteger(message, "paperId");
//            int positionId = MapUtils.getInteger(message, "positionId");
//            Long enrollTime = MapUtils.getLong(message, "enrollTime");
//            matchEssayUserMetaService.saveMatchEnrollInfo(userId, paperId, positionId, enrollTime);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
