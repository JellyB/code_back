package com.huatu.tiku.teacher.service;

import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.teacher.dao.mongo.ReflectQuestionDao;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.ztk.question.bean.ReflectQuestion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/11/1.
 */
@Slf4j
public class ReflectQuestionTest extends TikuBaseTest {

    @Autowired
    ReflectQuestionDao reflectQuestionDao;

    @Autowired
    CommonQuestionServiceV1 commonQuestionServiceV1;
    @Test
    public void test(){
        List<ReflectQuestion> all = reflectQuestionDao.findAll();
        checkAvailable(all);
        if (CollectionUtils.isEmpty(all)) {
            return;
        }
        checkOldQuestion(all);
        if (CollectionUtils.isNotEmpty(all)) {
            System.out.println("正常的映射关系有："+all.size());
        }
    }

    private void checkOldQuestion(List<ReflectQuestion> all) {
        List<Integer> oldIds = all.stream().map(ReflectQuestion::getOldId).collect(Collectors.toList());
        Example example = new Example(BaseQuestion.class);
        example.and().andIn("id",oldIds);
        List<BaseQuestion> baseQuestions = commonQuestionServiceV1.selectByExample(example);
        if(CollectionUtils.isEmpty(baseQuestions)){
            return;
        }
        List<Integer> collect = baseQuestions.stream().map(i -> i.getId()).map(Long::intValue).collect(Collectors.toList());
        reflectQuestionDao.deleteByIds(collect);
        all.removeIf(i->collect.contains(i.getOldId()));
        System.out.println("有问题的旧试题有="+collect);
    }

    private void checkAvailable(List<ReflectQuestion> all) {
        List<Long> ids = all.stream().map(ReflectQuestion::getNewId).map(Long::new).collect(Collectors.toList());
        Example example = new Example(BaseQuestion.class);
        example.and().andIn("id",ids);
        List<BaseQuestion> baseQuestions = commonQuestionServiceV1.selectByExample(example);
        if(CollectionUtils.isNotEmpty(baseQuestions)){
            List<Long> collect = baseQuestions.stream().map(i -> i.getId()).collect(Collectors.toList());
            ids.removeAll(collect);
        }
        System.out.println("有问题的试题有="+ids);
        List<Integer> collect = ids.stream().map(Long::intValue).collect(Collectors.toList());
        List<Integer> collect1 = all.stream().filter(i -> collect.contains(i.getNewId())).map(ReflectQuestion::getOldId).collect(Collectors.toList());
        reflectQuestionDao.deleteByIds(collect1);
        all.removeIf(i->collect1.contains(i.getOldId()));
        System.out.println("有问题的旧试题有="+collect1);
    }
}
