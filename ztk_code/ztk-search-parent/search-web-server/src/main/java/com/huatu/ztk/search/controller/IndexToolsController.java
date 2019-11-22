//package com.huatu.ztk.search.controller;
//
//import com.huatu.ztk.question.api.QuestionDubboService;
//import com.huatu.ztk.question.bean.Question;
//import com.huatu.ztk.search.service.QuestionSearchService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by shaojieyue
// * Created time 2016-05-04 14:52
// */
//
//@RestController
//@RequestMapping("/index")
//public class IndexToolsController {
//    private static final Logger logger = LoggerFactory.getLogger(IndexToolsController.class);
//
//    @Autowired
//    private MongoTemplate mongoTemplate;
//    @Autowired
//    private QuestionSearchService questionSearchService;
//
//    @Autowired
//    private QuestionDubboService questionDubboService;
//
//    @RequestMapping(value = "/questions")
//    public void create(@RequestParam int questionId){
//        final Question question = questionDubboService.findById(questionId);
//        questionSearchService.index(question);
//    }
//
//    @RequestMapping(value = "/questions/init")
//    public void init(){
//        int cursor = 0;
//        final int limit = 100;
//        int i = 0;
//        while (true){
//            final List<Question> questions = mongoTemplate.find(new Query().skip(cursor).limit(limit), Question.class, "ztk_question");
//            if (questions.size()<1) {
//                break;
//            }
//            cursor = cursor + limit;
//            for (Question question : questions) {
//                i++;
//                questionSearchService.index(question);
//                if (i % 10000 == 0) {
//                    logger.info("insert question index count={}",i);
//                }
//            }
//        }
//        logger.info("insert question index success. count={}",i);
//    }
//}
