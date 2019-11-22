package com.huatu.tiku.teacher.service.question.v1;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.entity.duplicate.QuestionDuplicate;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.request.question.v1.InsertQuestionReqV1;
import com.huatu.tiku.request.question.v1.UpdateQuestionReqV1;
import com.huatu.tiku.response.question.v1.SelectQuestionRespV1;
import com.huatu.tiku.service.BaseService;
import com.huatu.ztk.question.bean.Question;

/**
 * Created by huangqp on 2018\4\24 0024.
 */
public interface QuestionServiceV1 extends BaseService<QuestionDuplicate> {

    /**
     * 修改各种题型的特有属性(复用属性)
     *
     * @param question
     */
    void updateQuestion(UpdateQuestionReqV1 question);

    /**
     * 添加各种题型特有的属性（复合题添加材料和复用数据，其他类型试题添加复用数据）
     *
     * @param insertQuestionReq
     * @return
     * @throws BizException
     */
    Object insertQuestion(InsertQuestionReqV1 insertQuestionReq) throws BizException;


    /**
     * 将旧库表中的试题转成insert对象实例
     *
     * @param question
     * @return
     */
    InsertQuestionReqV1 assertInsertReq(Question question);

    /**
     * 查询试题的回显数据
     *
     * @param question
     * @return
     */
    SelectQuestionRespV1 findQuestion(BaseQuestion question);

    /**
     * 删除试题跟复用数据之间的关系，如果是复合题，需要删除试题和材料之间的关系
     * 如果是复合题子题，也同样检查删除试题与材料之间的关系
     *
     * @param questionId
     */
    void deleteQuestion(Long questionId);

    /**
     * 试题信息转换为字符串（批量编辑使用）
     * @param question
     * @return
     */
    StringBuilder formatQuestionInfo(Question question);

    /**
     * 根据题型，分析试题文本内容，组装试题维护参数（批量导入功能）
     *
     * @param questionType
     * @param sb
     * @param subjectId
     * @param content
     * @return
     */
    SelectQuestionRespV1 assertQuestionReq(Long questionType, StringBuilder sb, Long subjectId, StringBuilder content);

    /**
     * 转化mongo格式的question为查询对象
     *
     * @param question
     * @return
     */
    SelectQuestionRespV1 convertMongoQuestion(Question question);

    /**
     * 根据题型查询是否有重题，并返回重题的复合题属性
     *  @param question
     * @param subjectFlag  0全部科目范围1question所属科目范围
     */
    Object findDuplicateQuestion(Question question, Integer subjectFlag);

    /**
     * 试题转换格式为mongo适用 (mysql->mongo)
     *
     * @param question mysql-question
     * @return mongo-question
     */
    Question parseQuestion2MongoInfo(BaseQuestion question);

}
