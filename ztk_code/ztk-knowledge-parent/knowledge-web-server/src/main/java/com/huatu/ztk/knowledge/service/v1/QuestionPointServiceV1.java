package com.huatu.ztk.knowledge.service.v1;

import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;

import java.util.List;
import java.util.Map;

public interface QuestionPointServiceV1 {
    /**
     * 查询单个知识点信息
     * @param pointId
     * @return
     */
    QuestionPoint findById(int pointId);

    /**
     * 查询知识点下的试题量
     * @param pointId
     * @return
     */
    int count(int pointId);

    /**
     * 查询知识点下的所有试题
     * @return
     */
    Map<Integer, Integer> countAll();

    /**
     * 查询每个知识点下的试题ID集合
     * @param pointId
     * @return
     */
    List<String> getQuestionIds(int pointId);


    /**
     * 获取科目下的所有知识点（知识点无试题的不显示）
     * @param subject
     * @return
     */
    List<QuestionPoint> getAllQuestionPoints(int subject);

    /**
     * 获取科目下某一个知识点下的子知识点
     * @param subject
     * @param parent
     * @return
     */
    List<QuestionPoint> getQuestionPointsByParent(int subject, int parent);

    /**
     * 统计知识点下已完成数量+题量+错题量
     * @param treeList
     * @param finishCountMap
     * @param wrongCountMap
     * @param pointQuestionMap
     * @param unfinishedPointMap
     * @param userId
     * @return
     */
    List<QuestionPointTree> handlerCount(List<QuestionPointTree> treeList, Map<Integer, Integer> finishCountMap, Map<Integer, Integer> wrongCountMap, Map<Integer, Integer> pointQuestionMap, Map<Integer, Long> unfinishedPointMap, long userId);
}
