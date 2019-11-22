package com.huatu.ztk.knowledge.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.BaseTest;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.constant.CustomizeEnum;
import com.huatu.ztk.knowledge.service.v1.QuestionPointServiceV1;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shaojieyue
 * Created time 2016-05-18 16:27
 */

@RunWith(SpringJUnit4ClassRunner.class)
public class QuestionPointServiceTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(QuestionPointServiceTest.class);

    @Autowired
    private QuestionPointDubboService questionPointDubboService;
    @Autowired
    private QuestionPointService questionPointService;
    @Autowired
    @Qualifier("questionPointServiceImplV2")
    private QuestionPointServiceV1 questionPointServiceV2;
    @Test
    public void findPointTreeTest() {
        final boolean recursive = true;
        List<QuestionPointTree> pointTree = questionPointDubboService.findPointTree(592, recursive);
        logger.info("data={}", JsonUtil.toJson(pointTree));
        Assert.assertEquals(pointTree.size(), 0);

        pointTree = questionPointDubboService.findPointTree(642, recursive);
        logger.info("data={}", JsonUtil.toJson(pointTree));
        Assert.assertNotNull(pointTree);

        pointTree = questionPointDubboService.findPointTree(404, recursive);
        logger.info("data={}", JsonUtil.toJson(pointTree));
        Assert.assertNotNull(pointTree);

        pointTree = questionPointDubboService.findPointTree(394, false);//400
        logger.info("data={}", JsonUtil.toJson(pointTree));
        Assert.assertNotNull(pointTree);
    }

    @Test
    public void findPointTreeTestCase() throws JsonProcessingException {
        long userId = 233358929;
        int newSubject = 1;
        List<QuestionPointTree> points;

        points = questionPointService.questionPointTree(userId, newSubject, CustomizeEnum.ModeEnum.Write);
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(points));

    }


    /**
     * 构建知识点树
     */
    @Test
    public void buildQuestionTree() {

        QuestionPointTree QuestionPointTree1 = QuestionPointTree.builder().id(1).parent(0).name("一级知识点1")
                .children(new ArrayList<QuestionPointTree>()).build();
        QuestionPointTree QuestionPointTree2 = QuestionPointTree.builder().id(2).parent(0).name("一级知识点2")
                .children(new ArrayList<QuestionPointTree>()).build();
        QuestionPointTree QuestionPointTree10 = QuestionPointTree.builder().id(3).parent(1).name("二级知识点0")
                .children(new ArrayList<QuestionPointTree>()).build();
        QuestionPointTree QuestionPointTree11 = QuestionPointTree.builder().id(4).parent(1).name("二级知识点1")
                .children(new ArrayList<QuestionPointTree>()).build();
        QuestionPointTree QuestionPointTree100 = QuestionPointTree.builder().id(5).parent(2).name("二级知识点")
                .children(new ArrayList<QuestionPointTree>()).build();
        QuestionPointTree QuestionPointTree1000 = QuestionPointTree.builder().id(6).parent(3).name("三级知识点0")
                .children(new ArrayList<QuestionPointTree>()).build();
        QuestionPointTree QuestionPointTree10000 = QuestionPointTree.builder().id(7).parent(6).name("四级知识点0")
                .children(new ArrayList<QuestionPointTree>()).build();

        List<QuestionPointTree> points = new ArrayList<QuestionPointTree>();
        points.add(QuestionPointTree1);
        points.add(QuestionPointTree2);
        points.add(QuestionPointTree10);
        points.add(QuestionPointTree11);
        points.add(QuestionPointTree100);
        points.add(QuestionPointTree1000);
        points.add(QuestionPointTree10000);
        List<QuestionPointTree> rootList = new ArrayList();
        Map<Integer, QuestionPointTree> allMap = new HashMap();
        Map<Integer, QuestionPointTree> rootMap = new HashMap();
        points.forEach(questionPointTree -> {
            allMap.put(questionPointTree.getId(), questionPointTree);
            if (questionPointTree.getParent() == 0) {
                rootMap.put(questionPointTree.getId(), questionPointTree);
            }
        });
        points.forEach(questionPoint -> {
            if (rootMap.get(questionPoint.getId()) != null) {
                rootList.add(questionPoint);
            } else {
                allMap.get(questionPoint.getParent()).getChildren().add(questionPoint);
            }
        });
        System.out.println(rootList);

    }

    @Test
    public void testCountAll(){
        Map<Integer, Integer> integerIntegerMap = questionPointServiceV2.countAll();
        System.out.println(JsonUtil.toJson(integerIntegerMap));
    }
}
