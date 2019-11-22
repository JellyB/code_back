package com.huatu.ztk.report.task;

import com.google.common.collect.Maps;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.report.dao.AnswerCardDao;
import com.huatu.ztk.report.service.QuestionUserMetaService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
public class InitQuestionUserMetaTask {

    private static final Logger logger = LoggerFactory.getLogger(InitQuestionUserMetaTask.class);

    @Autowired
    private AnswerCardDao answerCardDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String SYNC_USER_META_CURSOR = "syncUserMetaCursor";

    private static String NANO_TIME_LOCK = "";
    @PostConstruct
    public void init() {
        //添加停止任务线程
        Runtime.getRuntime().addShutdownHook(new Thread(()-> unlock()));
    }

    @Scheduled(fixedRate = 20000)
    public void initQuestionUserMeta(){
        try{
            System.out.println("stage1");
            if (!getLock()) {
                return;
            }
            System.out.println("stage2");
            Object o = redisTemplate.opsForValue().get(SYNC_USER_META_CURSOR);
            System.out.println("stage3");
            long i = 0;
            try {
                if(null != o){
                    i = Long.parseLong(o.toString());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            System.out.println("cone:"+o);
            System.out.println("stage4");
            List<AnswerCard> answerCards = answerCardDao.findForPage(i, 1000);
            System.out.println("stage5");
            handlerAnswerCard.accept(answerCards);
            System.out.println("stage6");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            unlock();
            System.out.println("stage7");
        }
    }

    private boolean getLock() {
        System.out.println("check0");
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();

        String lockKey = getInitQuestionMetaLockKey();

        String value = opsForValue.get(lockKey);

        logger.info("get lock timestamp={}",System.currentTimeMillis());
        if (org.apache.commons.lang3.StringUtils.isBlank(value)) { //值为空
            System.out.println("check1");
            boolean booleanValue = opsForValue.setIfAbsent(lockKey, getLocalTimeLock()).booleanValue();
            if(booleanValue){
                System.out.println("check1.1");
                redisTemplate.expire(lockKey,1, TimeUnit.MINUTES);
            }
            return booleanValue;
        } else if (org.apache.commons.lang3.StringUtils.isNoneBlank(value) && !value.equals(getLocalTimeLock())) {
            System.out.println("check2");
            //被其它服务器锁定
            return false;
        } else { //被自己锁定
            System.out.println("check3");
            return true;
        }
    }

    /**
     * 正式处理逻辑
     */
    Consumer<List<AnswerCard>> handlerAnswerCard = (answerCards -> {
        if(CollectionUtils.isEmpty(answerCards)){
            return;
        }
        long maxId = 0;
        long l = System.currentTimeMillis();
        for (AnswerCard answerCard : answerCards) {
            maxId = Math.max(answerCard.getId(),maxId);
            sendMessage("init_question_user_meta",answerCard);
        }
        System.out.println(maxId+"|"+answerCards.size()+"|"+(System.currentTimeMillis()-l));
        redisTemplate.opsForValue().set(SYNC_USER_META_CURSOR,maxId+"");
    });

    private void sendMessage(String queueName, AnswerCard answerCard) {
        Map map = Maps.newHashMap();
        map.put("id",answerCard.getId());
        rabbitTemplate.convertAndSend("",queueName,map);
    }

    private void unlock() {
        String lockKey = getInitQuestionMetaLockKey();
        String currentServer = (String) redisTemplate.opsForValue().get(lockKey);

        logger.info("current server={}",currentServer);
        if (getLocalTimeLock().equals(currentServer)) {
            redisTemplate.delete(lockKey);
            logger.info("release lock,server={},timestamp={}",currentServer,System.currentTimeMillis());
        }
        NANO_TIME_LOCK = "";
    }

    private String getInitQuestionMetaLockKey() {
        return SYNC_USER_META_CURSOR + "_lockKey";
    }

    private String getLocalTimeLock() {
        if(StringUtils.isBlank(NANO_TIME_LOCK)){
            NANO_TIME_LOCK = System.nanoTime()+"";
        }
        return NANO_TIME_LOCK;
    }
}
