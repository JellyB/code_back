package com.huatu.ztk.knowledge.service.v1;

import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface QuestionErrorServiceV1 {

    /**
     * 查询用户在这个知识点上的错题量
     *
     * @param uid
     * @param questionPoint
     * @return
     */
    int count(long uid, QuestionPoint questionPoint);

    /**
     * 查询所有知识点下的错题量
     *
     * @param uid
     * @return
     */
    Map<Integer, Integer> countAll(long uid);


    Set<Integer> getQuestionIds(Integer pointId, long uid);

    /**
     * 用户知识点下的试题查询
     *
     * @param pointId
     * @param userId
     * @param end
     * @return
     */
    Set<Integer> getQuestionIds(int pointId, long userId,int start, int end);

    boolean isExist(long uid, int point, int questionId);

    /**
     * 删除知识点下的特定试题ID
     *
     * @param uid
     * @param point
     * @param questionId
     */
    void deleteQuestion(long uid, int point, int questionId);

    /**
     * 清空用户错题本
     * @param userId
     * @param subject
     */
    void clearAll(long userId, int subject);

    /**
     * 删除知识点下的背题模版数据
     * @param uid
     * @param point
     * @param questionId
     */
    void deleteLookMode(long uid, Integer point, int questionId);


    int countLookMode(long uid, int pointId);

    /**
     * 复制错题本数据到背题本
     * @param uid
     * @param pointId
     * @param total
     */
    void copyWrongSetToCursor(long uid, int pointId, int total);

    /**
     * 抽取特定数据的背题模式试题
     * @param uid
     * @param pointId
     * @param tempSize
     * @return
     */
    List<Integer> getQuestionIdsLookMode(long uid, int pointId, Integer tempSize);

    List<QuestionPointTree> queryErrorPointTrees(long userId, int subject);

    void clearRedisCache(long userId);


    void checkErrorPointRedis(long userId, int subject);
}
