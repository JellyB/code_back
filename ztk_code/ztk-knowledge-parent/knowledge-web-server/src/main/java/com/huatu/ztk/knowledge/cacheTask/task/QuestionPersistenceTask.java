package com.huatu.ztk.knowledge.cacheTask.task;

import com.huatu.ztk.knowledge.cacheTask.util.QuestionPersistenceCacheUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Created by junli on 2018/3/22.
 */
@Component
public class QuestionPersistenceTask {
    private static final Logger logger = LoggerFactory.getLogger(QuestionPersistenceTask.class);

    @Autowired
    private QuestionPersistenceCacheUtil util;

    /**
     * 最大线程数量
     */
    private static final int MAX_POOL_NUM = 3;
    private static ExecutorService POOL = Executors.newFixedThreadPool(MAX_POOL_NUM);


    @Scheduled(cron = "0 0 2 ? * *")
    public void collectTask() {
        Consumer consumer = (pool) -> {
            try {
                logger.info("<<<<开始自动持久化 用户 -收藏试题- 信息");
                util.cacheCollectQuestions();
            } catch (Exception e) {
                logger.info("收藏考题信息同步异常,异常信息:{}", e.getMessage());
            }
        };
        doPooLTask(consumer);
    }

    @Scheduled(cron = "0 10 2 ? * *")
    public void wrongTask() {
        Consumer consumer = (pool) -> {
            try {
                logger.info("<<<<开始自动持久化 用户 -错误试题- 信息");
                util.cacheWrongQuestions();
            } catch (Exception e) {
                logger.info("错误考题信息同步异常,异常信息:{}", e.getMessage());
            }
        };
        doPooLTask(consumer);
    }

    @Scheduled(cron = "0 20 2 ? * *")
    public void finishTask() {
        Consumer consumer = (pool) -> {
            try {
                logger.info("<<<<开始自动持久化 用户 -完成试题- 信息");
                util.cacheFinishQuestions();
            } catch (Exception e) {
                logger.info("完成考题信息同步异常,异常信息:{}", e.getMessage());
            }
        };
        doPooLTask(consumer);
    }

    /**
     * 循环生成任务
     * @param consumer 任务执行逻辑
     */
    private static void doPooLTask(Consumer consumer) {
        for (int index = 0; index < MAX_POOL_NUM; index++) {
            POOL.execute(() -> consumer.accept(POOL));
        }
    }
}
