package com.huatu.tiku.teacher.mongotest;

import com.alibaba.fastjson.JSON;
import com.huatu.tiku.TikuBaseTest;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.QuestionMeta;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author jbzm
 * @date 2018/7/20 1:51 PM
 **/
@Slf4j
public class lol extends TikuBaseTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private MongoTemplate mongoTemplate;

    private int pageNum = 0;

    @Test
    public void conutTest() {
        Query query = new Query();
        long count = mongoTemplate.count(query, GenericQuestion.class);

        System.out.println(count);
    }

    @Test
    public void genericQuestion() throws InterruptedException {
        Thread thread1 = new Thread(new Go(), "jbzm01");
        Thread thread2 = new Thread(new Go(), "jbzm02");
        Thread thread3 = new Thread(new Go(), "jbzm03");
        Thread thread4 = new Thread(new Go(), "jbzm04");
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        while (true) {
            Thread.sleep(1000);
        }
    }

    class Go implements Runnable {
        int pageSize = 100;

        @Override
        public void run() {
            while (true) {
                SpringbootPageable pageable = new SpringbootPageable();
                PageModel pm = new PageModel();
                Query query = new Query();
                pm.setPagenumber(getPageNum());
                log.info("page num" + pageNum);
                pm.setPagesize(pageSize);
                pageable.setPage(pm);
                List<GenericQuestion> all = mongoTemplate.find(query.with(pageable), GenericQuestion.class);
                if (all.size() < 5) {
                    break;
                }
                changeData(all);
            }
        }
    }

    private synchronized int getPageNum() {
        this.pageNum = pageNum + 1;
        return pageNum;
    }

    private void changeData(List<GenericQuestion> all) {
        List<String> collect = all.stream().filter(x -> x.getMeta() != null && x.getMeta().getAnswers().length >= 3).map(x -> {
            ElasticsearchVO elasticsearchVO = new ElasticsearchVO();
            BeanUtils.copyProperties(x, elasticsearchVO);
            int answer = x.getAnswer();
            QuestionMeta meta = x.getMeta();
            int[] answers = meta.getAnswers();
            BiFunction<Integer, int[], Integer> integerArrayIntegerBiFunction = (answer1, array) -> {
                if (array.length < 3) {
                    return -1;
                }
                for (int i = 0; i <= array.length; i++) {
                    try {
                        if (array[i] == answer1) {
                            return i;
                        }
                    } catch (RuntimeException e) {
                        for (int a : array) {
                            log.info(a + "");
                        }
                    }
                }
                throw new RuntimeException("o no fuck!!!!");
            };
            Integer result = integerArrayIntegerBiFunction.apply(answer, answers);
            int[] percents = meta.getPercents();
            int percent = percents[result];
            elasticsearchVO.setPercent(percent);
            return JSON.toJSONString(elasticsearchVO);
        }).collect(Collectors.toList());
        if (collect.size() == 0) {
            return;
        }
        BaseRequest baseRequest = new BaseRequest();
        baseRequest.setData(collect);
        baseRequest.setIndex("pandora_test");
        baseRequest.setType("test_jbzm");
        baseRequest.setOperation("save_batch");
        String toJSONString = JSON.toJSONString(baseRequest);
        log.info("send mq");
        rabbitTemplate.convertAndSend("pandora_question", toJSONString);
    }
}