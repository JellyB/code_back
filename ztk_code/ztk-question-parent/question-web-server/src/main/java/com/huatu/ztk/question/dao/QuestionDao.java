package com.huatu.ztk.question.dao;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.ReflectQuestion;
import com.huatu.ztk.question.common.QuestionStatus;
import com.huatu.ztk.question.common.QuestionType;
import com.huatu.ztk.question.util.PageUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapreduce.GroupBy;
import org.springframework.data.mongodb.core.mapreduce.GroupByResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.regex.Pattern.compile;

/**
 * 试题dao层
 * Created by shaojieyue
 * Created time 2016-04-25 16:11
 */

@Repository
public class QuestionDao {
    private static final Logger logger = LoggerFactory.getLogger(QuestionDao.class);
    public static final int ID_BASE = 2000000;//id基数，防止跟以前的id冲突
    //根据试题id取试题的缓存
    Cache<String, List<Question>> QUESTIONS_FILTER_CACHE =
            CacheBuilder.newBuilder()
                    .maximumSize(10)
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .build();

    /**
     * 存储试题的集合名字
     */
//    public static final String collection = "ztk_question";
    public static final String collection = "ztk_question_new";
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 插入试题
     *
     * @param question
     */
    public int insert(Question question) {
        logger.info("insert question:{}", question);
        int id = -1;
        Throwable throwable = null;
        if (question.getId() < 1) {//没有设置id，则生成id此处主要是数据初始化时用到
            long questionId = mongoTemplate.count(new Query(), collection) + ID_BASE;
            question.setId((int) questionId);//设置id
        }

        for (int i = 0; i < 5; i++) {//此处循环是未来保证key冲突的情况下也能插入到mongo里
            try {
                mongoTemplate.save(question, collection);
                id = question.getId();//获取id
                break;//插入成功，则跳出循环
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return id;
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    public Question findById(int id) {
        final Question question = mongoTemplate.findById(id, Question.class, collection);
        //删除状态判断放到各自的业务方法中
//        if (null != question && question.getStatus() == 4) {
//            return null;
//        }
        return question;
    }

    /**
     * 通过id批量查询
     *
     * @param ids
     * @return
     */
    public List<Question> findBath(List<Integer> ids) {
        if (ids != null && ids.size() < 100) {
            Query query = new Query();
            //通过id批量获取
            query.addCriteria(Criteria.where("id").in(ids).and("status").ne(QuestionStatus.DELETED));
            final List<Question> questions = mongoTemplate.find(query, Question.class, collection);
            return questions;
        }
        String nameStr = Joiner.on(",").join(ids);

        List<Question> questionList = QUESTIONS_FILTER_CACHE.getIfPresent(nameStr);
        if (questionList != null) {
            return questionList;
        }
        Query query = new Query();
        //通过id批量获取
        query.addCriteria(Criteria.where("id").in(ids).and("status").ne(QuestionStatus.DELETED));
        final List<Question> questions = mongoTemplate.find(query, Question.class, collection);
        QUESTIONS_FILTER_CACHE.put(nameStr, questions);
        return questions;
    }


    /**
     * 整体更新文档
     *
     * @param question
     */
    public void update(Question question) {
        logger.info("update question:{}", question);
        mongoTemplate.save(question, collection);
    }

    /**
     * 通过type 查询
     *
     * @param type
     * @param mode
     * @return
     */
    public List<Question> findByType(Integer type, Integer mode) {
        Query query = new Query();
        query.addCriteria(
                Criteria.where("points").is(type)
                        .andOperator(
                                Criteria.where("mode").is(mode),
                                Criteria.where("status").is(QuestionStatus.AUDIT_SUCCESS)
                        )
        );
        List<Question> questionList = mongoTemplate.find(query, Question.class, collection);
        return questionList;
    }

    /**
     * 通过试题id和科目批量查询试题信息
     *
     * @param subject
     * @param questionId
     * @param size
     * @return
     */
    public List<Question> findPageBySubject(int subject, int questionId, int size) {
        Query query = new Query();
        query.addCriteria(
                Criteria.where("subject").is(subject)
                        .andOperator(
                                Criteria.where("id").gt(questionId),
                                Criteria.where("status").is(QuestionStatus.AUDIT_SUCCESS)
                        )
        );
        query.with(new Sort(Sort.Direction.ASC, "id"));
        query.limit(size);
        List<Question> questionList = mongoTemplate.find(query, Question.class, collection);
        return questionList;
    }

    public List<Question> findQuestionsForPage(int cursor, int size, int subject) {
        Query query = new Query(Criteria.where("id").gt(cursor).and("subject").is(subject));
        query.with(new Sort(Sort.Direction.ASC, "id")).limit(size);
        return mongoTemplate.find(query, Question.class, "ztk_question");
    }

    public PageUtil<Question> findByConditionV3(Integer type, Integer difficult, Integer mode, String points, String content, String ids, Integer page, Integer pageSize, String subject) {

        List<Question> questions = Lists.newArrayList();
        Integer questionId = null;
        if (checkContentIsNum(content)) {
            questionId = new Integer(content);
        }
        List idList = new ArrayList();
        if (StringUtils.isNotEmpty(ids)) {
            String[] idArray = ids.split(",");
            for (String str : idArray) {
                Integer id = Ints.tryParse(str);
                idList.add(id);
            }
        }

        Criteria criteria = Criteria.where("status").is(QuestionStatus.AUDIT_SUCCESS);
        //id查询条件
        if (CollectionUtils.isNotEmpty(idList) && null == questionId) {
            criteria.and("id").nin(idList);
        } else if (CollectionUtils.isNotEmpty(idList) && null != questionId) {
            if (idList.contains(questionId)) {
                return null;
            } else {
                criteria.and("id").is(questionId);
            }
        } else if (CollectionUtils.isEmpty(idList) && null != questionId) {
            criteria.and("id").is(questionId);
        }

        //题干或者材料中含有关键字
        if (StringUtils.isNotEmpty(content) && null == questionId) {
            criteria.and("stem").regex(".*" + content + ".*");
        }


        if (StringUtils.isNotEmpty(points)) {
            String[] pointArray = points.split(",");
            List pointList = new ArrayList();
            for (String str : pointArray) {
                Integer point = Ints.tryParse(str);
                pointList.add(point);
            }
            criteria.and("points").in(pointList);
        }
        if (difficult > 0) {
            criteria.and("difficult").is(difficult);
        }
        
        if (StringUtils.isEmpty(subject)) {
            LinkedList<Object> subjectList = new LinkedList<>();
            String[] subjectArray = subject.split(",");
            for (String str : subjectArray) {
                Integer id = Ints.tryParse(str);
                subjectList.add(id);
            }
            criteria.and("subject").in(subjectList);
        }
//        if (subject > 0) {
//            criteria.and("subject").is(subject);
//        }
        if (mode > 0) {
            criteria.and("mode").is(mode);
        }

        //1客观题  2复合题（105.107）
        List<Integer> questionTypeList = Arrays.asList(QuestionType.COMPOSITED, QuestionType.MULTI_SUBJECTIVE);
        if (type == 1) {
            criteria.and("type").nin(questionTypeList);
            criteria.orOperator(Criteria.where("parent").is(0), Criteria.where("parent").exists(false));
        } else if (type == 2) {
            //复合题，parent不是0
            criteria.and("parent").ne(0);
        }
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.DESC, "createTime"));
        Stopwatch stopwatch = Stopwatch.createStarted();
        long count = 50000;
//                mongoTemplate.count(query, Question.class, collection);
//        logger.info("=======mongo查询用时count====="+String.valueOf(stopwatch.stop()));
        query.skip((page - 1) * pageSize).limit(pageSize);        //按年份排序

//        logger.info("mongo查询条件"+query.toString());
        Stopwatch stopwatch2 = Stopwatch.createStarted();
        List<Question> questionList = mongoTemplate.find(query, Question.class, collection);
//        logger.info("=======mongo查询用时page====="+String.valueOf(stopwatch2.stop()));
        questions.addAll(questionList);

        PageUtil p = PageUtil.builder()
                .result(questionList)
                .next(count > (page + 1) * pageSize ? 1 : 0)
                .total(count)
                .totalPage((0 == count % pageSize) ? (count / pageSize) : (count / pageSize + 1))
                .build();
        return p;

    }

    /**
     * 检查字符串是否是数字
     *
     * @return
     */
    boolean checkContentIsNum(String content) {
        if (StringUtils.isNotEmpty(content)) {
            Pattern pattern = compile("^[0-9]*$");
            Matcher matcher = pattern.matcher(content);
            return matcher.find();
        } else {
            return false;
        }
    }

    public List<Map> findMultiV3(Integer difficult, Integer mode, String points, String content, String ids, String subject) {
        Integer questionId = null;
        if (checkContentIsNum(content)) {
            questionId = new Integer(content);
        }
        List idList = new ArrayList();
        if (StringUtils.isNotEmpty(ids)) {
            String[] idArray = ids.split(",");
            for (String str : idArray) {
                Integer id = Ints.tryParse(str);
                idList.add(id);
            }
        }

        Criteria criteria = Criteria.where("status").is(QuestionStatus.AUDIT_SUCCESS);
        //id查询条件
        if (CollectionUtils.isNotEmpty(idList) && null == questionId) {
            criteria.and("id").nin(idList);
        } else if (CollectionUtils.isNotEmpty(idList) && null != questionId) {
            if (idList.contains(questionId)) {
                return new ArrayList<>();
            } else {
                criteria.and("id").is(questionId);
            }
        } else if (CollectionUtils.isEmpty(idList) && null != questionId) {
            criteria.and("id").is(questionId);
        }

        //题干或者材料中含有关键字
        if (StringUtils.isNotEmpty(content) && null == questionId) {
            criteria.and("stem").regex(".*" + content + ".*");
        }
//        if (subject > 0) {
//            criteria.and("subject").is(subject);
//        }
        if (StringUtils.isEmpty(subject)) {
            LinkedList<Object> subjectList = new LinkedList<>();
            String[] subjectArray = subject.split(",");
            for (String str : subjectArray) {
                Integer id = Ints.tryParse(str);
                subjectList.add(id);
            }
            criteria.and("subject").in(subjectList);
        }
        if (StringUtils.isNotEmpty(points)) {
            String[] pointArray = points.split(",");
            List pointList = new ArrayList();
            for (String str : pointArray) {
                Integer point = Ints.tryParse(str);
                pointList.add(point);
            }
            criteria.and("points").in(pointList);
        }
        if (difficult > 0) {
            criteria.and("difficult").is(difficult);
        }
        if (mode > 0) {
            criteria.and("mode").is(mode);
        }
        criteria.and("parent").ne(0);

        GroupByResults<Map> results = mongoTemplate.group(
                criteria,
                collection,
                GroupBy.key("parent").initialDocument("{ childCount: 0 }").reduceFunction("function(doc, prev) { prev.childCount += 1 }"),
                Map.class);
        return Lists.newArrayList(results.iterator());

    }


    /**
     * 将试题ID换成替换ID
     *
     * @param questions
     */
    public List<Integer> transReflectionId(List<Integer> questions) {
        Criteria criteria = Criteria.where("id").in(questions);
        Query query = new Query(criteria);
        logger.info("query={}", query);
        List<ReflectQuestion> reflectQuestions = mongoTemplate.find(query, ReflectQuestion.class, "reflect_question");
        if (CollectionUtils.isNotEmpty(reflectQuestions)) {
            Map<Integer, Integer> tempMap = reflectQuestions.stream().collect(Collectors.toMap(i -> i.getOldId(), i -> i.getNewId()));
            return questions.stream().map(i -> tempMap.getOrDefault(i, i)).collect(Collectors.toList());
        }
        return questions;
    }
}
