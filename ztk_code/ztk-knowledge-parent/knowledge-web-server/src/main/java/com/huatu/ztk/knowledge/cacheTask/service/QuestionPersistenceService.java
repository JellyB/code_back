package com.huatu.ztk.knowledge.cacheTask.service;

import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceEnum;
import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceModel;

import java.util.List;

/**
 * Created by junli on 2018/3/19.
 */
public interface QuestionPersistenceService {

    /**
     * 缓存有变更的key 值
     *
     * @param redisKey      需要缓存的key 值
     * @param cacheRedisKey 缓存的key值位置
     * @param updateTTL     是否需要修改缓存时间
     */
    void cacheAddUserActionFromRedis(String redisKey, QuestionPersistenceEnum.RedisKey cacheRedisKey, boolean updateTTL);

    /**
     * 移除缓存中有变更的key 值
     * 此处减少交互 使用批量操作
     *
     * @param redisKey      需要移除的key 值
     * @param cacheRedisKey 缓存的key值位置
     */
    void cacheDeleteUserActionFromRedis(String[] redisKey, QuestionPersistenceEnum.RedisKey cacheRedisKey);

    /**
     * 保存数据
     */
    void save(QuestionPersistenceModel model, QuestionPersistenceEnum.TableName tableName);

    /**
     * 根据用户ID 知识点ID 获取持久化数据
     */
    List<String> getQuestionIdByUserIdAndPointId(String userId, String questionPointId, QuestionPersistenceEnum.TableName tableName);


    /**
     * 根据一个key 获取错误试题缓存信息
     *
     * @param key redis
     * @return
     */
    List<String> getWrongCacheData(String key);

    /**
     * 根据一个key 获取收藏试题的缓存信息
     *
     * @param key redis
     * @return
     */
    List<String> getCollectCacheData(String key);

    /**
     * 根据一个key 获取完成试题的缓存信息
     *
     * @param key SSDB
     * @return
     */
    List<String> getFinishCacheData(String key);


    void judgeIsOutOfExpiredTime(long userId, List<String> collectCacheData, String pointId, String collectSetKey, QuestionPersistenceEnum.TableName collectTable, QuestionPersistenceModel model);

}
