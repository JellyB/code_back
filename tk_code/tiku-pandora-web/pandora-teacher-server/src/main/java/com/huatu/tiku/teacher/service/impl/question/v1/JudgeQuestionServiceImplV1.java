package com.huatu.tiku.teacher.service.impl.question.v1;

import com.google.common.collect.Lists;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.constant.QuestionTailConstant;
import com.huatu.tiku.constants.teacher.TeacherErrors;
import com.huatu.tiku.entity.duplicate.ObjectiveDuplicatePart;
import com.huatu.tiku.entity.duplicate.QuestionDuplicate;
import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.request.question.v1.*;
import com.huatu.tiku.response.question.DuplicatePartResp;
import com.huatu.tiku.response.question.v1.SelectJudgeQuestionRespV1;
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

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\6\27 0027.
 */
@Slf4j
@Service
public class JudgeQuestionServiceImplV1 extends BaseServiceImpl<QuestionDuplicate> implements QuestionServiceV1 {
    @Autowired
    ObjectiveDuplicatePartService objectiveDuplicatePartService;
    @Autowired
    HtmlFileUtil htmlFileUtil;
    @Autowired
    CommonQuestionServiceV1 teacherQuestionService;
    @Autowired
    TeacherMaterialService materialService;
    @Autowired
    QuestionMaterialService questionMaterialService;
    @Autowired
    KnowledgeService knowledgeService;

    public JudgeQuestionServiceImplV1() {
        super(QuestionDuplicate.class);
    }


    @Override
    @Transactional
    public void updateQuestion(UpdateQuestionReqV1 question) {
        UpdateCommonQuestionReqV1 updateCommonQuestionReqV1 = (UpdateCommonQuestionReqV1) question;
        UpdateJudgeQuestionReqV1 judgeQuestionRep = new UpdateJudgeQuestionReqV1();
        BeanUtils.copyProperties(updateCommonQuestionReqV1, judgeQuestionRep);
        checkUpdateQuestionStyle(judgeQuestionRep);

        judgeQuestionRep.setJudgeBasis(htmlFileUtil.html2DB(judgeQuestionRep.getJudgeBasis(), true));
        judgeQuestionRep.setAnalysis(htmlFileUtil.html2DB(judgeQuestionRep.getAnalysis(), true));
        judgeQuestionRep.setStem(htmlFileUtil.html2DB(judgeQuestionRep.getStem(), false));
        judgeQuestionRep.setExtend(htmlFileUtil.html2DB(judgeQuestionRep.getExtend(), true));
        //复用表信息处理(如果没有复用id，直接添加，否则不动复用数据（添加试题的时候不允许复用数据修改）)
        ObjectiveDuplicatePart objectiveDuplicatePart = new ObjectiveDuplicatePart();
        BeanUtils.copyProperties(judgeQuestionRep, objectiveDuplicatePart);
        objectiveDuplicatePart.setChoices(assertChoicesContent(Lists.newArrayList("<p>正确</p>", "<p>错误</p>")));
        if ("0".equals(objectiveDuplicatePart.getAnswer())) {
            objectiveDuplicatePart.setAnswer("B");
        } else {
            objectiveDuplicatePart.setAnswer("A");
        }
        if (judgeQuestionRep.getDuplicateId() == null || judgeQuestionRep.getDuplicateId() <= 0) {
            objectiveDuplicatePartService.insertWithFilter(objectiveDuplicatePart);
            judgeQuestionRep.setDuplicateId(objectiveDuplicatePart.getId());
        } else {
            objectiveDuplicatePart.setId(judgeQuestionRep.getDuplicateId());
            objectiveDuplicatePartService.updateWithFilter(objectiveDuplicatePart);
        }
        QuestionDuplicate questionDuplicate = QuestionDuplicate.builder().duplicateType(1).duplicateId(judgeQuestionRep.getDuplicateId()).questionId(judgeQuestionRep.getId()).build();
        Example example = new Example(QuestionDuplicate.class);
        example.and().andEqualTo("questionId", question.getId());
        //试题复用表关联关系
        updateByExampleSelective(questionDuplicate, example);
        List<Long> materialIds = question.getMaterialIds();
        if (CollectionUtils.isNotEmpty(materialIds)) {
            materialService.saveQuestionMaterialBindings(materialIds, question.getId());
        }
    }

    /**
     * 试题修改参数检查
     *
     * @param judgeQuestionRep
     */
    private void checkUpdateQuestionStyle(UpdateJudgeQuestionReqV1 judgeQuestionRep) {
        if (StringUtils.isBlank(judgeQuestionRep.getAnswer())) {
            throwBizException("答案不能为空");
        }
        if (StringUtils.isBlank(judgeQuestionRep.getStem())) {
            throwBizException("题干内容不能为空");
        }
        if (StringUtils.isBlank(judgeQuestionRep.getAnalysis())) {
            throwBizException("解析不能为空");
        }
    }

    /**
     * 添加判断题的复用部分
     *
     * @param insertQuestionReq
     * @return
     * @throws BizException
     */
    @Override
    @Transactional
    public Object insertQuestion(InsertQuestionReqV1 insertQuestionReq) throws BizException {
        InsertCommonQuestionReqV1 insertCommonQuestionReqV1 = (InsertCommonQuestionReqV1) insertQuestionReq;
        InsertJudgeQuestionReqV1 judgeQuestionRep = new InsertJudgeQuestionReqV1();
        BeanUtils.copyProperties(insertCommonQuestionReqV1, judgeQuestionRep);
        checkInsertQuestionStyle(judgeQuestionRep);
        judgeQuestionRep.setJudgeBasis(htmlFileUtil.html2DB(judgeQuestionRep.getJudgeBasis(), true));
        judgeQuestionRep.setAnalysis(htmlFileUtil.html2DB(judgeQuestionRep.getAnalysis(), true));
        judgeQuestionRep.setStem(htmlFileUtil.html2DB(judgeQuestionRep.getStem(), false));
        judgeQuestionRep.setExtend(htmlFileUtil.html2DB(judgeQuestionRep.getExtend(), true));
        //复用表信息处理(如果没有复用id，直接添加，否则不动复用数据（添加试题的时候不允许复用数据修改）)
        if (judgeQuestionRep.getDuplicateId() == null || judgeQuestionRep.getDuplicateId() <= 0) {
            ObjectiveDuplicatePart objectiveDuplicatePart = new ObjectiveDuplicatePart();
            BeanUtils.copyProperties(judgeQuestionRep, objectiveDuplicatePart);
            objectiveDuplicatePart.setChoices(assertChoicesContent(Lists.newArrayList("<p>正确</p>", "<p>错误</p>")));
            if ("0".equals(objectiveDuplicatePart.getAnswer())) {
                objectiveDuplicatePart.setAnswer("B");
            } else {
                objectiveDuplicatePart.setAnswer("A");
            }
            objectiveDuplicatePartService.insertWithFilter(objectiveDuplicatePart);
            judgeQuestionRep.setDuplicateId(objectiveDuplicatePart.getId());
        }
        QuestionDuplicate questionDuplicate = QuestionDuplicate.builder().duplicateType(1).duplicateId(judgeQuestionRep.getDuplicateId()).questionId(judgeQuestionRep.getId()).build();
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
     * 检查添加判断题的特有属性
     *
     * @param judgeQuestionRep
     */
    private void checkInsertQuestionStyle(InsertJudgeQuestionReqV1 judgeQuestionRep) {
        if (StringUtils.isBlank(judgeQuestionRep.getAnswer())) {
            throwBizException("答案不能为空");
        }
        if (StringUtils.isBlank(judgeQuestionRep.getStem())) {
            throwBizException("题干内容不能为空");
        }
        if (StringUtils.isBlank(judgeQuestionRep.getAnalysis())) {
            throwBizException("解析不能为空");
        }
    }

    /**
     * 将前端选项组装为mysql数据
     *
     * @param choices
     * @return
     */
    private String assertChoicesContent(List<String> choices) {
        StringBuilder sb = new StringBuilder();
        for (String choice : choices) {
            sb.append("<choices>").append(choice).append("<choices>");
        }
        return sb.toString();
    }

    @Override
    public InsertQuestionReqV1 assertInsertReq(Question question) {
        if (!(question instanceof GenericQuestion)) {
            log.error("question not match GenericQuestion:>>>{}", question);
            throw new BizException(TeacherErrors.NOT_MATCH_QUESTION_SAVE_TYPE);
        }

        GenericQuestion genericQuestion = (GenericQuestion) question;
        Long knowledgeId = new Long(genericQuestion.getPoints().get(2));
        Optional<Knowledge> optional = knowledgeService.findAll().stream().filter(i -> i.getId().equals(knowledgeId)).findFirst();
        if (null == optional) {
            log.info("试题：{}不存在知识点：{}",question.getId(),knowledgeId);
            throwBizException("知识点"+knowledgeId+"不存在");
        }
        InsertJudgeQuestionReqV1 insertQuestionReq = new InsertJudgeQuestionReqV1();
        insertQuestionReq.setStem(genericQuestion.getStem());
        insertQuestionReq.setAnalysis(genericQuestion.getAnalysis());
        insertQuestionReq.setAnswer(getAnswer(genericQuestion.getAnswer(), genericQuestion.getChoices()));
        insertQuestionReq.setId(new Long(genericQuestion.getId()));
        insertQuestionReq.setQuestionType(genericQuestion.getType());
        insertQuestionReq.setMultiId(new Long(genericQuestion.getParent()));
        insertQuestionReq.setDifficultyLevel(genericQuestion.getDifficult());
        insertQuestionReq.setSubject(new Long(genericQuestion.getSubject()));
        insertQuestionReq.setKnowledgeIds(Lists.newArrayList(knowledgeService.transKnowledgeId(knowledgeId,genericQuestion.getPoints().get(1).longValue())));
        insertQuestionReq.setStatus(question.getStatus() == 4 ? -1 : 1);
        insertQuestionReq.setCreatorId(question.getCreateBy());
        insertQuestionReq.setExtend(((GenericQuestion) question).getExtend());
        return insertQuestionReq;
    }

    /**
     * 根据答案和选项判断正确的选项内容，如果包含正确和对的字样，则答案返回1否则返回0
     *
     * @param answer
     * @param choices
     * @return
     */
    private String getAnswer(int answer, List<String> choices) {
        if (answer > 10 || CollectionUtils.isEmpty(choices) || choices.size() != 2) {
            throw new BizException(TeacherErrors.NOT_MATCH_QUESTION_SAVE_TYPE);
        }
        String rightConent = choices.get(answer - 1);
        if (rightConent.indexOf("正确") > 0 || rightConent.indexOf("对") > 0) {
            return "1";
        } else {
            return "0";
        }
    }

    /**
     * 查询判断单题的属性
     *
     * @param baseQuestion
     * @return
     */
    @Override
    public SelectQuestionRespV1 findQuestion(BaseQuestion baseQuestion) {
        Long id = baseQuestion.getId();
        if (!QuestionInfoEnum.QuestionSaveTypeEnum.JUDGE.equals(QuestionInfoEnum.getSaveTypeByQuestionType(baseQuestion.getQuestionType()))) {
            throw new BizException(ErrorResult.create(1000010, "查询的试题不是判断类的题目"));
        }
        SelectJudgeQuestionRespV1 selectJudgeQuestionResp = new SelectJudgeQuestionRespV1();
        QuestionDuplicate questionDuplicate = selectByQuestionId(id);
        if (questionDuplicate == null) {
            throw new BizException(TeacherErrors.NO_EXISTED_QUESTION);
        }
        BeanUtils.copyProperties(questionDuplicate, selectJudgeQuestionResp);
        ObjectiveDuplicatePart objectiveDuplicatePart = objectiveDuplicatePartService.selectByPrimaryKey(questionDuplicate.getDuplicateId());
        if (objectiveDuplicatePart != null) {
            BeanUtils.copyProperties(objectiveDuplicatePart, selectJudgeQuestionResp);
        }
        BeanUtils.copyProperties(baseQuestion, selectJudgeQuestionResp);
        selectJudgeQuestionResp.setId(baseQuestion.getId());
        selectJudgeQuestionResp.setAnswerDetail("A".equals(selectJudgeQuestionResp.getAnswer()) ? "正确" : "错误");
        selectJudgeQuestionResp.setAnswer("A".equals(selectJudgeQuestionResp.getAnswer()) ? "1" : "0");
        if (baseQuestion.getMultiId() > 0L) {
            List<Long> materialIds = materialService.findMaterialIdsByQuestion(baseQuestion);
            selectJudgeQuestionResp.setMaterialIds(materialIds);
        }
        teacherQuestionService.assertQuestionAttrs(selectJudgeQuestionResp);
        return selectJudgeQuestionResp;
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
        if(question instanceof GenericQuestion){
            GenericQuestion genericQuestion = (GenericQuestion)question;
            content.append(QuestionConvert.convertStemBefore(QuestionInfoEnum.QuestionTypeEnum.create(questionType).getValue(),QuestionConvert.htmlConvertContent(genericQuestion.getStem())));
            String answer = getAnswer(genericQuestion.getAnswer(), genericQuestion.getChoices());
            if(answer.equals("1")){
                content.append(QuestionConvert.convertAnswer("正确"));
            }else{
                content.append(QuestionConvert.convertAnswer("错误"));
            }
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
    public SelectJudgeQuestionRespV1 assertQuestionReq(Long questionType, StringBuilder sb, Long subjectId, StringBuilder content) {
        SelectJudgeQuestionRespV1 selectJudgeQuestionRespV1 = new SelectJudgeQuestionRespV1();
        selectJudgeQuestionRespV1.setQuestionType(questionType.intValue());
        sb = new StringBuilder(sb.toString().trim());
        //1、题干->答案
        String stem = HtmlConvertUtil.getContent(sb, "题干", "", QuestionTailConstant.QUESTION_TO_ANSWER);
        selectJudgeQuestionRespV1.setStem("<p>" + stem + "</p>");
        content.append(QuestionConvert.convertStem(stem));
        //2、答案->解析
        String answerDetail = HtmlConvertUtil.getContent(sb, "答案", QuestionTailConstant.QUESTION_TO_ANSWER, QuestionTailConstant.QUESTION_TO_ANALYSIS);
        selectJudgeQuestionRespV1.setAnswerDetail(answerDetail);
        if ("正确".equals(answerDetail) || "对".equals(answerDetail)) {
            selectJudgeQuestionRespV1.setAnswer("1");
            content.append(QuestionConvert.convertAnswer("对"));
        } else {
            selectJudgeQuestionRespV1.setAnswer("0");
            content.append(QuestionConvert.convertAnswer("错"));
        }
        //3、解析->拓展
        String analysis = HtmlConvertUtil.getContent(sb, "解析", QuestionTailConstant.QUESTION_TO_ANALYSIS, QuestionTailConstant.QUESTION_TO_EXTEND);
        selectJudgeQuestionRespV1.setAnalysis("<p>" + analysis.trim() + "</p>");
        content.append(QuestionConvert.convertAnalysis(analysis.trim()));
        //4、拓展->标签
        String extend = HtmlConvertUtil.getContent(sb, "拓展", QuestionTailConstant.QUESTION_TO_EXTEND, QuestionTailConstant.QUESTION_TO_TAG);
        selectJudgeQuestionRespV1.setExtend("<p>" + extend.trim() + "</p>");
        content.append(QuestionConvert.convertExtend(extend.trim()));
        //5、标签、知识点、难度
        teacherQuestionService.assertQuestionCommonReq(selectJudgeQuestionRespV1, sb, subjectId,content);
        return selectJudgeQuestionRespV1;
    }

    @Override
    public SelectQuestionRespV1 convertMongoQuestion(Question question) {
        GenericQuestion genericQuestion = (GenericQuestion) question;
        SelectJudgeQuestionRespV1 questionRespV1 = new SelectJudgeQuestionRespV1();
        questionRespV1.setDifficult(DifficultyLevelEnum.create(genericQuestion.getDifficult()).getTitle());
        questionRespV1.setDifficultyLevel(genericQuestion.getDifficult());
        questionRespV1.setMultiId(new Long(genericQuestion.getParent()));
        questionRespV1.setKnowledgeIds(Lists.newArrayList(new Long(genericQuestion.getPoints().get(2))));
        questionRespV1.setKnowledgeList(Lists.newArrayList(genericQuestion.getPointsName().get(2)));
        questionRespV1.setStem(genericQuestion.getStem());
        questionRespV1.setAnalysis(genericQuestion.getAnalysis());
        questionRespV1.setAnswer(HtmlConvertUtil.parseMongoAnswer(genericQuestion.getAnswer()));
        questionRespV1.setAnswerDetail(genericQuestion.getChoices().get(genericQuestion.getAnswer() - 1));
        questionRespV1.setExtend(genericQuestion.getExtend());
        return questionRespV1;
    }

    @Override
    public Object findDuplicateQuestion(Question question, Integer subjectFlag) {
        if (question instanceof GenericQuestion) {
            String stem = ((GenericQuestion) question).getStem();
            List<DuplicatePartResp> duplicateParts = teacherQuestionService.findDuplicatePart("", stem, "", "",
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
            List<String> materials = materialService.findByQuestionId(question.getMultiId()).stream().map(i -> i.getContent()).collect(Collectors.toList());
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
        return selectByExample(example).get(0);
    }
}

