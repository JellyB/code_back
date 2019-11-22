package com.huatu.tiku.teacher.service.question;

import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.entity.question.QuestionSimpleInfo;
import com.huatu.tiku.request.question.v1.BaseQuestionSearchReq;

import java.util.HashMap;
import java.util.List;

/**
 * Created by lijun on 2018/7/16
 */
public interface QuestionSearchService {
    /**
     * 试题信息列表查询  默认字段使用 -1
     */
    List<HashMap<String, Object>> list(BaseQuestionSearchReq baseQuestionSearchReq);

    /**
     * 获取试题的基础信息
     *
     * @param questionId   试题ID
     * @param questionType 试题类型 单选、多选 等类型
     * @return 根据不同的考题类型 返回不同的试题信息
     */
    HashMap getQuestionSimpleInfo(long questionId, int questionType);

    /**
     * 批量获取试题的基础信息
     *
     * @param params：questionId,questionType,multiId
     * @return 根据不同的考题类型 返回不同的试题信息
     */
    List<HashMap<String, Object>> getQuestionSimpleInfoListReturnMap(List<BaseQuestion> params);

    /**
     * 批量获取试题的基础信息
     *
     * @param params：questionId,questionType,multiId
     * @param baseQuestionId                         原始的questionId 信息,用以过滤复合题下只需要部分子题的情况
     * @return 根据不同的考题类型 返回不同的试题信息
     */
    List<HashMap<String, Object>> getQuestionSimpleInfoListReturnMap(List<BaseQuestion> params, List<Long> baseQuestionId);

    /**
     * 批量获取试题的基础信息
     *
     * @param params：questionId,questionType,multiId
     * @return 根据不同的考题类型 返回不同的试题信息
     */
    List<QuestionSimpleInfo> getQuestionSimpleInfoListReturnObject(List<BaseQuestion> params);

    /**
     * 批量获取试题的基础信息
     *
     * @param params：questionId,questionType,multiId
     * @param baseQuestionId                         原始的questionId 信息,用以过滤复合题下只需要部分子题的情况
     * @return 根据不同的考题类型 返回不同的试题信息
     */
    List<QuestionSimpleInfo> getQuestionSimpleInfoListReturnObject(List<BaseQuestion> params, List<Long> baseQuestionId);


    /**
     * 根据ID 批量组装数据
     *
     * @param questionIds
     * @return
     */
    List<QuestionSimpleInfo> listAllByQuestionId(List<Long> questionIds);

    /**
     * 根据试题ID 批量查询试题来源
     *
     * @param questionIds
     * @return
     */
    List<HashMap<String, Object>> findQuestionSource(List<Long> questionIds);

    /**
     * 根据试题ID，查询单个试题的试题来源信息
     *
     * @param questionId
     * @return
     */
    String findSingleQuestionSource(Long questionId);


}
