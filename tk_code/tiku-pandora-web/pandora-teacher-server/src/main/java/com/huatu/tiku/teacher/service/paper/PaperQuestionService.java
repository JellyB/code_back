package com.huatu.tiku.teacher.service.paper;

import com.huatu.tiku.entity.teacher.PaperQuestion;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.service.BaseService;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Created by lijun on 2018/8/3
 */
public interface PaperQuestionService extends BaseService<PaperQuestion> {

    /**
     * 保存试卷信息
     * 该接口会判断序号唯一
     *
     * @param questionId 试题ID
     * @param paperId    试卷ID
     * @param moduleId   对应试卷模块ID
     * @param sort       排列序号
     * @param typeInfo   试卷类型
     * @return 操作成功数量
     */
    int savePaperQuestionWithSort(Long questionId, Long paperId, Integer moduleId, Integer sort, PaperInfoEnum.TypeInfo typeInfo);

    /**
     * 保存试卷信息
     * 该接口会判断序号唯一
     *
     * @param questionId 试题ID
     * @param paperId    试卷ID
     * @param moduleId   对应试卷模块ID
     * @param sort       排列序号
     * @param typeInfo   试卷类型
     * @param score      分数
     * @return 操作成功数量
     */
    int savePaperQuestionWithSort(Long questionId, Long paperId, Integer moduleId, Integer sort, Double score, BiConsumer<Long, Integer> validatePaperInfo, PaperInfoEnum.TypeInfo typeInfo);

    /**
     * 批量保存试卷信息
     *
     * @param list 需要保存的数据
     * @return 失败的数据
     */
    List<PaperQuestion> savePaperQuestion(List<PaperQuestion> list, BiConsumer<Long, Integer> validatePaperInfo, Boolean isContinue);

    /**
     * 根据试卷ID 试卷类型 查询试题信息
     *
     * @param paperId  试卷ID
     * @param typeInfo 试卷类型
     * @return 关联信息集合
     */
    List<PaperQuestion> findByPaperIdAndType(Long paperId, PaperInfoEnum.TypeInfo typeInfo);

    /**
     * 解除 试卷-试题关联关系-单题
     *
     * @param paperId    试卷ID
     * @param typeInfo   试卷类型
     * @param questionId 试题ID
     * @return 操作成功数量
     */
    int deletePaperQuestionInfo(Long paperId, PaperInfoEnum.TypeInfo typeInfo, Long questionId);

    /**
     * 批量解除 试卷-试题关联关系-单题
     *
     * @param paperId        试卷ID
     * @param typeInfo       试卷类型
     * @param questionIdList 试题ID
     * @return 操作成功数量
     */
    int deletePaperQuestion(Long paperId, PaperInfoEnum.TypeInfo typeInfo, List<Long> questionIdList);

    /**
     * 解除 试卷-试题关联关系
     *
     * @param paperId  试卷ID
     * @param typeInfo 试卷类型
     * @return 操作成功数量
     */
    int deletePaperQuestionInfo(Long paperId, PaperInfoEnum.TypeInfo typeInfo);

    /**
     * @param paperId
     * @param typeInfo
     * @param sort
     * @return
     */
    boolean validateSort(Long paperId, PaperInfoEnum.TypeInfo typeInfo, Integer sort);

    /**
     * 根据模块信息修改试题分数
     *
     * @param paperId  试卷类型信息
     * @param typeInfo 试卷类型
     * @param moduleId 模块ID
     * @param score    分数
     * @return
     */
    int updateQuestionScoreByModuleId(Long paperId, PaperInfoEnum.TypeInfo typeInfo, Integer moduleId, Double score);

    /**
     * 查询试题的绑定关系
     *
     * @param questionId
     * @return
     */
    List<PaperQuestion> findByQuestionId(long questionId);

    /**
     * 单题算分,获取试卷总分
     * @param paperId
     * @param typeInfo
     * @return
     */
    Double getPaperQuestionScore(Long paperId, PaperInfoEnum.TypeInfo typeInfo);


    /**
     * 校验单题分数
     * @param score
     */
    void checkScore(double score);
}
