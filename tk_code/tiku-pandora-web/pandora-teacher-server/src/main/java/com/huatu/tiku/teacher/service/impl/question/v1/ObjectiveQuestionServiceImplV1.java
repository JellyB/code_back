package com.huatu.tiku.teacher.service.impl.question.v1;

import com.google.common.collect.Lists;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.constant.QuestionTailConstant;
import com.huatu.tiku.constants.teacher.TeacherErrors;
import com.huatu.tiku.entity.duplicate.ObjectiveDuplicatePart;
import com.huatu.tiku.entity.duplicate.QuestionDuplicate;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.request.question.v1.*;
import com.huatu.tiku.response.question.DuplicatePartResp;
import com.huatu.tiku.response.question.v1.SelectObjectiveQuestionRespV1;
import com.huatu.tiku.response.question.v1.SelectQuestionRespV1;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.enums.DifficultyLevelEnum;
import com.huatu.tiku.teacher.service.duplicate.ObjectiveDuplicatePartService;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import com.huatu.tiku.teacher.service.material.QuestionMaterialService;
import com.huatu.tiku.teacher.service.material.TeacherMaterialService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.tiku.teacher.service.question.v1.QuestionServiceV1;
import com.huatu.tiku.util.file.HtmlFileUtil;
import com.huatu.tiku.util.html.HtmlConvertUtil;
import com.huatu.tiku.util.question.QuestionConvert;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\6\27 0027.
 */
@Slf4j
@Service
public class ObjectiveQuestionServiceImplV1 extends BaseServiceImpl<QuestionDuplicate> implements QuestionServiceV1 {
    @Autowired
    HtmlFileUtil htmlFileUtil;
    @Autowired
    ObjectiveDuplicatePartService objectiveDuplicatePartService;
    @Autowired
    CommonQuestionServiceV1 commonQuestionServiceV1;
    @Autowired
    TeacherMaterialService materialService;
    @Autowired
    QuestionMaterialService questionMaterialService;
    @Autowired
    KnowledgeService knowledgeService;

    public ObjectiveQuestionServiceImplV1() {
        super(QuestionDuplicate.class);
    }

    @Override
    @Transactional
    public void updateQuestion(UpdateQuestionReqV1 question) {
        UpdateCommonQuestionReqV1 updateCommonQuestionReqV1 = (UpdateCommonQuestionReqV1) question;
        UpdateObjectiveQuestionReqV1 objectiveQuestionRep = new UpdateObjectiveQuestionReqV1();
        BeanUtils.copyProperties(updateCommonQuestionReqV1, objectiveQuestionRep);
        checkUpdateQuestionStyle(objectiveQuestionRep);

        objectiveQuestionRep.setChoices(objectiveQuestionRep.getChoices().stream().map(i -> htmlFileUtil.html2DB(i, false)).collect(Collectors.toList()));
        objectiveQuestionRep.setAnalysis(htmlFileUtil.html2DB(objectiveQuestionRep.getAnalysis(), true));
        objectiveQuestionRep.setStem(htmlFileUtil.html2DB(objectiveQuestionRep.getStem(), false));
        objectiveQuestionRep.setExtend(htmlFileUtil.html2DB(objectiveQuestionRep.getExtend(), true));
        char[] chars = objectiveQuestionRep.getAnswer().toCharArray();
        Arrays.sort(chars);
        objectiveQuestionRep.setAnswer(String.valueOf(chars));
        //复用表信息处理(如果没有复用id，直接添加，否则修改原数据)
        ObjectiveDuplicatePart objectiveDuplicatePart = new ObjectiveDuplicatePart();
        BeanUtils.copyProperties(objectiveQuestionRep, objectiveDuplicatePart);
        objectiveDuplicatePart.setChoices(HtmlConvertUtil.assertChoicesContent(objectiveQuestionRep.getChoices()));
        if (objectiveQuestionRep.getDuplicateId() == null || objectiveQuestionRep.getDuplicateId() <= 0) {
            objectiveDuplicatePartService.insertWithFilter(objectiveDuplicatePart);
            objectiveQuestionRep.setDuplicateId(objectiveDuplicatePart.getId());
        } else {
            objectiveDuplicatePart.setId(objectiveQuestionRep.getDuplicateId());
            objectiveDuplicatePartService.updateWithFilter(objectiveDuplicatePart);
        }
        QuestionDuplicate questionDuplicate = QuestionDuplicate.builder().duplicateType(1).duplicateId(objectiveQuestionRep.getDuplicateId()).questionId(objectiveQuestionRep.getId()).build();
        Example example = new Example(QuestionDuplicate.class);
        example.and().andEqualTo("questionId", question.getId());
        //试题复用表关联关系
        updateByExampleSelective(questionDuplicate, example);
        //材料添加
        List<Long> materialIds = question.getMaterialIds();
        if (CollectionUtils.isNotEmpty(materialIds)) {
            materialService.saveQuestionMaterialBindings(materialIds, question.getId());
        }
    }

    /**
     * 修改试题参数判断
     *
     * @param objectiveQuestionRep
     */
    private void checkUpdateQuestionStyle(UpdateObjectiveQuestionReqV1 objectiveQuestionRep) {
        if (CollectionUtils.isEmpty(objectiveQuestionRep.getChoices())) {
            throwBizException("选项内容不能为空");
        }
        if (StringUtils.isBlank(objectiveQuestionRep.getAnswer())) {
            throwBizException("答案不能为空");
        }
        if (StringUtils.isBlank(objectiveQuestionRep.getStem())) {
            throwBizException("题干不能为空");
        }
        if (StringUtils.isBlank(objectiveQuestionRep.getAnalysis())) {
            throwBizException("解析不能为空");
        }
    }

    /**
     * 添加选择题的复用部分数据
     *
     * @param insertQuestionReq
     * @return
     * @throws BizException
     */
    @Override
    @Transactional
    public Object insertQuestion(InsertQuestionReqV1 insertQuestionReq) throws BizException {
        //对象转换、格式判断
        InsertCommonQuestionReqV1 insertCommonQuestionReqV1 = (InsertCommonQuestionReqV1) insertQuestionReq;
        InsertObjectiveQuestionReqV1 objectiveQuestionRep = new InsertObjectiveQuestionReqV1();
        BeanUtils.copyProperties(insertCommonQuestionReqV1, objectiveQuestionRep);
        checkInsertQuestionStyle(objectiveQuestionRep);
        //内容格式转换
        objectiveQuestionRep.setChoices(objectiveQuestionRep.getChoices().stream().map(i -> htmlFileUtil.html2DB(i, false)).collect(Collectors.toList()));
        objectiveQuestionRep.setAnalysis(htmlFileUtil.html2DB(objectiveQuestionRep.getAnalysis(), true));
        objectiveQuestionRep.setStem(htmlFileUtil.html2DB(objectiveQuestionRep.getStem(), false));
        objectiveQuestionRep.setExtend(htmlFileUtil.html2DB(objectiveQuestionRep.getExtend(), true));
        char[] chars = objectiveQuestionRep.getAnswer().toCharArray();
        Arrays.sort(chars);
        objectiveQuestionRep.setAnswer(String.valueOf(chars));
        //复用表信息处理(如果没有复用id，直接添加，否则不动复用数据（添加试题的时候不允许复用数据修改）)
        if (objectiveQuestionRep.getDuplicateId() == null || objectiveQuestionRep.getDuplicateId() <= 0) {
            ObjectiveDuplicatePart objectiveDuplicatePart = new ObjectiveDuplicatePart();
            BeanUtils.copyProperties(objectiveQuestionRep, objectiveDuplicatePart);
            objectiveDuplicatePart.setChoices(HtmlConvertUtil.assertChoicesContent(objectiveQuestionRep.getChoices()));
            //保存复用ID
            objectiveDuplicatePartService.insertWithFilter(objectiveDuplicatePart);
            objectiveQuestionRep.setDuplicateId(objectiveDuplicatePart.getId());
        }
        QuestionDuplicate questionDuplicate = QuestionDuplicate.builder().duplicateType(1).duplicateId(objectiveQuestionRep.getDuplicateId()).questionId(objectiveQuestionRep.getId()).build();
        //试题复用表关联关系
        insert(questionDuplicate);
        //材料添加
        List<Long> materialIds = insertQuestionReq.getMaterialIds();
        if (CollectionUtils.isNotEmpty(materialIds)) {
            materialService.saveQuestionMaterialBindings(materialIds, insertQuestionReq.getId());
        }
        return null;
    }

    /**
     * 判断参数
     *
     * @param insertObjectiveQuestionReqV1
     */
    private void checkInsertQuestionStyle(InsertObjectiveQuestionReqV1 insertObjectiveQuestionReqV1) {
        if (CollectionUtils.isEmpty(insertObjectiveQuestionReqV1.getChoices())) {
            log.error("解析不能为空:{}", insertObjectiveQuestionReqV1.getId());
            throwBizException("选项内容不能为空");
        }
        if (StringUtils.isBlank(insertObjectiveQuestionReqV1.getAnswer())) {
            log.error("解析不能为空:{}", insertObjectiveQuestionReqV1.getId());
            throwBizException("答案不能为空");
        }
        if (StringUtils.isBlank(insertObjectiveQuestionReqV1.getStem())) {
            log.error("解析不能为空:{}", insertObjectiveQuestionReqV1.getId());
            throwBizException("题干不能为空");
        }
        if (StringUtils.isBlank(insertObjectiveQuestionReqV1.getAnalysis())) {
            log.error("解析不能为空:{}", insertObjectiveQuestionReqV1.getId());
            throwBizException("解析不能为空");
        }

    }

    /**
     * 将mongo中的question解析成插入数据模式
     *
     * @param question
     * @return
     */
    @Override
    public InsertQuestionReqV1 assertInsertReq(Question question) {
        if (!(question instanceof GenericQuestion)) {
            log.error("question not match GenericQuestion:>>>{}", question);
            throw new BizException(TeacherErrors.NOT_MATCH_QUESTION_SAVE_TYPE);
        }
        GenericQuestion genericQuestion = (GenericQuestion) question;
        Long knowledgeId = new Long(genericQuestion.getPoints().get(2));
        InsertObjectiveQuestionReqV1 insertQuestionReq = new InsertObjectiveQuestionReqV1();
        insertQuestionReq.setStem(genericQuestion.getStem());
        insertQuestionReq.setAnalysis(genericQuestion.getAnalysis());
        insertQuestionReq.setAnswer(int2CharAnswer(genericQuestion.getAnswer()));
        insertQuestionReq.setChoices(genericQuestion.getChoices());
        insertQuestionReq.setId(new Long(genericQuestion.getId()));
        insertQuestionReq.setQuestionType(genericQuestion.getType());
        insertQuestionReq.setMultiId(new Long(genericQuestion.getParent()));
        insertQuestionReq.setDifficultyLevel(genericQuestion.getDifficult());
        insertQuestionReq.setSubject(new Long(genericQuestion.getSubject()));
        insertQuestionReq.setKnowledgeIds(Lists.newArrayList(knowledgeService.transKnowledgeId(knowledgeId, genericQuestion.getPoints().get(1).longValue())));
        insertQuestionReq.setStatus(question.getStatus() == 4 ? -1 : 1);
        insertQuestionReq.setCreatorId(question.getCreateBy());
        insertQuestionReq.setExtend(((GenericQuestion) question).getExtend());
        return insertQuestionReq;
    }

    /**
     * 将mongo选项答案1-9转为字符串A-Z
     *
     * @param answer
     * @return
     */
    private static String int2CharAnswer(int answer) {
        String target = String.valueOf(answer);
        for (int i = 1; i < 10; i++) {
            target = target.replace(i + "", (char) ('A' + i - 1) + "");
        }
        return target;
    }

    @Override
    public SelectQuestionRespV1 findQuestion(BaseQuestion baseQuestion) {
        Long id = baseQuestion.getId();
        if (!QuestionInfoEnum.QuestionSaveTypeEnum.OBJECTIVE.equals(QuestionInfoEnum.getSaveTypeByQuestionType(baseQuestion.getQuestionType()))) {
            throw new BizException(ErrorResult.create(1000010, "查询的试题不是选择类的题目"));
        }
        SelectObjectiveQuestionRespV1 selectObjectiveQuestionResp = new SelectObjectiveQuestionRespV1();
        QuestionDuplicate duplicate = selectByQuestionId(id);
        if (duplicate == null) {
            throw new BizException(TeacherErrors.NO_EXISTED_QUESTION);
        }
        BeanUtils.copyProperties(duplicate, selectObjectiveQuestionResp);
        ObjectiveDuplicatePart objectiveDuplicatePart = objectiveDuplicatePartService.selectByPrimaryKey(duplicate.getDuplicateId());
        if (objectiveDuplicatePart != null) {
            BeanUtils.copyProperties(objectiveDuplicatePart, selectObjectiveQuestionResp);
            selectObjectiveQuestionResp.setChoices(HtmlConvertUtil.parseChoices(objectiveDuplicatePart.getChoices()));
        }
        BeanUtils.copyProperties(baseQuestion, selectObjectiveQuestionResp);
        selectObjectiveQuestionResp.setId(baseQuestion.getId());
        if (baseQuestion.getMultiId() > 0L) {
            List<Long> materialIds = materialService.findMaterialIdsByQuestion(baseQuestion);
            selectObjectiveQuestionResp.setMaterialIds(materialIds);
        }
        commonQuestionServiceV1.assertQuestionAttrs(selectObjectiveQuestionResp);
        return selectObjectiveQuestionResp;
    }

    @Override
    @Transactional
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
        if (question instanceof GenericQuestion) {
            GenericQuestion genericQuestion = (GenericQuestion) question;
            content.append(QuestionConvert.convertStemBefore(QuestionInfoEnum.QuestionTypeEnum.create(questionType).getValue(), QuestionConvert.htmlConvertContent(genericQuestion.getStem())));
            List<String> choices = genericQuestion.getChoices().stream().map(QuestionConvert::htmlConvertContent).collect(Collectors.toList());
            content.append(QuestionConvert.convertChoice(choices));
            content.append(QuestionConvert.convertAnswer(int2CharAnswer(genericQuestion.getAnswer())));
            content.append(QuestionConvert.convertAnalysis(QuestionConvert.htmlConvertContent(genericQuestion.getAnalysis())));
            content.append(QuestionConvert.convertExtend(QuestionConvert.htmlConvertContent(genericQuestion.getExtend())));
        }
        return content;
    }

    /**
     * 批量录题分析试题内容
     *
     * @param questionType
     * @param sb
     * @param subjectId
     * @param content
     * @return
     */
    @Override
    public SelectObjectiveQuestionRespV1 assertQuestionReq(Long questionType, StringBuilder sb, Long subjectId, StringBuilder content) {
        SelectObjectiveQuestionRespV1 selectObjectiveQuestionRespV1 = new SelectObjectiveQuestionRespV1();
        selectObjectiveQuestionRespV1.setQuestionType(questionType.intValue());
        sb = new StringBuilder(sb.toString().trim());
        //1、题干->选项
        String stem = HtmlConvertUtil.getContent(sb, "题干", "", QuestionTailConstant.QUESTION_TO_CHOICE);
        selectObjectiveQuestionRespV1.setStem("<p>" + stem + "</p>");
        content.append(QuestionConvert.convertStem(stem));
        //2、选项->答案
        String choiceContent = HtmlConvertUtil.getContent(sb, "选项", "", QuestionTailConstant.QUESTION_TO_ANSWER);
        List<Map<String, Object>> choices = HtmlConvertUtil.parseChoiceContent(choiceContent.trim());
        selectObjectiveQuestionRespV1.setChoices(choices.stream().map(i -> String.valueOf(i.get("value"))).collect(Collectors.toList()));
        content.append(QuestionConvert.convertChoice(selectObjectiveQuestionRespV1.getChoices()));
        //3、答案->解析
        String answer = HtmlConvertUtil.getContent(sb, "答案", QuestionTailConstant.QUESTION_TO_ANSWER, QuestionTailConstant.QUESTION_TO_ANALYSIS);
        content.append(QuestionConvert.convertAnswer(answer.trim()));
        selectObjectiveQuestionRespV1.setAnswer(answer);
        //4、解析->拓展
        String analysis = HtmlConvertUtil.getContent(sb, "解析", QuestionTailConstant.QUESTION_TO_ANALYSIS, QuestionTailConstant.QUESTION_TO_EXTEND);
        selectObjectiveQuestionRespV1.setAnalysis("<p>" + analysis.trim() + "</p>");
        content.append(QuestionConvert.convertAnalysis(analysis.trim()));
        //5、拓展->标签
        String extend = HtmlConvertUtil.getContent(sb, "拓展", QuestionTailConstant.QUESTION_TO_EXTEND, QuestionTailConstant.QUESTION_TO_TAG);
        selectObjectiveQuestionRespV1.setExtend("<p>" + extend.trim() + "</p>");
        content.append(QuestionConvert.convertExtend(extend.trim()));
        //5、标签、知识点、难度
        commonQuestionServiceV1.assertQuestionCommonReq(selectObjectiveQuestionRespV1, sb, subjectId, content);
        return selectObjectiveQuestionRespV1;
    }

    @Override
    public SelectQuestionRespV1 convertMongoQuestion(Question question) {
        GenericQuestion genericQuestion = (GenericQuestion) question;
        SelectObjectiveQuestionRespV1 questionRespV1 = new SelectObjectiveQuestionRespV1();
        questionRespV1.setDifficult(DifficultyLevelEnum.create(genericQuestion.getDifficult()).getTitle());
        questionRespV1.setDifficultyLevel(genericQuestion.getDifficult());
        questionRespV1.setMultiId(new Long(genericQuestion.getParent()));
        questionRespV1.setKnowledgeIds(Lists.newArrayList(new Long(genericQuestion.getPoints().get(2))));
        questionRespV1.setKnowledgeList(Lists.newArrayList(genericQuestion.getPointsName().get(2)));
        questionRespV1.setStem(genericQuestion.getStem());
        questionRespV1.setChoices(genericQuestion.getChoices());
        questionRespV1.setAnalysis(genericQuestion.getAnalysis());
        questionRespV1.setAnswer(HtmlConvertUtil.parseMongoAnswer(genericQuestion.getAnswer()));
        questionRespV1.setExtend(genericQuestion.getExtend());
        return questionRespV1;
    }

    /**
     * 选择题去重字段摘取
     *
     * @param question
     * @param subjectFlag
     */
    @Override
    public Object findDuplicateQuestion(Question question, Integer subjectFlag) {
        if (question instanceof GenericQuestion) {
            String choiceContent = HtmlConvertUtil.assertChoicesContent(((GenericQuestion) question).getChoices());
            String stem = ((GenericQuestion) question).getStem();
            List<DuplicatePartResp> duplicateParts = commonQuestionServiceV1.findDuplicatePart(choiceContent, stem, "", "",
                    "", "", "", "", "", "",
                    question.getType());
            if (CollectionUtils.isEmpty(duplicateParts)) {
                return null;
            }
            return duplicateParts;
        }
        return null;
    }

    @Override
    public Question parseQuestion2MongoInfo(BaseQuestion question) {
        HashMap<String, Object> mapData = objectiveDuplicatePartService.findByQuestionId(question.getId());
        GenericQuestion genericQuestion = new GenericQuestion();
        //转换数据格式
        genericQuestion.setStem(HtmlConvertUtil.getContentFromMap(mapData, "stem"));
        String content = String.valueOf(mapData.get("choices"));
        List<String> choices = HtmlConvertUtil.parseChoices(content);
        genericQuestion.setChoices(choices.stream().map(i -> HtmlConvertUtil.span2Img(i, true)).collect(Collectors.toList()));
        genericQuestion.setAnswer(HtmlConvertUtil.formatAnswer2Mongo(mapData.get("answer").toString()));
        genericQuestion.setAnalysis(HtmlConvertUtil.getContentFromMap(mapData, "analysis"));
        genericQuestion.setExtend(HtmlConvertUtil.getContentFromMap(mapData, "extend"));
        genericQuestion.setParent(question.getMultiId().intValue());
        if (question.getMultiId() > 0) {
            List<String> materials = materialService.findByQuestionId(question.getMultiId()).stream().map(i -> i.getContent())
                    .map(i->HtmlConvertUtil.span2Img(i,true)).collect(Collectors.toList());
            genericQuestion.setMaterial(StringUtils.join(materials, "</br>"));
            genericQuestion.setMaterials(materials);
        }
        return genericQuestion;
    }


    /**
     * 通过试题id查询复用数据id
     *
     * @param id
     * @return
     */
    private QuestionDuplicate selectByQuestionId(Long id) {
        Example example = new Example(QuestionDuplicate.class);
        example.and().andEqualTo("questionId", id);
        List<QuestionDuplicate> questionDuplicates = selectByExample(example);
        if (CollectionUtils.isEmpty(questionDuplicates)) {
            return null;
        }
        return questionDuplicates.get(0);
    }
}

