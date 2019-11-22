package com.huatu.ztk.knowledge.service.v1;

import com.huatu.ztk.knowledge.bean.QuestionPoint;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface QuestionFinishServiceV1 {

    /**
     * 用户单个知识点做题量查询
     * @param uid
     * @param questionPoint
     * @return
     */
    int count(long uid, QuestionPoint questionPoint);

    /**
     * 用户所有知识点下题量查询
     * @param userId
     * @return
     */
    Map<Integer,Integer> countAll(long userId);

    /**
     * 用户某些知识点下的题量查询
     * @param points
     * @param userId
     */
    Map<Integer,Integer> countByPoints(List<Integer> points, long userId);

    /**
     * qids中已做过试题筛选查询
     * @param uid
     * @param pointId
     * @param qids
     * @return
     */
    Set<String> filterQuestionIds(long uid, int pointId, Set<String> qids);


    Set<String> getQuestionIds(long uid, int pointId);

    void clearRedisCache(long userId);
}
