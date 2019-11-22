//package com.huatu.tiku.match.listener.impl;
//
//import com.huatu.tiku.match.listener.ListenerService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.rabbit.annotation.RabbitHandler;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//
///**
// * 申论状态队列异步更正
// * Created by huangqingpeng on 2018/10/24.
// */
//@Slf4j
//@Component
//public class EssayStatusListener implements ListenerService {
//
//    @RabbitHandler
//    @Override
//    public void onMessage(Map message) {
//        try {
//            log.info("==========开始更正用户申论考试状态===========");
//            log.info("message={}", message);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
