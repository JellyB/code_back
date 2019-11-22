package com.huatu.tiku.teacher.service;

import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.teacher.dao.mongo.NewQuestionDao;
import com.huatu.tiku.teacher.dao.mongo.OldQuestionDao;
import com.huatu.tiku.teacher.dao.mongo.ReflectQuestionDao;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.ReflectQuestion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/9/7.
 */
@Slf4j
public class ReflectQuestionServiceT extends TikuBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(TikuBaseTest.class);

    @Autowired
    ReflectQuestionDao reflectQuestionDao;

    @Autowired
    NewQuestionDao newQuestionDao;

    @Autowired
    OldQuestionDao oldQuestionDao;

    @Autowired
    CommonQuestionServiceV1 commonQuestionServiceV1;

    @Test
    public void testReflect() {
        //查询所有的oldId和newId
        List<ReflectQuestion> reflectQuestionList = reflectQuestionDao.findAll();
        if (CollectionUtils.isEmpty(reflectQuestionList)) {
            return;
        }
        List<Integer> newIds = reflectQuestionList.stream().map(ReflectQuestion::getNewId).collect(Collectors.toList());
        List<Integer> oldIds = reflectQuestionList.stream().map(ReflectQuestion::getOldId).collect(Collectors.toList());

        this.checkOldQuestionId(oldIds);
        this.checkNewQuestionId(newIds);
        //3.有交集
        if (oldIds.removeAll(newIds)) {
            logger.info("olds 与newIDs有交集", oldIds);
        }

    }

    /**
     * 校验旧题
     */
    public void checkOldQuestionId(List<Integer> oldIds) {
        //1.old 是否都存在于ztk_question中
        List<Question> existOldQuestionList = oldQuestionDao.findByIds(oldIds);
        List<Integer> existOldIDList = existOldQuestionList.stream().map(Question::getId).collect(Collectors.toList());
        existPandora(oldIds);
        //求交集,交集即不存在的,有问题
        oldIds.removeAll(existOldIDList);
        if (CollectionUtils.isNotEmpty(oldIds)) {
            logger.info("oldIds存在于reflectQuestion,但不存在于ztK_question", oldIds);
        }
        List<Question> questions = newQuestionDao.findByIds(oldIds);
        if (CollectionUtils.isNotEmpty(questions)) {
            logger.info("oldIds存在于reflectQuestion,但存在ztk_question_new中", questions);
        }


    }


    /**
     * 校验新题
     *
     * @param newIds
     */
    public void checkNewQuestionId(List<Integer> newIds) {
        //2.newI  mongo ztk_question_new|pandora.base_question （肯定关系）
        List<Question> existNewQuestionList = newQuestionDao.findByIds(newIds);
        List<Integer> existNewIDLis = existNewQuestionList.stream().map(Question::getId).collect(Collectors.toList());
        existPandora(newIds);
        newIds.removeAll(existNewIDLis);
        if (CollectionUtils.isNotEmpty(newIds)) {
            logger.info("newIds存在于reflectQuestion,但不存在于ztk_question", newIds);
        }
    }


    //是否存在 pandora中
    public void existPandora(List<Integer> ids) {

        Example example = new Example(BaseQuestion.class);
        example.and().andIn("id", ids);
        List<BaseQuestion> existPQuestion = commonQuestionServiceV1.selectByExample(example);
        List<Long> pandoraId = existPQuestion.stream().map(BaseQuestion::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(existPQuestion)) {
            logger.info("oldIds存在于reflectQuestion,但存在于pandora中", pandoraId);
        }
    }

}
