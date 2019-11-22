package com.huatu.ztk.knowledge.service;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.knowledge.BaseTest;
import com.huatu.ztk.knowledge.api.SubjectDubboService;
import com.huatu.ztk.knowledge.bean.SubjectTree;
import com.huatu.ztk.knowledge.servicePandora.SubjectService;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-07-22 12:48
 */
public class QuestionCollectServiceTest extends BaseTest {


    private static final Logger logger = LoggerFactory.getLogger(QuestionCollectServiceTest.class);

    @Autowired
    private QuestionCollectService questionCollectService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    SubjectService subjectService;

    @Autowired
    SubjectDubboService subjectDubboService;

    private long uid = 12252065;
    private int subject = 1;
    final int existQid = 31228;
    final int notExistQid = 111;
    @Test
    public void aaTest(){
        try {
            //测试收藏
            questionCollectService.collect(existQid,uid,subject);
            questionCollectService.collect(existQid,uid,subject);
        } catch (BizException e) {
        }

        //测试包含收藏
        Collection<String> collectQuestions = questionCollectService.findCollectQuestions(Lists.newArrayList(existQid + ""), uid,1);
        Assert.assertTrue(collectQuestions.contains(existQid+""));

        try {
            questionCollectService.collect(notExistQid,uid,subject);
            Assert.assertTrue(false);
        } catch (BizException e) {
            Assert.assertEquals(e.getErrorResult().getCode(), CommonErrors.RESOURCE_NOT_FOUND.getCode());
        }

        try {
            questionCollectService.cancel(existQid,uid,subject);
        } catch (BizException e) {
            Assert.assertTrue(false);
        }
        collectQuestions = questionCollectService.findCollectQuestions(Lists.newArrayList(existQid + ""), uid,1);
        System.out.println(collectQuestions);
        Assert.assertTrue(!collectQuestions.contains(existQid+""));

        try {
            questionCollectService.cancel(notExistQid,uid,subject);
            Assert.assertTrue(false);
        } catch (BizException e) {
            Assert.assertEquals(e.getErrorResult().getCode(), CommonErrors.RESOURCE_NOT_FOUND.getCode());
        }

    }

    @Test
    public void test123(){

        int o = subjectDubboService.getCatgoryBySubject(1);
        System.out.println("JsonUtil.toJson(notExistQid) = " + JsonUtil.toJson(o));
    }
}
