package com.huatu.tiku.teacher;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.teacher.dao.mongo.NewQuestionDao;
import com.huatu.tiku.teacher.dao.question.BaseQuestionMapper;
import com.huatu.ztk.question.bean.Question;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.junit.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangqingpeng on 2018/11/18.
 */
public class RabbitTest extends TikuBaseTest {
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    private NewQuestionDao newQuestionDao;
    @Autowired
    private BaseQuestionMapper baseQuestionMapper;

    @Test
    public void test1() {
        //分片查询mysql和mongo的数据比较
        int startIndex = 0;
        int offset = 1000;
        while(true){
            //查询MONGO复合条件的id（左开右闭）
            List<Question> mongoQuestionList= newQuestionDao.findByIdGtAndLimit(startIndex,offset);
            List<String> mongoQuestionIdList = Lists.newArrayList();
            if(CollectionUtils.isEmpty(mongoQuestionList)){
                break;
            }
            mongoQuestionList.forEach(question -> mongoQuestionIdList.add(question.getId()+""));
            int endIndex = mongoQuestionList.stream().map(Question::getId).max(Comparator.comparing(Integer::intValue)).get();

            List<Map<String,Long>> sqlQuestionList = baseQuestionMapper.findIdBetweenAnd(startIndex, endIndex);
            if(CollectionUtils.isNotEmpty(sqlQuestionList)){
                sqlQuestionList.forEach(question -> syncId(MapUtils.getInteger(question,"id")));
            }
            startIndex = endIndex;
            System.out.println("endIndex = " + endIndex);
        }
        System.out.println("结束了");
    }

    public void syncId(int id) {
        HashMap<Object, Object> map = Maps.newHashMap();
        map.put("qid", id);
        rabbitTemplate.convertAndSend("", "question_update_knowledge", map);
    }
}
