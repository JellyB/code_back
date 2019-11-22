package com.huatu.tiku.teacher.dao.mongo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.match.enums.MatchInfoEnum;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.ReflectQuestion;
import com.huatu.ztk.question.common.QuestionStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\6\25 0025.
 */
@Repository
@Slf4j
public class NewQuestionDao {

    /**
     * 存储试题的集合名字
     */
    public static final String collection = "ztk_question_new";
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    ReflectQuestionDao reflectQuestionDao;

    /**
     * 通过id查询
     *
     * @param question
     * @return
     */
    public Question save(Question question) {
        mongoTemplate.save(question, collection);
        return question;
    }

    public Question findById(int questionId) {
        return mongoTemplate.findById(questionId, Question.class, collection);
    }

    /**
     * 批量查询试题详情
     *
     * @param ids
     * @return
     */
    public List<Question> findByIds(List<Integer> ids) {
        List<ReflectQuestion> reflectQuestions = reflectQuestionDao.findByIds(ids);
        List<Integer> newIds = Lists.newArrayList(ids);
        Map<Integer, Integer> reflectMap = ids.stream().distinct().collect(Collectors.toMap(i -> i, i -> i));
        if (CollectionUtils.isNotEmpty(reflectQuestions)) {
            newIds.addAll(reflectQuestions.stream().map(i -> i.getNewId()).collect(Collectors.toList()));
            Map<Integer, Integer> collect = reflectQuestions.stream().collect(Collectors.toMap(i -> i.getOldId(), i -> i.getNewId()));
            reflectMap.putAll(collect);
        }
        Criteria criteria = Criteria.where("_id").in(newIds);
        Query query = new Query(criteria);
        List<Question> questions = mongoTemplate.find(query, Question.class, collection);
        List<Question> collect = ids.stream().map(i -> {
            int newId = reflectMap.get(i);
            Optional<Question> any = questions.stream().filter(q -> q.getId() == newId).findAny();
            if (any.isPresent()) {
                if (newId == i) {
                    Question question = any.get();
                    return question;
                } else {
                    Question question = mongoTemplate.findById(newId, Question.class, collection);
                    question.setId(i);
                    return question;
                }
            }
            return null;
        }).filter(Objects::nonNull)
                .collect(Collectors.toList());
//        for (Question question : questions) {
//            Integer id = question.getId();
//            if(reflectMap.containsKey(id)){
//                question.setId(reflectMap.get(id));
//                log.info("question {} is deleted",question.getId());
//                question.setStatus(QuestionStatus.DELETED);
//            }
//        }
//        if(CollectionUtils.isNotEmpty(questions)){
//            questions.removeIf(i->!(i instanceof GenericQuestion));
//        }
        return collect;
    }


    public List<Question> findByIdBetween(int startIndex, int endIndex) {
        Criteria criteria = Criteria.where("id").gt(startIndex).lte(endIndex);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Question.class, collection);
    }


    public int delByIdIn(List<Integer> ids) {
        Criteria criteria = Criteria.where("id").in(ids);
        Query query = new Query(criteria);
        return mongoTemplate.remove(query, Question.class, collection).getN();
    }


    public List<Question> findByIdGtAndLimit(int startIndex, int offset) {
        Criteria criteria = Criteria.where("id").gt(startIndex);
        Query query = new Query(criteria);
        query.limit(offset);
        return mongoTemplate.find(query, Question.class, collection);
    }


    /**
     * 根据科目查询试题信息（分页）
     *
     * @param startIndex
     * @param offset
     * @return
     */
    public List<Question> findBySubjectPage(int startIndex, int offset, int subject) {
        Criteria criteria = Criteria.where("id").gt(startIndex);
        if (subject > 0) {
            criteria.and("subject").is(subject);
        }
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.ASC, "id"));
        query.limit(offset);
        return mongoTemplate.find(query, Question.class, collection);
    }
}


