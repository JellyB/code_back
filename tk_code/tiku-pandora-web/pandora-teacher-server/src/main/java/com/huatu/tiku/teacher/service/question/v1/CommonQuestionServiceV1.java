package com.huatu.tiku.teacher.service.question.v1;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.request.question.v1.InsertQuestionReqV1;
import com.huatu.tiku.request.question.v1.UpdateQuestionReqV1;
import com.huatu.tiku.response.question.DuplicatePartResp;
import com.huatu.tiku.response.question.v1.SelectQuestionRespV1;
import com.huatu.tiku.service.BaseService;
import com.huatu.ztk.question.bean.Question;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 试题mysql数据处理服务
 * Created by huangqp on 2018\6\25 0025.
 */
public interface CommonQuestionServiceV1 extends BaseService<BaseQuestion> {

    /**
     * selectQuestionResp 补充查询（补充题目属性：科目，地区年份，来源，标签，知识点,难度名称）
     *
     * @param selectQuestionResp
     */
    void assertQuestionAttrs(SelectQuestionRespV1 selectQuestionResp);


    /**
     * 根据试题科目选择service实现类
     *
     * @param saveType
     * @return
     */
    QuestionServiceV1 choiceService(QuestionInfoEnum.QuestionSaveTypeEnum saveType);

    /**
     * 插入主表数据
     *
     * @param baseQuestion
     * @return
     */
    Long insertQuestion(BaseQuestion baseQuestion);


    /**
     * 删除试题(无连带删除)
     *
     * @param questionId 试题id
     * @param modifierId
     * @return
     * @throws BizException
     */
    Object deleteQuestion(Long questionId, Long modifierId,Boolean isDuplicateFlag) throws BizException;

    /**
     * 修改试题
     *
     * @param updateQuestionReq
     * @return
     * @throws BizException
     */
    Object updateQuestion(UpdateQuestionReqV1 updateQuestionReq) throws BizException;

    /**
     * 删除试题，根据连带关系
     *
     * @param questionId
     * @param modifierId
     * @param copyFlag   如果是true表示删除连带关系的题，即同二级科目下的重复题目，如果是false表示只删除自己题号相关的属性
     * @return
     */
    Object deleteQuestionByFlag(Long questionId, Long modifierId, Boolean copyFlag);


    /**
     * 添加试题
     *
     * @param insertQuestionReq
     */
    Map insertQuestion(InsertQuestionReqV1 insertQuestionReq);

    /**
     * 查询试题信息（修改回显）
     *
     * @param questionId
     * @param withParent 作为子题的时候，是否携带复合题属性
     * @return
     */
    SelectQuestionRespV1 findQuestionInfo(Long questionId, boolean withParent);


    /**
     * span2image
     *
     * @param question
     */
    void convertQuestionSpan2Img(SelectQuestionRespV1 question);

    /**
     * 查询复合题下子题信息（修改回显）
     *
     * @param parentId
     * @return
     */
    List<SelectQuestionRespV1> findChildren(Long parentId);

    /**
     * 删除试题（物理删除，数据重复迁移，先删再加，避免mysql唯一性报错）(迁移专用)
     *
     * @param id
     */
    void deleteQuestionPhysical(Integer id);

    /**
     * 修改试题发布/未发布状态
     *
     * @param questionId
     * @param status
     * @return
     */
    Object updateQuestionBizStatus(Long questionId, Integer status);

    /**
     * 试题作废和取消作废状态
     *
     * @param questionId
     * @param availFlag
     * @return
     */
    Object updateQuestionAvailable(Long questionId, QuestionInfoEnum.AvailableEnum availFlag);

    /**
     * 修改试题残缺和完整状态
     *
     * @param questionId
     * @param missFlag   残缺枚举
     * @return
     */
    Object updateQuestionStatus(Long questionId, QuestionInfoEnum.CompleteEnum missFlag);

    /**
     * 根据题型做去重查询
     *
     * @param choices
     * @param stem
     * @param analysis
     * @param extend
     * @param answerComment
     * @param analyzeQuestion
     * @param answerRequest
     * @param bestowPointExplain
     * @param trainThought
     * @param omnibusRequirements
     * @param questionType
     * @return 复用内容
     */
    List<DuplicatePartResp> findDuplicatePart(String choices, String stem, String analysis, String extend, String answerComment, String analyzeQuestion, String answerRequest, String bestowPointExplain, String trainThought, String omnibusRequirements, Integer questionType);

    /**
     * 根据各种题型的格式解析试题录入文本
     *
     * @param text
     * @param subjectId
     * @return
     */
    Object parseQuestionInfo(String text, Long subjectId,Long questionId);

    /**
     * 分析试题共有属性（标签，知识点，难度）
     *
     * @param selectQuestionRespV1
     * @param sb
     * @param subjectId
     * @param content
     */
    void assertQuestionCommonReq(SelectQuestionRespV1 selectQuestionRespV1, StringBuilder sb, Long subjectId, StringBuilder content);

    /**
     * 将试题格式从mongo转为pandora适用的返回数据
     *
     * @param question
     * @return
     */
    SelectQuestionRespV1 convertQuestionMongo2DB(Question question);

    /**
     * 通过mongo存储的question对象，查询再mysql中是否有重题（数据迁移时使用）
     *
     * @param question
     * @param subjectFlag
     */
    Object findDuplicateQuestion(Question question, Integer subjectFlag, Integer yearFlag);

    /**
     * 试题数据转为mongo格式
     *
     * @param questionId 试题id
     * @return mongo格式试题对象
     */
    Question parseQuestion2Mongo(long questionId);

    /**
     * 查询每个科目下的试题数量
     *
     * @return
     */
    List<Map<String, Long>> countQuestionGroupBySubject();

    /**
     * 批量修改试题发布状态
     *
     * @param questionIds
     * @param status
     * @return
     */
    Object updateQuestionBizStatusBatch(List<Long> questionIds, Integer status);

    /**
     * 去重操作
     *
     * @param newId 替换ID
     * @param oldId 被替换的ID
     * @return
     */
    List<String> clearDuplicate(long newId, long oldId);


    /**
     * 根据ID查询试题信息，并组装显示信息
     *
     * @param questionIds
     * @return
     */
    List<BaseQuestion> findByIds(List<Long> questionIds);

    /**
     * 将试题的属性拼接成试题字符串（批量编辑回显使用）
     *
     * @param questionId
     * @return
     */
    String formatQuestionInfo(Long questionId);

    void findAndHandlerQuestion(Consumer<List<Question>> consumer,int subject);
}
