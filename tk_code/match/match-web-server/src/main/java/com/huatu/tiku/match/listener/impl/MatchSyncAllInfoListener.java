//package com.huatu.tiku.match.listener.impl;
//
//import com.huatu.tiku.match.listener.ListenerService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections.MapUtils;
//import org.springframework.amqp.rabbit.annotation.RabbitHandler;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//
///**
// * 申论整体数据同步
// * Created by huangqingpeng on 2018/12/27.
// */
//@Slf4j
//@Component
//public class MatchSyncAllInfoListener implements ListenerService {
//
//    @RabbitHandler
//    @Override
//    public void onMessage(Map message) {
//        try{
//            log.info("matchSyncAllInfo's message = {}",message);
//            Object userMeta = MapUtils.getObject(message, "userMeta");
////            if(userMeta instanceof Essay){
////
////            }
//            Object answerCard = MapUtils.getObject(message, "answerCard");
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//}
