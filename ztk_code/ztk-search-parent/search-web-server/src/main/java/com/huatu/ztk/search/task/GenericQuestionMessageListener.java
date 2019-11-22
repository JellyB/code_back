//package com.huatu.ztk.search.task;
//
//import com.google.common.primitives.Ints;
//import com.huatu.ztk.commons.JsonUtil;
//import com.huatu.ztk.question.api.QuestionDubboService;
//import com.huatu.ztk.question.bean.Question;
//import com.huatu.ztk.search.service.QuestionSearchService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.core.MessageListener;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.Map;
//
///**
// * 普通试题监控
// * Created by shaojieyue
// * Created time 2016-05-17 11:20
// */
//public class GenericQuestionMessageListener implements MessageListener {
//    private static final Logger logger = LoggerFactory.getLogger(GenericQuestionMessageListener.class);
//
//    @Autowired
//    private QuestionDubboService questionDubboService;
//
//    @Autowired
//    private QuestionSearchService questionSearchService;
//
//    @Override
//    public void onMessage(Message message) {
//        String text = new String(message.getBody());
//        logger.info("receive message={}",text);
//        try {
//            final Map data = JsonUtil.toMap(text);
//            final Integer questionId = Ints.tryParse(data.get("qid").toString());
//            final Question question = questionDubboService.findById(questionId);
//
//            if (question == null) {
//                logger.error("qid={} not exist.",questionId);
//                return;
//            }
//
//            questionSearchService.index(question);
//        }catch (Exception e){
//            logger.error("ex",e);
//        }
//    }
//}
