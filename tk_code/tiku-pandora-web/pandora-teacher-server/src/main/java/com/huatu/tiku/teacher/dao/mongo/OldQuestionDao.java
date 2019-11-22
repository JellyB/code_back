package com.huatu.tiku.teacher.dao.mongo;

import com.google.common.collect.Lists;
import com.huatu.ztk.question.bean.Question;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\6\25 0025.
 */
@Slf4j
@Repository
public class OldQuestionDao {
    /**
     * id基数，防止跟以前的id冲突
     */
    public static final int ID_BASE = 2000000;

    /**
     * 存储试题的集合名字
     */
    public static final String collection = "ztk_question";
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    NewQuestionDao newQuestionDao;

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    public Question findById(int id) {
        Question question = mongoTemplate.findById(id, Question.class, collection);
        if (null == question) {
            question = newQuestionDao.findById(id);
        }
        return question;
    }

    /**
     * 批量查询试题详情
     *
     * @param ids
     * @return
     */
    public List<Question> findByIds(List<Integer> ids) {
        Criteria criteria = Criteria.where("_id").in(ids);
        Query query = new Query(criteria);
        log.info("query={}", query);
        List<Question> questions = mongoTemplate.find(query, Question.class, collection);
        if (CollectionUtils.isEmpty(questions)) {
            return newQuestionDao.findByIds(ids);
        }
        if (questions.size() < ids.size()) {
            List<Integer> collect = questions.parallelStream().map(Question::getId).collect(Collectors.toList());
            List<Integer> newIds = ids.stream().filter(i -> !collect.contains(i)).collect(Collectors.toList());
            List<Question> list = newQuestionDao.findByIds(newIds);
            if (CollectionUtils.isNotEmpty(list)) {
                questions.addAll(list);
            }
            List<Question> result = Lists.newArrayList();
            ids.stream().forEach(id -> {
                Optional<Question> first = questions.stream().filter(i -> i.getId() == id).findFirst();
                if (first.isPresent()) {
                    result.add(first.get());
                }
            });
            return result;
        }
        return questions;
    }


}

