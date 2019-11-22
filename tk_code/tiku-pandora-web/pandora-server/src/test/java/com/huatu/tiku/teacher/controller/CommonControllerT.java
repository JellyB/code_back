package com.huatu.tiku.teacher.controller;

import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.teacher.controller.admin.tag.TagController;
import com.huatu.tiku.teacher.controller.admin.common.CommonController;
import com.huatu.ztk.commons.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by huangqp on 2018\7\14 0014.
 */
@Slf4j
public class CommonControllerT extends TikuBaseTest{
    @Autowired
    CommonController commonController;
    @Autowired
    TagController tagController;
    private final static Long subject = 1L;
    @Test
    public void getQuestionType(){
        Object questionType = commonController.getQuestionType(subject);
        log.info("subject={},questionType={}",subject, JsonUtil.toJson(questionType));
    }
    @Test
    public void getKnowledgeTreeBySubject(){
        Object knowledgeTreeBySubject = commonController.getKnowledgeTreeBySubject(subject);
        log.info("subject={},knowledgeTreeBySubject={}",subject, JsonUtil.toJson(knowledgeTreeBySubject));
    }
    @Test
    public void getModeType(){
        Object modeType = commonController.getModeType();
        log.info("subject={},modeType={}",subject, JsonUtil.toJson(modeType));
    }
    @Test
    public void getDifficult(){
        Object difficult = commonController.getDifficult();
        log.info("subject={},difficult={}",subject, JsonUtil.toJson(difficult));
    }
    @Test
    public void getAreaList(){
        Object areaList = commonController.getAreaList(subject);
        log.info("subject={},areaList={}",subject, JsonUtil.toJson(areaList));
    }
    @Test
    public void getQuestionStatus(){
        Object questionStatus = commonController.getQuestionStatus();
        log.info("subject={},questionStatus={}",subject, JsonUtil.toJson(questionStatus));
    }
    @Test
    public void getSubjectCount(){
        Object subjectCount = commonController.getSubjectCount();
        log.info("subject={},subjectTree={}",subject, JsonUtil.toJson(subjectCount));
    }

    @Test
    public void tagControllerAll(){
        Object tagControllerAll = tagController.findAll(-1, "0", 1,100);
        log.info("subject={},tagControllerAll={}",subject, JsonUtil.toJson(tagControllerAll));
    }
    @Test
    public void testTotal(){
        getQuestionType();
        getKnowledgeTreeBySubject();
        getModeType();
        getDifficult();
        getAreaList();
        getQuestionStatus();
        getSubjectCount();
        tagControllerAll();
    }
}

