package com.huatu.tiku.teacher.listener;

import com.huatu.tiku.constants.cache.RedisKeyConstant;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.teacher.dao.mongo.ReflectQuestionDao;
import com.huatu.tiku.teacher.service.common.ImportService;
import com.huatu.tiku.teacher.service.question.OldQuestionService;
import com.huatu.tiku.teacher.service.question.SyncQuestionService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.GenericSubjectiveQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.ReflectQuestion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by huangqp on 2018\7\2 0002.
 */
@Slf4j
@Component
//@RabbitListener(queues = "sync_question_2_mysql_test")
public class SyncQuestion2DBListener {
    @Autowired
    SyncQuestionService syncQuestionService;
    @Autowired
    OldQuestionService oldQuestionService;
    @Autowired
    ReflectQuestionDao reflectQuestionService;
    @Autowired
    ImportService importService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    CommonQuestionServiceV1 commonQuestionServiceV1;

    @RabbitHandler
    public void onMessage(Map message) {
        try {
            log.info("message={}", message);
            int flag = Integer.parseInt(message.get("flag").toString());
            int questionId = Integer.parseInt(message.get("questionId").toString());
            int paperId = Integer.parseInt(message.get("paperId").toString());
            int sort = Integer.parseInt(message.get("sort").toString());
            String moduleName = message.get("moduleName").toString();
            //只建立绑定关系的可以先处理
            if (flag == -1) {
                syncQuestionService.bindQuestion(questionId, new Long(paperId), sort, moduleName);
                importService.sendQuestion2Mongo(questionId);
                return;
            }
            Question question = oldQuestionService.findQuestion(questionId);
            int parent = 0;
            if (question instanceof GenericQuestion) {
                parent = ((GenericQuestion) question).getParent();
            } else if (question instanceof GenericSubjectiveQuestion) {
                parent = ((GenericSubjectiveQuestion) question).getParent();
            }
            //如果有父节点，判断父节点是否已被处理，如果未被处理，则直接返回迁移
            if (parent > 0) {
                ReflectQuestion reflectQuestion = reflectQuestionService.findById(parent);
                if (reflectQuestion == null) {
                    String lockKey = RedisKeyConstant.getCompositeSync2MysqlLock(parent);
                    //自身服务多线程锁
                    synchronized (lockKey) {
                        //分布式锁
                        boolean myLock = getMyLocK(lockKey);
                        if (myLock) {
                            ReflectQuestion temp = reflectQuestionService.findById(parent);
                            if (temp == null) {
                                syncQuestionService.syncQuestion(parent, new Long(paperId), -1, moduleName);
                                //mysql->mongo
                                importService.sendQuestion2Mongo(questionId);
                            }
                            unLockMe(lockKey);
                        }
                    }
                }
                int limit = 1;
                while (true) {
                    ReflectQuestion reflectQuestion1 = reflectQuestionService.findById(parent);
                    BaseQuestion baseQuestion = commonQuestionServiceV1.selectByPrimaryKey(new Long(parent));
                    if (null != reflectQuestion1 || null != baseQuestion) {
                        syncQuestionService.syncQuestion(questionId, new Long(paperId), sort, moduleName);
                        importService.sendQuestion2Mongo(questionId);
                        break;
                    }
                    Thread.currentThread().sleep(100);
                    limit++;
                    if (limit > 100) {
                        log.info("等待线程处理超时，questionId={},分布式锁key={}", questionId, RedisKeyConstant.getCompositeSync2MysqlLock(parent));
                        log.error("sync_question_2_mysql fail，message={}", message);
                        return;
                    }
                }
            } else {
                syncQuestionService.syncQuestion(questionId, new Long(paperId), sort, moduleName);
                importService.sendQuestion2Mongo(questionId);
            }

        } catch (Exception e) {
            log.error("消息消费异常。。。");
            log.error("sync_question_2_mysql fail，message={}", message);
            e.printStackTrace();
        }
    }

    private void unLockMe(String lockKey) {
        redisTemplate.delete(lockKey);
    }

    /**
     * 是否能获取到分布式锁
     *
     * @param lockKey
     * @return
     */
    private boolean getMyLocK(String lockKey) {
        String serverIp = System.getProperty("server_ip");
        return redisTemplate.opsForValue().setIfAbsent(lockKey, serverIp);
    }


}

