package com.huatu.tiku.teacher.service.question;

import com.huatu.tiku.response.question.v1.SelectQuestionRespV1;
import com.huatu.ztk.question.bean.Question;

import java.util.List;
import java.util.Map;

/**
 * 迁移数据专用service
 * Created by huangqp on 2018\6\25 0025.
 */
public interface SyncQuestionService {

    /**
     * 同步试题
     *
     * @param id
     */
    Long syncQuestion(Integer id);

    /**
     * 同步试题+试题绑定关系+模块信息
     *
     * @param questionId
     * @param paperId
     * @param sort
     * @param module
     */
    void  syncQuestion(Integer questionId, Long paperId, Integer sort, String module);

    /**
     * 批量查询试题信息
     *
     * @param questions 某一个模块下的试题（不包含所属的复合题信息）
     * @param movedIds  已经处理的试题id
     * @param sortMap   试题id->题序
     * @return
     */
    List<SelectQuestionRespV1> findQuestionByIds(List<Question> questions, List<Long> movedIds, Map<Integer, Integer> sortMap);


    /**
     * 对试卷下试题做去重操作
     *
     * @param questionId 去重试题id(mongo)
     * @param paperId    试卷id
     * @param sort       题序
     * @param module     模块名称
     * @param id         确认重复的试题id(已入库)
     */
    void duplicateQuestion(Integer questionId, Long paperId, Integer sort, String module, Long id);

    /**
     * 去重查询
     *
     * @param questionId
     * @param subjectFlag
     *@param yearFlag @return
     */
    Object findDuplicateQuestion(Integer questionId, Integer subjectFlag, Integer yearFlag);

    /**
     * 绑定试题试卷
     * @param questionId
     * @param paperId
     * @param sort
     * @param moduleName
     */
    void bindQuestion(int questionId, Long paperId, int sort, String moduleName);
}

