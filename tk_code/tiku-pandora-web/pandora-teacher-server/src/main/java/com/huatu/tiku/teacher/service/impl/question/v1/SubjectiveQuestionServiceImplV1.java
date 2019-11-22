package com.huatu.tiku.teacher.service.impl.question.v1;

import com.google.common.collect.Lists;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.constant.QuestionTailConstant;
import com.huatu.tiku.constants.teacher.TeacherErrors;
import com.huatu.tiku.entity.duplicate.QuestionDuplicate;
import com.huatu.tiku.entity.duplicate.SubjectiveDuplicatePart;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.request.question.v1.*;
import com.huatu.tiku.response.question.v1.SelectQuestionRespV1;
import com.huatu.tiku.response.question.v1.SelectSubjectiveQuestionRespV1;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.enums.DifficultyLevelEnum;
import com.huatu.tiku.teacher.enums.SubjectEnum;
import com.huatu.tiku.teacher.service.duplicate.SubjectiveDuplicatePartService;
import com.huatu.tiku.teacher.service.material.QuestionMaterialService;
import com.huatu.tiku.teacher.service.material.TeacherMaterialService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.tiku.teacher.service.question.v1.QuestionServiceV1;
import com.huatu.tiku.teacher.service.subject.TeacherSubjectService;
import com.huatu.tiku.util.file.HtmlFileUtil;
import com.huatu.tiku.util.html.HtmlConvertUtil;
import com.huatu.tiku.util.question.QuestionConvert;
import com.huatu.ztk.question.bean.GenericSubjectiveQuestion;
import com.huatu.ztk.question.bean.Question;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SubjectiveQuestionServiceImplV1 extends BaseServiceImpl<QuestionDuplicate> implements QuestionServiceV1 {

    @Autowired
    HtmlFileUtil htmlFileUtil;
    @Autowired
    SubjectiveDuplicatePartService subjectiveDuplicatePartService;
    @Autowired
    CommonQuestionServiceV1 teacherQuestionService;
    @Autowired
    TeacherMaterialService materialService;
    @Autowired
    QuestionMaterialService questionMaterialService;
    @Autowired
    TeacherSubjectService teacherSubjectService;

    public SubjectiveQuestionServiceImplV1() {
        super(QuestionDuplicate.class);
    }

    @Override
    @Transactional
    public void updateQuestion(UpdateQuestionReqV1 question) {
        UpdateCommonQuestionReqV1 updateCommonQuestionReqV1 = (UpdateCommonQuestionReqV1) question;
        UpdateSubjectiveQuestionReqV1 subjectiveQuestionReq = new UpdateSubjectiveQuestionReqV1();
        BeanUtils.copyProperties(updateCommonQuestionReqV1, subjectiveQuestionReq);
        checkUpdateQuestionStyle(subjectiveQuestionReq);

        subjectiveQuestionReq.setStem(htmlFileUtil.html2DB(subjectiveQuestionReq.getStem(), false));
        subjectiveQuestionReq.setAnswerComment(htmlFileUtil.html2DB(subjectiveQuestionReq.getAnswerComment(), true));
        subjectiveQuestionReq.setAnalyzeQuestion(htmlFileUtil.html2DB(subjectiveQuestionReq.getAnalyzeQuestion(), true));
        subjectiveQuestionReq.setExtend(htmlFileUtil.html2DB(subjectiveQuestionReq.getExtend(), true));

        //复用表信息处理(如果没有复用id，直接添加，否则不动复用数据（添加试题的时候不允许复用数据修改）)

        SubjectiveDuplicatePart subjectiveDuplicatePart = new SubjectiveDuplicatePart();
        BeanUtils.copyProperties(subjectiveQuestionReq, subjectiveDuplicatePart);
        if (subjectiveQuestionReq.getDuplicateId() == null || subjectiveQuestionReq.getDuplicateId() <= 0) {
            //插入主观题目表
            subjectiveDuplicatePartService.insertWithFilter(subjectiveDuplicatePart);
            subjectiveQuestionReq.setDuplicateId(subjectiveDuplicatePart.getId());
        } else {
            subjectiveDuplicatePart.setId(subjectiveQuestionReq.getDuplicateId());
            subjectiveDuplicatePartService.updateWithFilter(subjectiveDuplicatePart);
        }
        //duplicateId 保存表的类型
        QuestionDuplicate questionDuplicate = QuestionDuplicate.builder().duplicateType(QuestionInfoEnum.QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE.getCode()).duplicateId(subjectiveQuestionReq.getDuplicateId()).questionId(subjectiveQuestionReq.getId()).build();
        Example example = new Example(QuestionDuplicate.class);
        example.and().andEqualTo("questionId", question.getId());
        //试题复用表关联关系，绑定关联关系
        updateByExampleSelective(questionDuplicate, example);
        List<Long> materialIds = question.getMaterialIds();
        if (CollectionUtils.isNotEmpty(materialIds)) {
            materialService.saveQuestionMaterialBindings(materialIds, question.getId());
        }
    }

    /**
     * 修改试题参数检查
     *
     * @param subjectiveQuestionReq
     */
    private void checkUpdateQuestionStyle(UpdateSubjectiveQuestionReqV1 subjectiveQuestionReq) {
        if (StringUtils.isBlank(subjectiveQuestionReq.getStem())) {
            throwBizException("题干不能为空");
        }
        if (StringUtils.isBlank(subjectiveQuestionReq.getAnswerComment())) {
            throwBizException("参考答案不能为空");
        }
        if (StringUtils.isBlank(subjectiveQuestionReq.getAnalyzeQuestion())) {
            if (SubjectEnum.isExist(subjectiveQuestionReq.getSubject().intValue()) == false)
                throwBizException("试题分析不能为空");
        }
    }

    @Override
    @Transactional
    public Object insertQuestion(InsertQuestionReqV1 insertQuestionReq) throws BizException {
        //对象转换、格式判断
        InsertCommonQuestionReqV1 insertCommonQuestionReqV1 = (InsertCommonQuestionReqV1) insertQuestionReq;
        InsertSubjectiveQuestionReqV1 subjectiveQuestionReq = new InsertSubjectiveQuestionReqV1();
        BeanUtils.copyProperties(insertCommonQuestionReqV1, subjectiveQuestionReq);
        checkInsertQuestionStyle(subjectiveQuestionReq);
        //处理文本中的图片格式
        subjectiveQuestionReq.setStem(htmlFileUtil.html2DB(subjectiveQuestionReq.getStem(), false));
        subjectiveQuestionReq.setAnswerComment(htmlFileUtil.html2DB(subjectiveQuestionReq.getAnswerComment(), true));
        subjectiveQuestionReq.setAnalyzeQuestion(htmlFileUtil.html2DB(subjectiveQuestionReq.getAnalyzeQuestion(), true));
        subjectiveQuestionReq.setExtend(htmlFileUtil.html2DB(subjectiveQuestionReq.getExtend(), true));

        if (subjectiveQuestionReq.getDuplicateId() == null) {
            SubjectiveDuplicatePart subjectiveDuplicatePart = new SubjectiveDuplicatePart();
            BeanUtils.copyProperties(subjectiveQuestionReq, subjectiveDuplicatePart);

            //插入主观题目表（question_duplicate_subjective）
            subjectiveDuplicatePartService.insertWithFilter(subjectiveDuplicatePart);
            subjectiveQuestionReq.setDuplicateId(subjectiveDuplicatePart.getId());
        }

        //type=主管题目，插入question_duplicate_relation绑定关系
        QuestionDuplicate questionDuplicate = QuestionDuplicate.builder().duplicateType(QuestionInfoEnum.QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE.getCode()).duplicateId(subjectiveQuestionReq.getDuplicateId()).
                questionId(subjectiveQuestionReq.getId()).build();

        this.insert(questionDuplicate);
        //材料添加
        List<Long> materialIds = insertQuestionReq.getMaterialIds();
        if (CollectionUtils.isNotEmpty(materialIds)) {
            materialService.saveQuestionMaterialBindings(materialIds, insertQuestionReq.getId());
        }
        return null;
    }

    /**
     * 判断添加参数问题
     */
    private void checkInsertQuestionStyle(InsertSubjectiveQuestionReqV1 subjectiveQuestionReq) {
        if (StringUtils.isBlank(subjectiveQuestionReq.getStem())) {
            throwBizException("题干不能为空");
        }
        if (StringUtils.isBlank(subjectiveQuestionReq.getAnswerComment())) {
            throwBizException("参考答案不能为空");
        }
        if (StringUtils.isBlank(subjectiveQuestionReq.getAnalyzeQuestion())) {
            if (SubjectEnum.isExist(subjectiveQuestionReq.getSubject().intValue()) == false)
                throwBizException("试题分析不能为空");
        }
    }


    @Override
    public InsertQuestionReqV1 assertInsertReq(Question question) {
        if (!(question instanceof GenericSubjectiveQuestion)) {
            log.error("question not match GenericQuestion:>>>{}", question);
            throw new BizException(TeacherErrors.NOT_MATCH_QUESTION_SAVE_TYPE);
        }
        GenericSubjectiveQuestion subjectiveQuestion = (GenericSubjectiveQuestion) question;
        InsertSubjectiveQuestionReqV1 insertQuestionReq = new InsertSubjectiveQuestionReqV1();
        insertQuestionReq.setStem(subjectiveQuestion.getStem());
        if (StringUtils.isNotEmpty(subjectiveQuestion.getScoreExplain())) {
            insertQuestionReq.setAnalyzeQuestion(subjectiveQuestion.getScoreExplain());
        } else {
            insertQuestionReq.setAnalyzeQuestion(subjectiveQuestion.getSolvingIdea());
        }
        insertQuestionReq.setAnswerComment(subjectiveQuestion.getReferAnalysis());
        insertQuestionReq.setAnalyzeQuestion(subjectiveQuestion.getReferAnalysis());
        insertQuestionReq.setAnswerRequest(subjectiveQuestion.getAnswerRequire());
        insertQuestionReq.setTrainThought(subjectiveQuestion.getSolvingIdea());
        insertQuestionReq.setId(new Long(subjectiveQuestion.getId()));
        insertQuestionReq.setQuestionType(subjectiveQuestion.getType());
        insertQuestionReq.setMultiId(new Long(subjectiveQuestion.getParent()));
        insertQuestionReq.setDifficultyLevel(subjectiveQuestion.getDifficult());
        insertQuestionReq.setSubject(new Long(subjectiveQuestion.getSubject()));
        insertQuestionReq.setKnowledgeIds(Lists.newArrayList());
        insertQuestionReq.setStatus(question.getStatus() == 4 ? -1 : 1);
        insertQuestionReq.setCreatorId(question.getCreateBy());
        return insertQuestionReq;
    }

    /**
     * 查询题目信息
     *
     * @param baseQuestion
     * @return
     */
    @Override
    public SelectQuestionRespV1 findQuestion(BaseQuestion baseQuestion) {

        // 判断是否是主观类的题目
        Long id = baseQuestion.getId();
        if (!QuestionInfoEnum.QuestionSaveTypeEnum.SUBJECTIVE.equals(QuestionInfoEnum.getSaveTypeByQuestionType(baseQuestion.getQuestionType()))) {
            throw new BizException(ErrorResult.create(1000010, "主观类题目不能为空"));
        }

        SelectSubjectiveQuestionRespV1 selectSubjectiveQuestionResp = new SelectSubjectiveQuestionRespV1();
        //查找（question_duplicate_relation绑定关系表）
        Example example = new Example(QuestionDuplicate.class);
        example.and().andEqualTo("questionId", id);
        List<QuestionDuplicate> questionDuplicates = selectByExample(example);
        //题目不存在
        if (CollectionUtils.isEmpty(questionDuplicates)) {
            throw new BizException(TeacherErrors.NO_EXISTED_QUESTION);
        }
        QuestionDuplicate judgeQuestion = questionDuplicates.get(0);
        BeanUtils.copyProperties(judgeQuestion, selectSubjectiveQuestionResp);

        // 查询主观题表内容（question_duplicate_subjective）
        SubjectiveDuplicatePart subjectiveDuplicatePart = subjectiveDuplicatePartService.selectByPrimaryKey(judgeQuestion.getDuplicateId());
        if (subjectiveDuplicatePart != null) {
            BeanUtils.copyProperties(subjectiveDuplicatePart, selectSubjectiveQuestionResp);
        }
        BeanUtils.copyProperties(baseQuestion, selectSubjectiveQuestionResp);
        selectSubjectiveQuestionResp.setId(baseQuestion.getId());
        if (baseQuestion.getMultiId() > 0L) {
            List<Long> materialIds = materialService.findMaterialIdsByQuestion(baseQuestion);
            selectSubjectiveQuestionResp.setMaterialIds(materialIds);
        }
        //补充查询，附加所有试题关联信息
        teacherQuestionService.assertQuestionAttrs(selectSubjectiveQuestionResp);
        return selectSubjectiveQuestionResp;
    }

    @Override
    public void deleteQuestion(Long questionId) {
        Example example = new Example(QuestionDuplicate.class);
        example.and().andEqualTo("questionId", questionId);
        deleteByExample(example);
        questionMaterialService.deleteByQuestionId(questionId);
    }

    @Override
    public StringBuilder formatQuestionInfo(Question question) {
        StringBuilder content = new StringBuilder("");
        Integer questionType = question.getType();
        if (question instanceof GenericSubjectiveQuestion) {
            GenericSubjectiveQuestion genericSubjectiveQuestion = (GenericSubjectiveQuestion) question;
            content.append(QuestionConvert.convertStemBefore(QuestionInfoEnum.QuestionTypeEnum.create(questionType).getValue(), QuestionConvert.htmlConvertContent(genericSubjectiveQuestion.getStem())));
            content.append(QuestionConvert.convertAnswer(QuestionConvert.htmlConvertContent(genericSubjectiveQuestion.getReferAnalysis())));
            content.append(QuestionConvert.convertAnalysis(QuestionConvert.htmlConvertContent(genericSubjectiveQuestion.getAnswerRequire())));
            content.append(QuestionConvert.convertExtend(QuestionConvert.htmlConvertContent("")));
        }
        return content;
    }

    @Override
    public SelectSubjectiveQuestionRespV1 assertQuestionReq(Long questionType, StringBuilder sb, Long subjectId, StringBuilder content) {
        SelectSubjectiveQuestionRespV1 selectSubjectiveQuestionReqV1 = new SelectSubjectiveQuestionRespV1();
        selectSubjectiveQuestionReqV1.setQuestionType(questionType.intValue());
        sb = new StringBuilder(sb.toString().trim());
        //1、题干->答案
        String stem = HtmlConvertUtil.getContent(sb, "题干", "", QuestionTailConstant.QUESTION_TO_ANSWER);
        selectSubjectiveQuestionReqV1.setStem("<p>" + stem + "</p>");
        content.append(QuestionConvert.convertStem(stem));
        //2、答案->解析
        String answerDetail = HtmlConvertUtil.getContent(sb, "答案", QuestionTailConstant.QUESTION_TO_ANSWER, QuestionTailConstant.QUESTION_TO_ANALYSIS);
        selectSubjectiveQuestionReqV1.setAnswerComment(answerDetail);
        content.append(QuestionConvert.convertAnswer(answerDetail));
        //3、解析->拓展
        String analysis = HtmlConvertUtil.getContent(sb, "解析", QuestionTailConstant.QUESTION_TO_ANALYSIS, QuestionTailConstant.QUESTION_TO_EXTEND);
        selectSubjectiveQuestionReqV1.setAnalyzeQuestion("<p>" + analysis.trim() + "</p>");
        content.append(QuestionConvert.convertAnalysis(analysis.trim()));
        //4、拓展->标签
        String extend = HtmlConvertUtil.getContent(sb, "拓展", QuestionTailConstant.QUESTION_TO_EXTEND, QuestionTailConstant.QUESTION_TO_TAG);
        selectSubjectiveQuestionReqV1.setExtend("<p>" + extend.trim() + "</p>");
        content.append(QuestionConvert.convertExtend(extend.trim()));
        //5、标签、知识点、难度
        teacherQuestionService.assertQuestionCommonReq(selectSubjectiveQuestionReqV1, sb, subjectId, content);
        return selectSubjectiveQuestionReqV1;
    }

    @Override
    public SelectQuestionRespV1 convertMongoQuestion(Question question) {
        GenericSubjectiveQuestion genericSubjectiveQuestion = (GenericSubjectiveQuestion) question;
        SelectSubjectiveQuestionRespV1 questionRespV1 = new SelectSubjectiveQuestionRespV1();
        questionRespV1.setDifficult(DifficultyLevelEnum.create(genericSubjectiveQuestion.getDifficult()).getTitle());
        questionRespV1.setDifficultyLevel(genericSubjectiveQuestion.getDifficult());
        questionRespV1.setMultiId(new Long(genericSubjectiveQuestion.getParent()));
        questionRespV1.setStem(genericSubjectiveQuestion.getStem());
        questionRespV1.setAnalyzeQuestion(genericSubjectiveQuestion.getReferAnalysis());
        questionRespV1.setAnswerComment(genericSubjectiveQuestion.getReferAnalysis());
        questionRespV1.setAnswerRequest(genericSubjectiveQuestion.getAnswerRequire());
        questionRespV1.setBestowPointExplain(genericSubjectiveQuestion.getScoreExplain());
        questionRespV1.setTrainThought(genericSubjectiveQuestion.getSolvingIdea());
        questionRespV1.setKnowledgeIds(Lists.newArrayList());
        return questionRespV1;
    }

    @Override
    public Object findDuplicateQuestion(Question question, Integer subjectFlag) {
        if (question instanceof GenericSubjectiveQuestion) {
            return teacherQuestionService.findDuplicatePart("", ((GenericSubjectiveQuestion) question).getStem(),
                    "", "", "", "",
                    "", "",
                    "", "", question.getType());
        }
        return null;
    }

    @Override
    public Question parseQuestion2MongoInfo(BaseQuestion question) {
        HashMap<String, Object> mapData = subjectiveDuplicatePartService.findByQuestionId(question.getId());
        GenericSubjectiveQuestion genericSubjectiveQuestion = new GenericSubjectiveQuestion();
        //转换数据格式
        genericSubjectiveQuestion.setStem(HtmlConvertUtil.getContentFromMap(mapData, "stem"));
        genericSubjectiveQuestion.setScoreExplain(HtmlConvertUtil.getContentFromMap(mapData, "analyzeQuestion"));
        genericSubjectiveQuestion.setReferAnalysis(HtmlConvertUtil.getContentFromMap(mapData, "answerComment"));
        genericSubjectiveQuestion.setAnswerRequire(HtmlConvertUtil.getContentFromMap(mapData, "referAnalysis"));
        genericSubjectiveQuestion.setSolvingIdea(HtmlConvertUtil.getContentFromMap(mapData, "trainThought"));
        genericSubjectiveQuestion.setRequire(HtmlConvertUtil.getContentFromMap(mapData, "answerRequire"));
        genericSubjectiveQuestion.setExtend(HtmlConvertUtil.getContentFromMap(mapData, "extend"));
        genericSubjectiveQuestion.setParent(question.getMultiId().intValue());
        if (question.getMultiId() > 0) {
            List<String> materials = materialService.findByQuestionId(question.getMultiId()).stream()
                    .map(i -> i.getContent()).map(i -> HtmlConvertUtil.span2Img(i, true)).collect(Collectors.toList());
            genericSubjectiveQuestion.setMaterial(StringUtils.join(materials, "</br>"));
            genericSubjectiveQuestion.setMaterials(materials);
        }
        return genericSubjectiveQuestion;
    }

}
