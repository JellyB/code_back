package com.huatu.ztk.knowledge.service;

import com.huatu.ztk.knowledge.common.DatacleanConfig;
import com.huatu.ztk.knowledge.service.v1.QuestionErrorServiceV1;
import com.huatu.ztk.knowledge.service.v1.QuestionFinishServiceV1;
import com.huatu.ztk.knowledge.service.v1.QuestionPointServiceV1;
import com.huatu.ztk.knowledge.servicePandora.KnowledgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.awt.image.PixelGrabber;

@Service
public class PoxyUtilService {

    private static final Logger logger = LoggerFactory.getLogger(PoxyUtilService.class);
    @Autowired
    private DatacleanConfig datacleanConfig;
    @Autowired
    @Qualifier("questionPointServiceImplV1")
    private QuestionPointServiceV1 questionPointServiceV1;
    @Autowired
    @Qualifier("questionPointServiceImplV2")
    private QuestionPointServiceV1 questionPointServiceV2;

    public QuestionPointServiceV1 getQuestionPointService() {
        return getQuestionPointService(datacleanConfig.getServiceFlag());
    }

    public QuestionPointServiceV1 getQuestionPointService(int flag) {
//        switch (flag){
//            case 1:
//                return questionPointServiceV1;
//            case 2:
//                return questionPointServiceV2;
//        }
//        return questionPointServiceV1;
        return questionPointServiceV2;
    }

    @Autowired
    @Qualifier("questionErrorServiceImplV1")
    private QuestionErrorServiceV1 questionErrorServiceV1;

    @Autowired
    @Qualifier("questionErrorServiceImplV2")
    private QuestionErrorServiceV1 questionErrorServiceV2;

    public QuestionErrorServiceV1 getQuestionErrorService() {
        return getQuestionErrorService(datacleanConfig.getServiceFlag());
    }

    public QuestionErrorServiceV1 getQuestionErrorService(int flag) {
        switch (flag){
            case 1:
                return questionErrorServiceV1;
            case 2:
                return questionErrorServiceV2;
        }
        return questionErrorServiceV1;
    }

    @Autowired
    @Qualifier("questionFinishServiceImplV1")
    private QuestionFinishServiceV1 questionFinishServiceV1;

    @Autowired
    @Qualifier("questionFinishServiceImplV2")
    private QuestionFinishServiceV1 questionFinishServiceV2;

    public QuestionFinishServiceV1 getQuestionFinishService() {
        return getQuestionFinishService(datacleanConfig.getServiceFlag());
    }

    public QuestionFinishServiceV1 getQuestionFinishService(int flag) {
        switch (flag){
            case 1:
                return questionFinishServiceV1;
            case 2:
                return questionFinishServiceV2;
        }
        return questionFinishServiceV1;
    }

    @Autowired
    @Qualifier("knowledgeServiceImpl")
    private KnowledgeService knowledgeServiceV1;
    @Autowired
    @Qualifier("knowledgeServiceImplV2")
    private KnowledgeService knowledgeServiceV2;
    public KnowledgeService getKnowledgeService() {
        return getKnowledgeService(datacleanConfig.getServiceFlag());
    }

    public KnowledgeService getKnowledgeService(int flag) {
        switch (flag){
            case 1:
                return knowledgeServiceV1;
            case 2:
                return knowledgeServiceV2;
        }
        return knowledgeServiceV1;
    }
}
