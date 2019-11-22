package com.huatu.ztk.knowledge.dataClean;

import com.google.common.collect.Sets;
import com.huatu.ztk.knowledge.BaseTest;
import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceEnum;
import com.huatu.ztk.knowledge.cacheTask.service.QuestionPersistenceService;
import com.huatu.ztk.knowledge.cacheTask.task.UserDataClean;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

/**
 * Created by lijun on 2018/9/11
 */
public class QuestionDataHandle extends BaseTest {

    @Autowired
    private UserDataClean userDataClean;

    @Autowired
    private QuestionPersistenceService service;

    @Test
    public void test() {
        HashSet<String> wrongPointKeySet = Sets.newHashSet();
        wrongPointKeySet.add(RedisKnowledgeKeys.getWrongSetKey(233883562L, 423));
        Function<String, List<String>> wrongFunction = (cacheKey) -> {
            List<String> wrongCacheData = service.getWrongCacheData(cacheKey);
            return wrongCacheData;
        };

//        userDataClean.doCleanPoint(wrongPointKeySet, wrongFunction,
//                QuestionPersistenceEnum.RedisKey.QUESTION_USER_CACHE_WRONG,
//                QuestionPersistenceEnum.TableName.QUESTION_USER_CACHE_WRONG
//        );
    }
}
