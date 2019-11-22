package com.huatu.ztk.knowledge.cacheTask.util;

import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceEnum;
import com.huatu.ztk.knowledge.cacheTask.service.QuestionPersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by junli on 2018/3/20.
 */
@Component
public class QuestionPersistenceUtil {

    @Autowired
    private QuestionPersistenceService questionPersistenceService;

    public void addFinishQuestionPersistence(String key) {
        //由于 finishQuestion 存储在SSDB 中,此处不处理 key
        questionPersistenceService.cacheAddUserActionFromRedis(key, QuestionPersistenceEnum.RedisKey.QUESTION_USER_CACHE_FINISH, false);
    }

    public void addCollectQuestionPersistence(String key) {
        questionPersistenceService.cacheAddUserActionFromRedis(key, QuestionPersistenceEnum.RedisKey.QUESTION_USER_CACHE_COLLECT, true);
    }

    public void addWrongQuestionPersistence(String key) {
        questionPersistenceService.cacheAddUserActionFromRedis(key, QuestionPersistenceEnum.RedisKey.QUESTION_USER_CACHE_WRONG, true);
    }

}