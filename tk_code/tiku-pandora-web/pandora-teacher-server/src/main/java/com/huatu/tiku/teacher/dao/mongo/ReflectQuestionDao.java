package com.huatu.tiku.teacher.dao.mongo;

import com.huatu.tiku.entity.teacher.PaperQuestion;
import com.huatu.tiku.teacher.enums.StatusEnum;
import com.huatu.ztk.question.bean.ReflectQuestion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 映射表底层实现
 * Created by huangqingpeng on 2018/8/23.
 */
@Repository
@Slf4j
public class ReflectQuestionDao {
    /**
     * 存储试题的集合名字
     */
    public static final String collection = "reflect_question";

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<ReflectQuestion> findByIds(List<Integer> ids) {
        Criteria criteria = Criteria.where("oldId").in(ids);
        Query query = new Query(criteria);
        log.info("query={}", query);
        return mongoTemplate.find(query, ReflectQuestion.class, collection);
    }

    public ReflectQuestion findById(int id) {
        return mongoTemplate.findById(id, ReflectQuestion.class);
    }

    public void insertRelation(Integer questionId, Long newId) {
        ReflectQuestion reflectQuestion = ReflectQuestion.builder().oldId(questionId).newId(newId.intValue()).status(StatusEnum.NORMAL.getValue()).build();
        mongoTemplate.save(reflectQuestion);
    }

    /**
     * 试卷试题对应表中的试题ID替换成有效Id
     *
     * @param questionList
     */
    public void transQuestionId(List<PaperQuestion> questionList) {
        if (CollectionUtils.isEmpty(questionList)) {
            return;
        }
        List<Integer> questionIds = questionList.stream().map(PaperQuestion::getQuestionId).map(Long::intValue).collect(Collectors.toList());
        List<ReflectQuestion> reflections = findByIds(questionIds);
        if (CollectionUtils.isEmpty(reflections)) {
            return;
        }
        Map<Long, Long> reflectionMap = reflections.stream().collect(Collectors.toMap(i -> new Long(i.getOldId()), i -> new Long(i.getNewId())));
        for (PaperQuestion paperQuestion : questionList) {
            Long newId = reflectionMap.getOrDefault(paperQuestion.getQuestionId(), -1L);
            //如果有可以替换的试题,证明该ID已被去重处理，所以需要使用newID去替换他来做查询和后续处理
            if (newId > 0) {
                paperQuestion.setQuestionId(newId);
            }
        }
    }

    /**
     * 替换无效ID
     * @param questionIdList
     */
    public void transId(List<Long> questionIdList) {
        if(CollectionUtils.isEmpty(questionIdList)){
            return;
        }
        List<ReflectQuestion> reflections = findByIds(questionIdList.stream().map(Long::intValue).collect(Collectors.toList()));
        if (CollectionUtils.isEmpty(reflections)) {
            return;
        }
        List<Long> collect = questionIdList.stream().map(i -> {
            Optional<ReflectQuestion> first = reflections.stream().filter(reflectQuestion -> reflectQuestion.getOldId().equals(i.intValue())).findFirst();
            if (null == first) {
                return i;
            } else {
                Integer newId = first.get().getNewId();
                return new Long(newId);
            }
        }).collect(Collectors.toList());
        questionIdList.clear();
        questionIdList.addAll(collect);
    }

    public void deleteByIds(List<Integer> questionIds) {
        Criteria criteria = Criteria.where("oldId").in(questionIds);
        Query query = new Query(criteria);
        mongoTemplate.remove(query,ReflectQuestion.class,collection);
    }

    public List<ReflectQuestion> findAll() {
        Criteria criteria = Criteria.where("status").is(1);
        Query query = new Query(criteria);
        log.info("query={}", query);
        return mongoTemplate.find(query, ReflectQuestion.class, collection);
    }
}
