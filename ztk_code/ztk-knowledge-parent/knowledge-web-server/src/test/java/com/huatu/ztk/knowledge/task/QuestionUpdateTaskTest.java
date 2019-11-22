package com.huatu.ztk.knowledge.task;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.BaseTest;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.CompositeQuestion;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by shaojieyue
 * Created time 2016-07-14 19:36
 */
public class QuestionUpdateTaskTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(QuestionUpdateTaskTest.class);

    @Autowired
    private QuestionUpdateTask questionUpdateTask;

    @Autowired
    private QuestionDubboService questionDubboService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    ThreadPoolExecutor executor = new ThreadPoolExecutor(30, 80, 2000, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(200000));
    @Test
    public void onMessageTest(){

        for (int i = 4735; i < 4750; i++) {
            Map data = new HashMap<>();
            data.put("qid",2000000+i);
            Message message = new Message(JsonUtil.toJson(data).getBytes(),null);
            questionUpdateTask.onMessage(message);
        }

        for (int i = 91824; i < 100000; i++) {
            Map data = new HashMap<>();
            data.put("qid",i);
            Message message = new Message(JsonUtil.toJson(data).getBytes(),null);
            questionUpdateTask.onMessage(message);
        }
    }

    @Test
    public void dd(){
        int maxId= 0;
        while (true){
            final Criteria criteria = Criteria.where("_id").gt(maxId);
            Query query = new Query(criteria);
            query.with(new Sort(Sort.Direction.ASC,"_id")).limit(1000);
            final List<Question> questions = mongoTemplate.find(query, Question.class);
            if (CollectionUtils.isEmpty(questions)) {
                break;
            }
            for (Question question : questions) {
                maxId = Math.max(maxId,question.getId());
                try {
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                procces(question);
                            } catch (Exception e) {
                                logger.error("ex", e);
                            }
                        }
                    });
                }catch (RejectedExecutionException executionException){
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                procces(question);
                            } catch (Exception e) {
                                logger.error("ex", e);
                            }
                        }
                    });

                }

            }
            System.out.println("-------------------------处理完");
        }

    }

    private void procces(Question question) {
        final String questionIdKey = RedisKnowledgeKeys.getQuestionIdKey(question.getId());
        //把试题核心数据写入redis
        if (question instanceof GenericQuestion) {
            GenericQuestion genericQuestion = (GenericQuestion)question;
            //把核心信息存入redis
            Map<String,String> data = new HashMap();
            data.put("area", genericQuestion.getArea()+"");
            data.put("difficult", genericQuestion.getDifficult()+"");
            data.put("year", genericQuestion.getYear()+"");
            data.put("parent", genericQuestion.getParent()+"");
            data.put("moduleId", genericQuestion.getPoints().get(0)+"");//模块id
            redisTemplate.opsForHash().putAll(questionIdKey, data);
        }else if (question instanceof CompositeQuestion) {
            CompositeQuestion compositeQuestion = (CompositeQuestion)question;
            final Question subQuestion = questionDubboService.findById(compositeQuestion.getQuestions().get(0));
            //把核心信息存入redis
            Map<String,String> data = new HashMap();
            //复合题只存储id列表
            data.put("subIds", StringUtils.join(compositeQuestion.getQuestions(), ","));
            //取复合题第一个试题的模块id来作为复合题的模块id,目的是保证子题在抽题的时候的连续性(属于同一个模块)
            data.put("moduleId", ((GenericQuestion)subQuestion).getPoints().get(0)+"");
            redisTemplate.opsForHash().putAll(questionIdKey, data);
        }
        System.out.println("proccess id="+question.getId());
    }
}
