//package com.huatu.tiku.match.listener.impl;
//
//import com.huatu.tiku.match.listener.ListenerService;
//import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections4.MapUtils;
//import org.apache.commons.lang.StringUtils;
//import org.springframework.amqp.rabbit.annotation.RabbitHandler;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//
///**
// * Created by huangqingpeng on 2018/10/25.
// */
//@Slf4j
//@Component
//public class MatchEnrollInfoListener implements ListenerService {
//
//    @Autowired
//    private MatchUserMetaService matchUserMetaService;
//
//    @RabbitHandler
//    @Override
//    @Deprecated
//    public void onMessage(Map message) {
//        try {
//            log.info("==========开始互通用户报名数据===========");
//            log.info("message={}", message);
//            int userId = MapUtils.getInteger(message, "userId");
//            int paperId = MapUtils.getInteger(message, "paperId");
//            int positionId = MapUtils.getInteger(message, "positionId");
//            int schoolId = MapUtils.getInteger(message, "schoolId",-1);
//            String schoolName = MapUtils.getString(message, "schoolName", StringUtils.EMPTY);
//            Long enrollTime = MapUtils.getLong(message, "enrollTime");
////            matchUserMetaService.saveMatchEnrollInfo(userId, paperId, positionId, schoolId, schoolName,enrollTime);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
