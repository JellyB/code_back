package com.huatu.ztk.knowledge.dao;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.BaseTest;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.common.QuestionMetaFlag;
import com.huatu.ztk.knowledge.service.QuestionErrorService;
import com.huatu.ztk.knowledge.service.v1.QuestionErrorServiceV1;
import com.huatu.ztk.knowledge.service.v2.impl.QuestionErrorServiceImplV2;
import com.huatu.ztk.question.bean.QuestionUserMeta;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Map;
import java.util.Set;

/**
 * Created by shaojieyue
 * Created time 2016-05-23 18:57
 */
public class QuestionPointDaoTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(QuestionPointDaoTest.class);

    @Autowired
    private QuestionPointDao questionPointDao;

    @Autowired
    private QuestionUserMetaDao questionUserMetaDao;

    @Autowired
    @Qualifier("questionErrorServiceImplV1")
    private QuestionErrorServiceV1 questionErrorServiceV1;

    @Test
    public void findByIdTest() {
//        QuestionPoint questionPoint = questionPointDao.findById(392);
//        Assert.assertEquals(questionPoint.getChildren().size(),7);
//
//        questionPoint = questionPointDao.findById(-1);
//        Assert.assertNull(questionPoint);
//
//        questionPoint = questionPointDao.findById(393);
//        Assert.assertEquals(questionPoint.getChildren().size(),4);
//
//        questionPoint = questionPointDao.findById(394);
//        Assert.assertEquals(questionPoint.getChildren().size(),0);
    }


    //查询错题数量
    @Test
    public void testQuestionMetaCount() {
        //questionUserMetaDao.getAllCount(233982368L);
    }


    /**
     * 查询某个知识点下的试题
     */
    @Test
    public void testQuestionMeta() {

        //查询某个一级知识点下的试题ids
        //questionErrorServiceV2.getQuestionIds(642, 233982368L);

        //查询某个知识点下的试题ids
        Set<Integer> questionIds = questionErrorServiceV1.getQuestionIds(642, 233982368L, 0, 15);
        logger.info("试题ID是:{}", JsonUtil.toJson(questionIds));

    }


    /**
     * 错题数量测试用例
     */
    @Test
    public void testQuestionCount() {

        long userId = 233982368L;
        int questionPointId = 642;

        QuestionPoint questionPoint = new QuestionPoint();
        questionPoint.setId(642);
        questionPoint.setLevel(1);
        //查询某个知识点下试题数量
       /* int count = questionErrorServiceV2.count(userId, questionPoint);
        logger.info("count是:{}", count);*/

        Map<Integer, Integer> countAllMap = questionErrorServiceV1.countAll(233982080L);
        logger.info("countAllMap 是:{}", countAllMap);
    }


    /**
     * 错题数量测试用例
     */
    @Test
    public void testCleanQuestion() {

        long userId = 233982368L;
        int pointId = 642;
        int question = 49363;

        //删除单个错题
        // questionErrorServiceV2.deleteQuestion(userId, pointId, question);

        //清除错题本
        questionErrorServiceV1.clearAll(userId, 1);

    }


}
