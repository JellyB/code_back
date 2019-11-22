package com.huatu.ztk.knowledge.cacheTask.dao;

import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceEnum;
import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceModel;

import java.util.List;

/**
 * Created by junli on 2018/3/19.
 */
public interface QuestionPersistenceDao {

    /**
     * 保存数据
     */
    void save(String userId, String questionPointId, String questionId, QuestionPersistenceEnum.TableName tableName);

    /**
     * 根据用户ID 知识点ID 获取持久化数据
     */
    List<String> getQuestionIdByUserIdAndPointId(String userId, String questionPointId, QuestionPersistenceEnum.TableName tableName);

    /**
     * 逻辑删除
     */
    void delete(String userId, String questionPointId, QuestionPersistenceEnum.TableName tableName);

    /**
     * 物理删除
     */
    void deletePhysics(String userId, String questionPointId, QuestionPersistenceEnum.TableName tableName);

    /**
     * 根据用户ID查询所有的知识点
     */
    List<QuestionPersistenceModel> findByUserId(String userId, QuestionPersistenceEnum.TableName tableName);
}
