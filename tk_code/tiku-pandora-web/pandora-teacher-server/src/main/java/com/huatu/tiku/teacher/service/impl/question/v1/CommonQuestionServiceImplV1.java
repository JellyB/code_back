package com.huatu.tiku.teacher.service.impl.question.v1;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.ErrorResult;
import com.huatu.common.SuccessMessage;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.constant.QuestionTailConstant;
import com.huatu.tiku.constants.teacher.TeacherErrors;
import com.huatu.tiku.dto.KnowledgeVO;
import com.huatu.tiku.dto.QuestionYearAreaDTO;
import com.huatu.tiku.entity.duplicate.QuestionDuplicate;
import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.tiku.entity.knowledge.QuestionKnowledge;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.entity.tag.Tag;
import com.huatu.tiku.entity.teacher.PaperActivity;
import com.huatu.tiku.entity.teacher.PaperEntity;
import com.huatu.tiku.entity.teacher.PaperQuestion;
import com.huatu.tiku.enums.BaseInfo;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.request.material.MaterialReq;
import com.huatu.tiku.request.question.v1.InsertQuestionReqV1;
import com.huatu.tiku.request.question.v1.QuestionReqV1;
import com.huatu.tiku.request.question.v1.UpdateQuestionReqV1;
import com.huatu.tiku.response.question.DuplicatePartResp;
import com.huatu.tiku.response.question.v1.*;
import com.huatu.tiku.response.subject.SubjectNodeResp;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.dao.mongo.NewQuestionDao;
import com.huatu.tiku.teacher.dao.mongo.ReflectQuestionDao;
import com.huatu.tiku.teacher.dao.question.BaseQuestionMapper;
import com.huatu.tiku.teacher.dao.question.BaseQuestionSearchMapper;
import com.huatu.tiku.teacher.enums.BizStatusEnum;
import com.huatu.tiku.teacher.enums.DifficultyLevelEnum;
import com.huatu.tiku.teacher.enums.StatusEnum;
import com.huatu.tiku.teacher.service.PaperActivityService;
import com.huatu.tiku.teacher.service.common.ImportService;
import com.huatu.tiku.teacher.service.duplicate.ObjectiveDuplicatePartService;
import com.huatu.tiku.teacher.service.duplicate.QuestionDuplicateService;
import com.huatu.tiku.teacher.service.duplicate.SubjectiveDuplicatePartService;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeSubjectService;
import com.huatu.tiku.teacher.service.knowledge.QuestionKnowledgeService;
import com.huatu.tiku.teacher.service.paper.PaperEntityService;
import com.huatu.tiku.teacher.service.paper.PaperQuestionService;
import com.huatu.tiku.teacher.service.question.DuplicateQuestionService;
import com.huatu.tiku.teacher.service.question.QuestionSearchService;
import com.huatu.tiku.teacher.service.question.QuestionTypeService;
import com.huatu.tiku.teacher.service.question.QuestionYearAreaViewService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.tiku.teacher.service.question.v1.QuestionServiceV1;
import com.huatu.tiku.teacher.service.subject.TeacherSubjectService;
import com.huatu.tiku.teacher.service.tag.TeacherTagService;
import com.huatu.tiku.util.html.HtmlConvertUtil;
import com.huatu.tiku.util.question.QuestionConvert;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.KnowledgeInfo;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.ReflectQuestion;
import com.huatu.ztk.question.common.QuestionStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.huatu.tiku.constant.BaseConstant.QUESTION_PARSE_ERROR;
import static com.huatu.tiku.enums.QuestionInfoEnum.QuestionSaveTypeEnum.SUBJECTIVE;

/**
 * Created by huangqp on 2018\4\23 0023.
 */
@Slf4j
@Service
public class CommonQuestionServiceImplV1 extends BaseServiceImpl<BaseQuestion> implements CommonQuestionServiceV1 {

    @Autowired
    private BaseQuestionMapper baseQuestionMapper;
    @Autowired
    private TeacherSubjectService teacherSubjectService;
    @Autowired
    private TeacherTagService tagService;
    @Autowired
    private KnowledgeService knowledgeService;
    @Autowired
    KnowledgeSubjectService knowledgeSubjectService;
    @Autowired
    private QuestionDuplicateService questionDuplicateService;
    @Autowired
    PaperQuestionService paperQuestionService;
    @Autowired
    SubjectiveDuplicatePartService subjectiveDuplicatePartService;
    @Autowired
    ObjectiveDuplicatePartService objectiveDuplicatePartService;
    @Autowired
    QuestionTypeService questionTypeService;
    @Autowired
    QuestionKnowledgeService questionKnowledgeService;
    @Autowired
    QuestionYearAreaViewService questionYearAreaViewService;
    @Autowired
    ReflectQuestionDao reflectQuestionDao;
    @Resource(name = "objectiveQuestionServiceImplV1")
    private QuestionServiceV1 objectiveQuestionService;
    @Resource(name = "compositeQuestionServiceImplV1")
    private QuestionServiceV1 compositeQuestionService;
    @Resource(name = "judgeQuestionServiceImplV1")
    private QuestionServiceV1 judgeQuestionService;
    @Resource(name = "subjectiveQuestionServiceImplV1")
    private QuestionServiceV1 subjectiveQuestionService;

    @Autowired
    private PaperEntityService paperEntityService;
    @Autowired
    private PaperActivityService paperActivityService;
    @Autowired
    private BaseQuestionSearchMapper searchMapper;
    @Autowired
    private QuestionSearchService questionSearchService;
    @Autowired
    private ImportService importService;
    @Autowired
    private NewQuestionDao questionDao;

    @Autowired
    private DuplicateQuestionService duplicateQuestionService;


    public CommonQuestionServiceImplV1() {
        super(BaseQuestion.class);
    }


    /**
     * 附加所有试题关联信息
     *
     * @param selectQuestionResp
     */
    @Override
    public void assertQuestionAttrs(SelectQuestionRespV1 selectQuestionResp) {
        long questionId = selectQuestionResp.getId();
        //难度
        selectQuestionResp.setDifficult(DifficultyLevelEnum.create(selectQuestionResp.getDifficultyLevel()).getTitle());
        //试题知识点数据
        Example knowledgeExamp = new Example(QuestionKnowledge.class);
        knowledgeExamp.and().andEqualTo("questionId", questionId);
        List<QuestionKnowledge> questionKnowledgeList = questionKnowledgeService.selectByExample(knowledgeExamp);
        //知识点信息整合
        if (CollectionUtils.isNotEmpty(questionKnowledgeList)) {
            List<Long> ids = questionKnowledgeList.stream().map(i -> i.getKnowledgeId()).distinct().collect(Collectors.toList());
            selectQuestionResp.setKnowledgeIds(ids);
            List<String> knowledgeList = knowledgeService.getKnowledgeNameByIds(ids);
            if (CollectionUtils.isNotEmpty(knowledgeList)) {
                selectQuestionResp.setKnowledgeList(knowledgeList);
            }
        }

        //科目数据分析
        SubjectNodeResp subjectNodeResp = teacherSubjectService.parseSubject(selectQuestionResp.getSubject());
        selectQuestionResp.setSubject(subjectNodeResp.getSubject());
        if (CollectionUtils.isNotEmpty(subjectNodeResp.getGrades())) {
            selectQuestionResp.setGrades(subjectNodeResp.getGrades());
            selectQuestionResp.setGradeList(teacherSubjectService.getNameByIds(subjectNodeResp.getGrades()));
        }

        //试题标签数据
        List<HashMap<String, Object>> questionTags = searchMapper.getQuestionTagInfo(questionId);
        if (CollectionUtils.isNotEmpty(questionTags)) {
            List<Long> tags = questionTags.stream().filter(i -> i.get("id") != null).map(i -> Long.parseLong(i.get("id").toString())).collect(Collectors.toList());
            List<String> tagNames = questionTags.stream().filter(i -> i.get("name") != null).map(i -> i.get("name").toString()).collect(Collectors.toList());
            selectQuestionResp.setTags(tags);
            selectQuestionResp.setTagList(tagNames);
        }

        //试题来源数据查询
        String source = questionSearchService.findSingleQuestionSource(questionId);
        if (StringUtils.isNotBlank(source)) {
            selectQuestionResp.setSourceList(Lists.newArrayList(source));
        }
        QuestionYearAreaDTO questionYearAreaDTO = questionYearAreaViewService.selectByPrimaryKey(questionId);
        selectQuestionResp.setQuestionYearArea(questionYearAreaDTO);

    }

    /**
     * 修改试题信息
     *
     * @param question
     * @return
     */
    @Transactional
    public void updateQuestionBase(UpdateQuestionReqV1 question) throws BizException {
        QuestionInfoEnum.QuestionSaveTypeEnum newSaveType = QuestionInfoEnum.getSaveTypeByQuestionType(question.getQuestionType());
        BaseQuestion updateQuestion = selectByPrimaryKey(question.getId());
        BeanUtils.copyProperties(question, updateQuestion);
        //更新统计标签
        if (CollectionUtils.isNotEmpty(question.getStatisticsTagList())) {
            updateQuestion.setStatisticsTag(JsonUtil.toJson(question.getStatisticsTagList()));
        }
        //保证复用字段跟这里的字段只维护一份
        Example example = new Example(BaseQuestion.class);
        example.and().andEqualTo("id", question.getId());
        updateByExampleSelective(updateQuestion, example);
        if (newSaveType == null) {
            throw new BizException(TeacherErrors.ILLEGAL_QUESTION_SAVE_TYPE);
        }
        QuestionServiceV1 questionService = choiceService(newSaveType);
        questionService.updateQuestion(question);
        if (newSaveType.equals(QuestionInfoEnum.QuestionSaveTypeEnum.COMPOSITE)) {
            return;
        }
        //处理其他属性关联表的修改
        updateQuestionCommonAttrs(question);
    }

    /**
     * 处理其他属性关联表的修改
     *
     * @param question
     */
    @Transactional
    public void updateQuestionCommonAttrs(UpdateQuestionReqV1 question) throws BizException {
        Long questionId = question.getId();
        Long modifierId = question.getModifierId();
        /**
         * 筛选出符合科目的知识点
         * TODO 如果有学段，需要做兼容逻辑（选段作为科目，且考虑多学段的问题）
         */
        //试题标签表数据
        tagService.updateQuestionTag(question.getTags(), questionId, modifierId);

        if (CollectionUtils.isNotEmpty(question.getKnowledgeIds())) {
            List<Long> knowledgeIds = knowledgeSubjectService.choicesKnowledgeBySubject(question.getKnowledgeIds(), question.getSubject());
            //试题知识点数据
            knowledgeService.updateQuestionKnowledge(knowledgeIds, questionId);
        }
    }


    /**
     * 修改试题发布状态
     *
     * @param questionId
     * @param status
     * @return
     */
    @Override
    public Object updateQuestionBizStatus(Long questionId, Integer status) {
        if (questionId == null || questionId <= 0) {
            throw new BizException(TeacherErrors.NO_EXISTED_QUESTION);
        }
        BaseQuestion baseQuestion = selectByPrimaryKey(questionId);
        baseQuestion.setBizStatus(status);
        String info = "";
        if (BizStatusEnum.NO_PUBLISH.getValue().equals(status)) {
            updateByPrimaryKey(baseQuestion);
            info = "试题取消发布成功";
        } else if (BizStatusEnum.PUBLISH.getValue().equals(status)) {
            updateByPrimaryKey(baseQuestion);
            info = "试题发布成功";
        } else {
            throwBizException("修改失败，未知的状态参数");
        }
        //mysql->mongo
        importService.sendQuestion2Mongo(questionId.intValue());
        return SuccessMessage.create(info);
    }

    /**
     * 修改试题作废标识
     *
     * @param questionId
     * @param availFlag
     * @return
     */
    @Override
    public Object updateQuestionAvailable(Long questionId, QuestionInfoEnum.AvailableEnum availFlag) {
        if (questionId == null || questionId <= 0) {
            throw new BizException(TeacherErrors.NO_EXISTED_QUESTION);
        }
        BaseQuestion baseQuestion = selectByPrimaryKey(questionId);
        baseQuestion.setAvailFlag(availFlag.getCode());
        save(baseQuestion);
        //mysql->mongo
        importService.sendQuestion2Mongo(questionId.intValue());
        return SuccessMessage.create("试题" + availFlag.getDesc() + "成功");
    }

    /**
     * 修改试题残缺状态标识
     *
     * @param questionId
     * @param missFlag
     * @return
     */
    @Override
    public Object updateQuestionStatus(Long questionId, QuestionInfoEnum.CompleteEnum missFlag) {
        if (questionId == null || questionId <= 0) {
            throw new BizException(TeacherErrors.NO_EXISTED_QUESTION);
        }
        BaseQuestion baseQuestion = selectByPrimaryKey(questionId);
        if (baseQuestion == null) {
            throw new BizException(TeacherErrors.NO_EXISTED_QUESTION);
        }
        baseQuestion.setMissFlag(missFlag.getCode());
        save(baseQuestion);
        //mysql->mongo
        importService.sendQuestion2Mongo(questionId.intValue());
        return SuccessMessage.create("试题" + missFlag.getDesc() + "成功");

    }

    /**
     * 去重查询
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
     * @return
     */
    @Override
    public List<DuplicatePartResp> findDuplicatePart(String choices, String stem, String analysis, String extend,
                                                     String answerComment, String analyzeQuestion, String answerRequest, String bestowPointExplain, String trainThought, String omnibusRequirements, Integer questionType) {
        QuestionInfoEnum.QuestionDuplicateTypeEnum duplicateType = QuestionInfoEnum.getDuplicateTypeByQuestionType(questionType);
        List<DuplicatePartResp> duplicatePartResps = null;
        if (duplicateType.equals(QuestionInfoEnum.QuestionDuplicateTypeEnum.JUDGE_OBJECT)) {
            duplicatePartResps = objectiveDuplicatePartService.selectByMyExample(choices, stem, analysis, extend, questionType);
        } else if (duplicateType.equals(QuestionInfoEnum.QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE)) {
            duplicatePartResps = subjectiveDuplicatePartService.selectByMyExample(stem, extend, answerComment, analyzeQuestion, answerRequest, bestowPointExplain, trainThought, omnibusRequirements, questionType);
        } else {
            throwBizException("题型参数异常：type=" + questionType);
        }
        if (CollectionUtils.isEmpty(duplicatePartResps)) {
            return Lists.newArrayList();
        }
        List<Long> allDuplicateIds = duplicatePartResps.stream().map(DuplicatePartResp::getDuplicateId).collect(Collectors.toList());
        Example example = new Example(QuestionDuplicate.class);
        example.and().andIn("duplicateId", allDuplicateIds);
        List<QuestionDuplicate> questionDuplicates = questionDuplicateService.selectByExample(example);
        //没有绑定试题的复用数据无效
        if (CollectionUtils.isEmpty(questionDuplicates)) {
            return Lists.newArrayList();
        }
        //所有相关试题Id
        List<Long> questionIds = questionDuplicates.stream().map(QuestionDuplicate::getQuestionId).distinct().collect(Collectors.toList());
        Example questionExample = new Example(BaseQuestion.class);
        questionExample.and().andIn("id", questionIds);
        List<BaseQuestion> baseQuestions = selectByExample(questionExample);
        if (CollectionUtils.isEmpty(baseQuestions)) {
            return Lists.newArrayList();
        }
        //真实存在的试题ID
        List<Long> collect = baseQuestions.stream().map(BaseQuestion::getId).collect(Collectors.toList());
        questionDuplicates = questionDuplicates.stream().filter(i -> collect.contains(i.getQuestionId())).collect(Collectors.toList());
        //绑定试题的复用数据
        List<Long> duplicateIds = questionDuplicates.stream().map(QuestionDuplicate::getDuplicateId).distinct().collect(Collectors.toList());
        //筛选绑定试题的复用数据
        return duplicatePartResps.stream().filter(i -> duplicateIds.contains(i.getDuplicateId())).collect(Collectors.toList());
    }

    @Override
    public Object parseQuestionInfo(String text, Long subjectId, Long questionId) {
        //<p><br>标签全部转换为换行，其他标签全部删掉
        text = text.replace("\r\n", "\n").replaceAll("<[/]?br[/]?>", "\n").replaceAll("<[/]?p>", "\n").replaceAll("<[^>]+>", "");
        while (text.indexOf("\n\n") != -1) {
            text = text.replace("\n\n", "\n");
        }
        StringBuilder sb = new StringBuilder(text.trim());
        String questionTypeName = HtmlConvertUtil.getQuestionTypeName(sb);
        Long questionType = -1L;
        if (StringUtils.isNotBlank(questionTypeName)) {
            questionType = questionTypeService.findIdByName(questionTypeName.trim());
        }
        //业务类型
        QuestionInfoEnum.QuestionSaveTypeEnum bizType = QuestionInfoEnum.getSaveTypeByQuestionType(questionType.intValue());
        if (bizType.equals(QuestionInfoEnum.QuestionSaveTypeEnum.UNKNOWN_TYPE)) {
            throwBizException("题型不存在，请确认是否有题型字段，或者" + QUESTION_PARSE_ERROR);
        }
        checkTagExist(sb.toString());
        QuestionServiceV1 questionServiceV1 = choiceService(bizType);
        StringBuilder content = new StringBuilder("");
        content.append(QuestionConvert.convertQuestionType(questionTypeName.trim()));
        SelectQuestionRespV1 selectQuestionRespV1 = questionServiceV1.assertQuestionReq(questionType, sb, subjectId, content);
        //检查非空字段
        checkEmptyStyle(selectQuestionRespV1);
        selectQuestionRespV1.setParseContent(content.toString().replace("<br>", "\n"));

        Long duplicateId = null;
        //查询duplicateId
        if (questionId != BaseInfo.SEARCH_DEFAULT_LONG) {
            List<Map> duplicateQuestion = questionDuplicateService.findWithDuplicateByQuestionId(questionId);
            if (CollectionUtils.isNotEmpty(duplicateQuestion)) {
                Object duplicateIdObj = duplicateQuestion.get(0).get("duplicateId");
                if (null != duplicateIdObj)
                    duplicateId = Long.valueOf(duplicateIdObj.toString());
            }
        }
        selectQuestionRespV1.setDuplicateId(duplicateId);
        log.info("复用ID是:{}", selectQuestionRespV1.getDuplicateId());
        return selectQuestionRespV1;
    }

    @Override
    public String formatQuestionInfo(Long questionId) {
        Question question = questionDao.findById(questionId.intValue());
        if (null == question) {
            throw new BizException(ErrorResult.create(10010120, "试题不存在或没有被同步"));
        }
        Integer questionType = question.getType();
        QuestionInfoEnum.QuestionSaveTypeEnum bizType = QuestionInfoEnum.getSaveTypeByQuestionType(questionType.intValue());
        if (bizType.equals(QuestionInfoEnum.QuestionSaveTypeEnum.UNKNOWN_TYPE)) {
            throwBizException("题型不存在，请确认是否有题型字段，或者" + QUESTION_PARSE_ERROR);
        }
        QuestionServiceV1 questionServiceV1 = choiceService(bizType);
        StringBuilder content = questionServiceV1.formatQuestionInfo(question);
        List<HashMap<String, Object>> questionTags = searchMapper.getQuestionTagInfo(questionId);
        if (CollectionUtils.isNotEmpty(questionTags)) {
            List<String> tagNames = questionTags.stream().filter(i -> i.get("name") != null).map(i -> i.get("name").toString()).collect(Collectors.toList());
            content.append(QuestionConvert.convertTag(tagNames));
        } else {
            content.append(QuestionConvert.convertTag(Lists.newArrayList()));
        }
        List<KnowledgeInfo> pointList = question.getPointList();
        List<String> knowledgeList = pointList.stream().map(i -> i.getPointsName().stream().collect(Collectors.joining("*"))).collect(Collectors.toList());
        content.append(QuestionConvert.convertKnowledge(knowledgeList));
        content.append(QuestionConvert.convertDifficult(DifficultyLevelEnum.create(question.getDifficult()).getTitle()));
        return content.toString();
    }

    @Override
    public void findAndHandlerQuestion(Consumer<List<Question>> consumer, int subject) {
        //分片查询mongo的数据比较
        int startIndex = 0;
        int offset = 1000;
        while (true) {
            //查询MONGO复合条件的id（左开右闭）
            List<Question> mongoQuestionList = questionDao.findBySubjectPage(startIndex, offset, subject);
            if (CollectionUtils.isEmpty(mongoQuestionList)) {
                break;
            } else {
                consumer.accept(mongoQuestionList);
            }
            int endIndex = mongoQuestionList.stream().map(Question::getId).max(Comparator.comparing(Integer::intValue)).get();
            startIndex = endIndex;
        }
    }

    /**
     * 验证标签不能为空
     */
    public void checkTagExist(String content) {
        QuestionTailConstant.getTagList().stream().forEach(tag -> {
            tag = tag.substring(1, tag.length());
            Pattern pattern = Pattern.compile(tag);
            Matcher matcher = pattern.matcher(content);
            while (!matcher.find()) {
                throwBizException(tag + "标签不能为空!");
            }
        });
    }

    /**
     * 解析字段非空校验
     *
     * @param question
     */
    private void checkEmptyStyle(SelectQuestionRespV1 question) {
        //校验字符串或者集合是否为空
        Function<Object, Boolean> isEmpty = (s -> {
            if (s instanceof String) {
                if (StringUtils.isBlank((String) s) || StringUtils.isBlank(((String) s).trim().replaceAll("<[/]?p>", ""))) {
                    return true;
                }
            }
            if (s instanceof Collection) {
                if (CollectionUtils.isEmpty((Collection) s)) {
                    return true;
                }
            }
            return false;
        });
        //筛选需要判空的字段
        Function<SelectQuestionRespV1, Map<String, Object>> checkAttrs = (q -> {
            HashMap<String, Object> result = Maps.newHashMap();
            if (q instanceof SelectObjectiveQuestionRespV1) {
                result.put("题干", ((SelectObjectiveQuestionRespV1) q).getStem());
                result.put("选项", ((SelectObjectiveQuestionRespV1) q).getChoices());
                result.put("答案", ((SelectObjectiveQuestionRespV1) q).getAnswer());
                result.put("解析", ((SelectObjectiveQuestionRespV1) q).getAnalysis());
            }
            if (q instanceof SelectJudgeQuestionRespV1) {
                result.put("题干", ((SelectJudgeQuestionRespV1) q).getStem());
                result.put("答案", ((SelectJudgeQuestionRespV1) q).getAnswer());
                result.put("解析", ((SelectJudgeQuestionRespV1) q).getAnalysis());
            }
            if (q instanceof SelectSubjectiveQuestionRespV1) {
                result.put("题干", ((SelectSubjectiveQuestionRespV1) q).getStem());
                result.put("答案", ((SelectSubjectiveQuestionRespV1) q).getAnswerComment());
                result.put("解析", ((SelectSubjectiveQuestionRespV1) q).getAnalyzeQuestion());
            }
            return result;
        });
        Map<String, Object> resultMap = checkAttrs.apply(question);
        //key为字段名，value为字段内容
        for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
            if (isEmpty.apply(entry.getValue())) {
                throwBizException(entry.getKey() + "不能为空");
            }
        }
        /**
         * update by lizhenjuan
         * 选择类题目答案校验
         */
        checkJudgeQuestion(resultMap, question.getQuestionType());

    }

    @Override
    public void assertQuestionCommonReq(SelectQuestionRespV1 selectQuestionRespV1, StringBuilder sb, Long subjectId, StringBuilder content) {
        //6、标签->知识点
        String tagName = HtmlConvertUtil.getContent(sb, "标签", QuestionTailConstant.QUESTION_TO_TAG, QuestionTailConstant.QUESTION_TO_KNOWLEDGE);
        List<String> tagNames = Arrays.stream(tagName.trim().split("，")).map(i -> i.trim()).collect(Collectors.toList());
        List<Long> tagIds = tagService.getTagIdByNames(tagNames);
        if (CollectionUtils.isNotEmpty(tagIds) && tagIds.size() == tagNames.size()) {
            content.append(QuestionConvert.convertTag(tagNames));
        } else if (CollectionUtils.isEmpty(tagIds)) {
            content.append(QuestionConvert.convertTag(Lists.newArrayList()));
        } else {
            Example example = new Example(Tag.class);
            example.and().andIn("id", tagIds);
            content.append(QuestionConvert.convertTag(tagService.selectByExample(example).stream().map(Tag::getName).collect(Collectors.toList())));
        }
        //7、知识点->难度
        String knowledgeName = HtmlConvertUtil.getContent(sb, "知识点", QuestionTailConstant.QUESTION_TO_KNOWLEDGE, QuestionTailConstant.QUESTION_TO_DIFFICULT);
        List<String> knowledgeList = Arrays.stream(knowledgeName.trim().split("，")).map(i -> i.trim()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(knowledgeList)) {
            throwBizException("知识点解析不到内容，请确认是否有知识点标签，或者" + QUESTION_PARSE_ERROR);
        }
        List<KnowledgeVO> knowledgeVOS = knowledgeService.showKnowledgeTreeBySubject(subjectId, false);
        List<Long> knowledgeIds = knowledgeService.getKnowledgeIdByInfo(knowledgeList, knowledgeVOS);
        knowledgeList.clear();
        if (CollectionUtils.isNotEmpty(knowledgeIds)) {
            knowledgeList.addAll(knowledgeService.getKnowledgeNameByIds(knowledgeIds));
        }
        content.append(QuestionConvert.convertKnowledge(knowledgeList));
        //8难度->
        String difficult = sb.toString().trim().replaceAll(QuestionTailConstant.QUESTION_TO_DIFFICULT, "").trim();
        DifficultyLevelEnum difficultyLevelEnum = DifficultyLevelEnum.create(difficult);
        Integer difficultLevel = difficultyLevelEnum.getValue();
        content.append(QuestionConvert.convertDifficult(difficultyLevelEnum.getTitle()));
        selectQuestionRespV1.setTagList(tagNames);
        selectQuestionRespV1.setTags(tagIds);
        selectQuestionRespV1.setKnowledgeList(knowledgeList);
        selectQuestionRespV1.setKnowledgeIds(knowledgeIds);
        selectQuestionRespV1.setDifficult(difficult);
        selectQuestionRespV1.setDifficultyLevel(difficultLevel);

    }

    @Override
    public SelectQuestionRespV1 convertQuestionMongo2DB(Question question) {
        Integer questionType = question.getType();
        QuestionInfoEnum.QuestionSaveTypeEnum saveType = QuestionInfoEnum.getSaveTypeByQuestionType(questionType);
        QuestionServiceV1 questionServiceV1 = choiceService(saveType);
        SelectQuestionRespV1 selectQuestionRespV1 = questionServiceV1.convertMongoQuestion(question);
        selectQuestionRespV1.setId(new Long(question.getId()));
        selectQuestionRespV1.setMoveFlag(0);
        selectQuestionRespV1.setMode(question.getMode());
        selectQuestionRespV1.setBizStatus(question.getStatus() == QuestionStatus.AUDIT_SUCCESS ? BizStatusEnum.PUBLISH.getValue() : BizStatusEnum.NO_PUBLISH.getValue());
        selectQuestionRespV1.setQuestionType(question.getType());
        selectQuestionRespV1.setSubject(new Long(question.getSubject()));
        return selectQuestionRespV1;
    }

    /**
     * @param question
     * @param subjectFlag 科目过滤标识（复合题科目过滤在内层实现，单题的科目过滤在本层实现）
     * @param yearFlag    年份过滤标识（复合题无来源，不过滤，单题的时间过滤在本层实现）
     * @return
     */
    @Override
    public Object findDuplicateQuestion(Question question, Integer subjectFlag, Integer yearFlag) {
        Integer questionType = question.getType();
        QuestionInfoEnum.QuestionSaveTypeEnum saveType = QuestionInfoEnum.getSaveTypeByQuestionType(questionType);
        QuestionServiceV1 questionServiceV1 = choiceService(saveType);
        Object duplicateQuestion = questionServiceV1.findDuplicateQuestion(question, subjectFlag);
        //如果是复合题，复合题id和材料数据都已得到，不做下一步数据处理，其他题型试题，需要通过duplicateID,得到试题id
        if (QuestionInfoEnum.QuestionSaveTypeEnum.COMPOSITE.equals(saveType)) {
            return duplicateQuestion;
        } else if (duplicateQuestion != null) {
            List<DuplicatePartResp> duplicatePartResps = (List<DuplicatePartResp>) duplicateQuestion;
            List<Long> ids = duplicatePartResps.stream().map(i -> i.getDuplicateId()).collect(Collectors.toList());
            //查询复用数据和试题的绑定关系
            Example example = new Example(QuestionDuplicate.class);
            example.and().andIn("duplicateId", ids);
            List<QuestionDuplicate> questionDuplicates = questionDuplicateService.selectByExample(example);
            if (subjectFlag.equals(BaseInfo.YESANDNO.YES.getCode())) {
                List<Long> questionIds = questionDuplicates.stream().map(QuestionDuplicate::getQuestionId).distinct().collect(Collectors.toList());
                Example questionExample = new Example(BaseQuestion.class);
                questionExample.and().andIn("id", questionIds);
                //科目筛选
                List<Long> idFilterBySubject = selectByExample(questionExample).stream().filter(i -> i.getSubjectId().intValue() == question.getSubject()).map(BaseQuestion::getId).collect(Collectors.toList());
                questionDuplicates.removeIf(i -> !idFilterBySubject.contains(i.getQuestionId()));
            }
            if (yearFlag.equals(BaseInfo.YESANDNO.YES.getCode()) && question.getYear() > 0) {
                //试题的年份正常的才可以做筛选，负责不做处理
                if (question.getYear() < 3000 && question.getYear() > 1900) {
                    questionDuplicates = questionDuplicates.stream().filter(i -> {
                        QuestionYearAreaDTO questionYearAreaDTO = questionYearAreaViewService.selectByPrimaryKey(i.getQuestionId());
                        List<Integer> yearList = questionYearAreaDTO.getYearList();
                        if (CollectionUtils.isEmpty(yearList)) {
                            return false;
                        }
                        return yearList.contains(question.getYear());
                    }).collect(Collectors.toList());
                }
            }
            Map<Long, List<QuestionDuplicate>> questionDuplicateMap = questionDuplicates.stream().collect(Collectors.groupingBy(i -> i.getDuplicateId()));
            //删除没有绑定试题的复用数据
            duplicatePartResps.removeIf(i -> questionDuplicateMap.get(i.getDuplicateId()) == null);
            //绑定试题的复用数据，暴露一个试题id给前端
            duplicatePartResps.forEach(i -> i.setQuestionId(questionDuplicateMap.get(i.getDuplicateId()).get(0).getQuestionId()));

            return duplicatePartResps;
        }
        return null;
    }

    /**
     * 生成潘多拉试题对应的mongo对象，如果pandora试题是删除状态，mongo中没有数据，则不处理，否则，查询mongo，然后将状态置为删除
     *
     * @param questionId 试题id
     * @return
     */
    @Override
    public Question parseQuestion2Mongo(long questionId) {
        //查询试题，如果没有则用映射到试题替代
        BaseQuestion question = findVaildQuestion(questionId);
        if (null != question && question.getStatus() <= 0) {
            Question data = questionDao.findById(question.getId().intValue());
            if (null != data) {
                data.setStatus(QuestionStatus.DELETED);
            }
            return data;
        }
        QuestionInfoEnum.QuestionSaveTypeEnum saveTypeEnum = QuestionInfoEnum.getSaveTypeByQuestionType(question.getQuestionType());
        QuestionServiceV1 questionServiceV1 = choiceService(saveTypeEnum);
        //不同类型试题特有属性添加
        Question result = questionServiceV1.parseQuestion2MongoInfo(question);
        //同步试题ID得到他的mode属性
        Function<Long, Integer> getQuestionMode = (id -> {
            int mode = 2;
            Example example = new Example(PaperQuestion.class);
            example.and().andEqualTo("questionId", question.getId()).andEqualTo("paperType", PaperInfoEnum.TypeInfo.ENTITY.getCode());
            List<PaperQuestion> paperQuestions = paperQuestionService.selectByExample(example);
            if (CollectionUtils.isNotEmpty(paperQuestions)) {
                Example paperExample = new Example(PaperEntity.class);
                paperExample.and().andIn("id", paperQuestions.stream().map(PaperQuestion::getPaperId).distinct().collect(Collectors.toList()));
                List<PaperEntity> paperEntities = paperEntityService.selectByExample(paperExample);
                for (PaperEntity paperEntity : paperEntities) {
                    if (paperEntity.getMode().equals(PaperInfoEnum.ModeEnum.TRUE_PAPER.getCode())) {
                        mode = PaperInfoEnum.ModeEnum.TRUE_PAPER.getCode();
                        break;
                    }
                }
            }
            return mode;
        });
        //共有属性添加
        Function<Question, Question> transData = (temp -> {
            temp.setId(question.getId().intValue());
            Integer mode = getQuestionMode.apply(question.getId());
            if (!mode.equals(question.getMode())) {
                question.setMode(mode);
                save(question);
            }
            temp.setMode(mode);
            temp.setSubject(question.getSubjectId().intValue());
            temp.setType(question.getQuestionType());
            QuestionYearAreaDTO questionYearAreaDTO = questionYearAreaViewService.selectByPrimaryKey(question.getId());
            if (null != questionYearAreaDTO) {
                if (CollectionUtils.isNotEmpty(questionYearAreaDTO.getAreaList())) {
                    QuestionYearAreaDTO.Area area = questionYearAreaDTO.getAreaList().get(0);
                    temp.setArea(null == area.getAreaId() ? -9 : area.getAreaId().intValue());
                }
                if (CollectionUtils.isNotEmpty(questionYearAreaDTO.getYearList())) {
                    Integer year = questionYearAreaDTO.getYearList().get(0);
                    temp.setYear(null == year ? -1 : year);
                }
            } else {
                temp.setArea(-9);
                temp.setYear(-1);
            }
            temp.setDifficult(question.getDifficultyLevel());
            String source = questionSearchService.findSingleQuestionSource(questionId);
            if (StringUtils.isNotBlank(source)) {
                temp.setFrom(source);
            }
            temp.setScore(new Float(question.getScore()));
            if (question.getStatus().intValue() != BaseInfo.YESANDNO.YES.getKey()) {
                temp.setStatus(QuestionStatus.DELETED);
            } else if (question.getBizStatus().equals(BizStatusEnum.PUBLISH.getValue())) {
                temp.setStatus(QuestionStatus.AUDIT_SUCCESS);
            } else {
                temp.setStatus(QuestionStatus.CREATED);
            }
            //知识点转化
            Example questionKnowledgeExample = new Example(QuestionKnowledge.class);
            questionKnowledgeExample.and().andEqualTo("questionId", question.getId());
            List<QuestionKnowledge> questionKnowledges = questionKnowledgeService.selectByExample(questionKnowledgeExample);
            if (CollectionUtils.isNotEmpty(questionKnowledges)) {
                List<Long> knowledgeIds = questionKnowledges.stream().map(i -> i.getKnowledgeId()).collect(Collectors.toList());
                List<KnowledgeInfo> knowledgeInfos = knowledgeService.getPointListByIds(knowledgeIds);
                knowledgeInfos.sort(Comparator.comparing(i -> i.getPoints().get(2)));
                temp.setPointList(knowledgeInfos);
                if (temp instanceof GenericQuestion && CollectionUtils.isNotEmpty(knowledgeInfos)) {
                    int index = Math.min(3, knowledgeInfos.get(0).getPoints().size());
                    ((GenericQuestion) temp).setPoints(knowledgeInfos.get(0).getPoints().subList(0, index));
                    ((GenericQuestion) temp).setPointsName(knowledgeInfos.get(0).getPointsName().subList(0, index));
                }
            }
            return temp;
        });
        return transData.apply(result);
    }

    /**
     * 查询有效的试题内容
     *
     * @param questionId
     * @return
     */
    private BaseQuestion findVaildQuestion(long questionId) {
        //删除状态的试题也能查到
        BaseQuestion question = baseQuestionMapper.selectByPrimaryKey(questionId);
        if (null == question) {
            ReflectQuestion reflection = reflectQuestionDao.findById(new Long(questionId).intValue());
            if (null != reflection) {
                question = baseQuestionMapper.selectByPrimaryKey(new Long(reflection.getNewId()));
            } else {
                throwBizException("无有效试题,ID=" + questionId);
            }
        }
        return question;
    }

    @Override
    public List<Map<String, Long>> countQuestionGroupBySubject() {
        return baseQuestionMapper.countGroupBySubject();
    }

    @Override
    public QuestionServiceV1 choiceService(QuestionInfoEnum.QuestionSaveTypeEnum saveType) {
        switch (saveType) {
            case OBJECTIVE:
                return objectiveQuestionService;
            case JUDGE:
                return judgeQuestionService;
            case SUBJECTIVE:
                return subjectiveQuestionService;
            case COMPOSITE:
                return compositeQuestionService;
        }
        return null;
    }

    @Override
    @Transactional
    public Long insertQuestion(BaseQuestion baseQuestion) {
        QuestionInfoEnum.QuestionSaveTypeEnum saveType = QuestionInfoEnum.getSaveTypeByQuestionType(baseQuestion.getQuestionType());
        if (QuestionInfoEnum.QuestionSaveTypeEnum.COMPOSITE.equals(saveType)) {
            baseQuestion.setMultiFlag(BaseInfo.YESANDNO.YES.getCode());
            baseQuestion.setMultiId(0L);
        }
        //插入初始状态，未发布
        if (baseQuestion.getBizStatus() == null || baseQuestion.getBizStatus() <= 0) {
            baseQuestion.setBizStatus(BizStatusEnum.NO_PUBLISH.getValue());
        }
        //是否废弃
        if (baseQuestion.getAvailFlag() == null || baseQuestion.getAvailFlag() <= 0) {
            baseQuestion.setAvailFlag(QuestionInfoEnum.AvailableEnum.AVAILABLE.getCode());
        }
        //是否残缺
        if (baseQuestion.getMissFlag() == null || baseQuestion.getMissFlag() <= 0L) {
            baseQuestion.setMissFlag(QuestionInfoEnum.CompleteEnum.COMPLETE.getCode());
        }
        //是否特岗教师
        if (baseQuestion.getSpecialFlag() == null || baseQuestion.getSpecialFlag() <= 0L) {
            baseQuestion.setSpecialFlag(BaseInfo.YESANDNO.NO.getCode());
        }
        insert(baseQuestion);
        return baseQuestion.getId();
    }

    /**
     * 录入试题的其他属性（知识点，标签）
     *
     * @param insertQuestionReq
     */
    @Transactional
    public void insertQuestionCommonAttr(InsertQuestionReqV1 insertQuestionReq) throws BizException {
        Long questionId = insertQuestionReq.getId();
        Long subjectId = insertQuestionReq.getSubject();
        if (CollectionUtils.isNotEmpty(insertQuestionReq.getKnowledgeIds())) {
            List<Long> knowledgeIds = knowledgeSubjectService.choicesKnowledgeBySubject(insertQuestionReq.getKnowledgeIds(), subjectId);
            //试题知识点数据
            questionKnowledgeService.insertQuestionKnowledgeInfo(knowledgeIds, questionId);
        }
        //试题标签数据
        if (CollectionUtils.isNotEmpty(insertQuestionReq.getTags())) {
            tagService.insertBatchQuestionTags(insertQuestionReq.getTags(), questionId);
        }
    }

    /**
     * 删除试题
     *
     * @param questionId
     * @param modifierId
     * @return
     */
    @Override
    @Transactional
    public Object deleteQuestion(Long questionId, Long modifierId, Boolean isDuplicateFlag) throws BizException {
        if (questionId == null || questionId.longValue() <= 0) {
            throw new BizException(TeacherErrors.NOT_NULL_PARAM);
        }

        BaseQuestion baseQuestion = selectByPrimaryKey(questionId);
        log.info("删除试题,试题ID是:{}", questionId);
        /**
         * 删除单题判断是否绑定试卷，是否发布
         * 删除复合题，判断是否有子题
         */
        if (null != baseQuestion) {
            if (QuestionInfoEnum.getSaveTypeByQuestionType(baseQuestion.getQuestionType()).equals(QuestionInfoEnum.QuestionSaveTypeEnum.COMPOSITE)) {
                List<BaseQuestion> questions = findBaseChildren(questionId);
                if (CollectionUtils.isNotEmpty(questions)) {
                    throwBizException("该材料下有" + questions.size() + "道子题，删除材料需先将该材料下的" + questions.size() + "道子题删除");
                }
            } else {
                //去重功能的试题不需要校验是否绑定试卷和是否发布
                if (!isDuplicateFlag) {
                    //校验试题是否绑定试卷
                    this.checkIsBindingPaper(questionId);
                    //（校验）试题取消发布后才能删除试题
                    if (baseQuestion.getBizStatus().equals(BizStatusEnum.PUBLISH.getValue())) {
                        throw new BizException(TeacherErrors.NO_CANCEL_PUBLISH_QUESTIO);
                    }
                }
            }
            baseQuestion.setId(questionId);
            baseQuestion.setModifierId(modifierId);
            baseQuestion.setStatus(StatusEnum.DELETE.getValue());
            save(baseQuestion);
            //附表数据删除

            QuestionServiceV1 questionServiceV1 = choiceService(QuestionInfoEnum.getSaveTypeByQuestionType(baseQuestion.getQuestionType()));
            questionServiceV1.deleteQuestion(baseQuestion.getId());
            //试题标签数据删除
            tagService.deleteQuestionTagByQuestion(questionId, modifierId);
            //试题知识点数据删除
            questionKnowledgeService.deleteByQuestionId(questionId);
            //同步试题数据到mongo
            importService.sendQuestion2Mongo(questionId.intValue());
        }
        return questionId;
    }

    /**
     * 查询复合题所有子题的主表信息
     *
     * @param questionId
     * @return
     */
    private List<BaseQuestion> findBaseChildren(Long questionId) {
        Example example = new Example(BaseQuestion.class);
        example.and().andEqualTo("multiId", questionId);
        return selectByExample(example);
    }


    /**
     * 修改试题
     *
     * @param updateQuestionReq
     * @return
     * @throws BizException
     */
    @Override
    @Transactional
    public Object updateQuestion(UpdateQuestionReqV1 updateQuestionReq) throws BizException {
        //TODO 通过各个方面校验试题修改参数的准确性
        checkoutCommonAttr(updateQuestionReq);
        updateQuestionBase(updateQuestionReq);
        Map mapData = Maps.newHashMap();
        mapData.put("questionId", updateQuestionReq.getId());
        //mysql->mongo
        importService.sendQuestion2Mongo(updateQuestionReq.getId().intValue());
        //发送到ES查重库
        importService.sendQuestion2SearchForDuplicate(updateQuestionReq.getId());
        return mapData;
    }


    @Override
    @Transactional
    public Object deleteQuestionByFlag(Long questionId, Long modifierId, Boolean copyFlag) {
        //是否是去重功能调用的删除
        Boolean isDuplicateFlag = false;
        Map mapData = Maps.newHashMap();
        if (!copyFlag) {
            Object object = deleteQuestion(questionId, modifierId, isDuplicateFlag);
            mapData.put("questionId", object);
            return mapData;
        }
        //查询跟该试题使用同样复用数据的试题信息
        List<Map> questions = questionDuplicateService.findWithDuplicateByQuestionId(questionId);
        String questionIds = "";
        if (questions != null) {
            for (Map question : questions) {
                Object object = deleteQuestion(Long.parseLong(question.get("questionId").toString()), modifierId, isDuplicateFlag);
                questionIds += object;
            }
            mapData.put("questionId", questionIds);
            return mapData;
        }
        return mapData;
    }


    /**
     * 添加主表数据，并分流添加其他数据
     *
     * @param insertQuestionReq
     * @return
     */
    @Override
    @Transactional
    public Map insertQuestion(InsertQuestionReqV1 insertQuestionReq) {
        //检查各种题型共有参数是否完整
        checkoutCommonAttr(insertQuestionReq);
        //添加逻辑
        Map mapData = Maps.newHashMap();
        Integer type = insertQuestionReq.getQuestionType();
        QuestionInfoEnum.QuestionSaveTypeEnum saveType = QuestionInfoEnum.getSaveTypeByQuestionType(type);
        QuestionServiceV1 questionServiceV1 = choiceService(saveType);
        List<Long> grades = insertQuestionReq.getGrades();
        if (CollectionUtils.isEmpty(grades)) {
            BaseQuestion baseQuestion = new BaseQuestion();
            BeanUtils.copyProperties(insertQuestionReq, baseQuestion);
            baseQuestion.setSubjectId(insertQuestionReq.getSubject());
            //添加统计标签
            if (CollectionUtils.isNotEmpty(insertQuestionReq.getStatisticsTagList())) {
                baseQuestion.setStatisticsTag(JsonUtil.toJson(insertQuestionReq.getStatisticsTagList()));
            }
            //主表插入
            Long questionId = insertQuestion(baseQuestion);
            insertQuestionReq.setId(questionId);
            //复用数据插入
            questionServiceV1.insertQuestion(insertQuestionReq);
            //插入公共属性
            insertQuestionCommonAttr(insertQuestionReq);
            //mysql->mongo
            importService.sendQuestion2Mongo(questionId.intValue());
            //发送到ES查重库
            importService.sendQuestion2SearchForDuplicate(questionId);
            log.info("发送到ES查重库,questionId 是：{}", questionId);

            mapData.put("questionId", questionId);
        } else {
            List<Long> ids = Lists.newArrayList();
            for (Long grade : grades) {
                BaseQuestion baseQuestion = new BaseQuestion();
                BeanUtils.copyProperties(insertQuestionReq, baseQuestion);
                baseQuestion.setSubjectId(grade);
                if (CollectionUtils.isNotEmpty(insertQuestionReq.getStatisticsTagList())) {
                    baseQuestion.setStatisticsTag(JsonUtil.toJson(insertQuestionReq.getStatisticsTagList()));
                }
                Long questionId = insertQuestion(baseQuestion);
                insertQuestionReq.setId(questionId);
                //mysql->mongo
                importService.sendQuestion2Mongo(questionId.intValue());
                //发送到ES查重库
                importService.sendQuestion2SearchForDuplicate(questionId);
                questionServiceV1.insertQuestion(insertQuestionReq);
                ids.add(questionId);
            }
            mapData.put("questionId", ids);
        }
        return mapData;
    }

    /**
     * 如果非复合题，判断试题是否有知识点和难度字段
     *
     * @param questionReqV1
     */
    private void checkoutCommonAttr(QuestionReqV1 questionReqV1) {
        Integer type = questionReqV1.getQuestionType();
        QuestionInfoEnum.QuestionSaveTypeEnum saveType = QuestionInfoEnum.getSaveTypeByQuestionType(type);
        if (QuestionInfoEnum.QuestionSaveTypeEnum.COMPOSITE.equals(saveType) || SUBJECTIVE.equals(saveType)) {
            return;
        }
        if (questionReqV1.getDifficultyLevel() == null || questionReqV1.getDifficultyLevel() <= 0) {
            throwBizException("试题难度不能为空");
        }
        if (CollectionUtils.isEmpty(questionReqV1.getKnowledgeIds())) {
            throwBizException("试题知识点不能为空");
        }

        //校验保存的多个知识点,必须是三级以及以上知识点
        List<Long> knowledgeIds = questionReqV1.getKnowledgeIds();
        if (CollectionUtils.isNotEmpty(knowledgeIds)) {
            List<Knowledge> knowledgeInfoByKnowIds = knowledgeService.findKnowledgeInfoByKnowIds(knowledgeIds);
            if (CollectionUtils.isEmpty(knowledgeInfoByKnowIds)) {
                return;
            }
            List<Knowledge> collect = knowledgeInfoByKnowIds.stream().filter(knowledge -> knowledge.getLevel() < 3).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)) {
                throwBizException("绑定的知识点中，存在非三级知识点");
            }
        }
    }

    @Override
    public SelectQuestionRespV1 findQuestionInfo(Long questionId, boolean withParent) {
        BaseQuestion question = selectByPrimaryKey(questionId);
        if (question == null) {
            throw new BizException(TeacherErrors.NO_EXISTED_QUESTION);
        }
        /**
         * 如果是子题查询回显信息，返回复合题结构，子题只携带被查询的试题
         * @author huangqp
         * 根据原型编辑子题时，展示复合题属性，
         * 所以查询查询的时候，按照复合题的查询逻辑查询，之后删除其他子题，只保留需要查询的子题即可
         */
        if (question.getMultiId() > 0L && withParent) {
            return findChildWithParent(question);
        }
        Integer type = question.getQuestionType();
        QuestionServiceV1 questionServiceV1 = choiceService(QuestionInfoEnum.getSaveTypeByQuestionType(type));
        SelectQuestionRespV1 result = questionServiceV1.findQuestion(question);
        result.setSubject(question.getSubjectId());
        //回显示统计标签
        if (StringUtils.isNotEmpty(question.getStatisticsTag())) {
            result.setStatisticsTagList(JsonUtil.toList(question.getStatisticsTag(), String.class));
        }
        return result;
    }

    /**
     * 转换公式
     *
     * @param question
     */
    public void convertQuestionSpan2Img(SelectQuestionRespV1 question) {
        if (question instanceof SelectObjectiveQuestionRespV1) {
            ((SelectObjectiveQuestionRespV1) question).setAnalysis(HtmlConvertUtil.span2Img(((SelectObjectiveQuestionRespV1) question).getAnalysis(), false));
            ((SelectObjectiveQuestionRespV1) question).setExtend(HtmlConvertUtil.span2Img(((SelectObjectiveQuestionRespV1) question).getExtend(), false));
            ((SelectObjectiveQuestionRespV1) question).setStem(HtmlConvertUtil.span2Img(((SelectObjectiveQuestionRespV1) question).getStem(), false));
            List<String> choices = ((SelectObjectiveQuestionRespV1) question).getChoices().stream().map(i -> HtmlConvertUtil.span2Img(i, false)).collect(Collectors.toList());
            ((SelectObjectiveQuestionRespV1) question).setChoices(choices);
        } else if (question instanceof SelectJudgeQuestionRespV1) {
            ((SelectJudgeQuestionRespV1) question).setAnalysis(HtmlConvertUtil.span2Img(((SelectJudgeQuestionRespV1) question).getAnalysis(), false));
            ((SelectJudgeQuestionRespV1) question).setExtend(HtmlConvertUtil.span2Img(((SelectJudgeQuestionRespV1) question).getExtend(), false));
            ((SelectJudgeQuestionRespV1) question).setStem(HtmlConvertUtil.span2Img(((SelectJudgeQuestionRespV1) question).getStem(), false));
        } else if (question instanceof SelectSubjectiveQuestionRespV1) {
            if (StringUtils.isNotBlank(((SelectSubjectiveQuestionRespV1) question).getAnalyzeQuestion())) {
                ((SelectSubjectiveQuestionRespV1) question).setAnalyzeQuestion(HtmlConvertUtil.span2Img(((SelectSubjectiveQuestionRespV1) question).getAnalyzeQuestion(), false));
            }
            ((SelectSubjectiveQuestionRespV1) question).setExtend(HtmlConvertUtil.span2Img(((SelectSubjectiveQuestionRespV1) question).getExtend(), false));
            ((SelectSubjectiveQuestionRespV1) question).setStem(HtmlConvertUtil.span2Img(((SelectSubjectiveQuestionRespV1) question).getStem(), false));
            ((SelectSubjectiveQuestionRespV1) question).setAnswerComment(HtmlConvertUtil.span2Img(((SelectSubjectiveQuestionRespV1) question).getAnswerComment(), false));
        } else if (question instanceof SelectCompositeQuestionRespV1) {
            for (MaterialReq materialReq : question.getMaterials()) {
                if (StringUtils.isNotBlank(materialReq.getContent())) {
                    materialReq.setContent(HtmlConvertUtil.span2Img(materialReq.getContent(), false));
                }
            }
            List<SelectQuestionRespV1> children = ((SelectCompositeQuestionRespV1) question).getChildren();
            if (CollectionUtils.isNotEmpty(children)) {
                children.forEach(child -> convertQuestionSpan2Img(child));
            }
        }
    }

    /**
     * 查询子题信息并携带复合题属性（即复合题结构，只携带被查询的子题）
     *
     * @param question
     * @return
     */
    private SelectQuestionRespV1 findChildWithParent(BaseQuestion question) {
        SelectQuestionRespV1 questionInfo = findQuestionInfo(question.getMultiId(), false);
        if (questionInfo instanceof SelectCompositeQuestionRespV1) {
            List<SelectQuestionRespV1> children = ((SelectCompositeQuestionRespV1) questionInfo).getChildren();
            children.removeIf(i -> !i.getId().equals(question.getId()));
            ((SelectCompositeQuestionRespV1) questionInfo).setChildren(children);
        }
        return questionInfo;
    }


    @Override
    public List<SelectQuestionRespV1> findChildren(Long parentId) {
        List<BaseQuestion> baseQuestions = findBaseChildren(parentId);
        if (CollectionUtils.isEmpty(baseQuestions)) {
            return Lists.newArrayList();
        }
        List<SelectQuestionRespV1> results = Lists.newArrayList();
        for (BaseQuestion baseQuestion : baseQuestions) {
            results.add(findQuestionInfo(baseQuestion.getId(), false));
        }
        return results;
    }

    /**
     * 逻辑删除试卷主表信息，其他关联信息逻辑删除(迁移数据使用)
     *
     * @param id
     */
    @Override
    @Transactional
    public void deleteQuestionPhysical(Integer id) {
        Long questionId = new Long(id);
        Long modifierId = -9L;
        baseQuestionMapper.deleteByPrimaryKey(questionId);
        //附表数据删除
        Example delDuplicateExample = new Example(QuestionDuplicate.class);
        delDuplicateExample.and().andEqualTo("questionId", questionId);
        questionDuplicateService.deleteByExample(delDuplicateExample);
        //试题标签数据删除
        tagService.deleteQuestionTagByQuestion(questionId, modifierId);
        //试题知识点数据删除
        questionKnowledgeService.deleteByQuestionId(questionId);
        //发送到ES查重库
        importService.sendQuestion2SearchForDuplicate(questionId);
    }

    /**
     * 校验试题是否绑定试卷
     *
     * @param questionId 试题ID
     * @return
     */
    public void checkIsBindingPaper(Long questionId) {
        Example example = new Example(PaperQuestion.class);
        example.and().andEqualTo("questionId", questionId);
        List<PaperQuestion> questionList = paperQuestionService.selectByExample(example);

        Map<Integer, List<Long>> listMap = questionList.stream()
                .collect((Collectors.groupingBy(PaperQuestion::getPaperType,
                        Collectors.mapping(PaperQuestion::getPaperId, Collectors.toList()))));
        if (listMap == null || listMap.size() == 0) {
            return;
        }
        List<String> paperNameList = new ArrayList<>();
        if (listMap.get(PaperInfoEnum.TypeInfo.ENTITY.getCode()) != null) {
            Example paperExample = new Example(PaperEntity.class);
            paperExample.and().andIn("id", listMap.get(PaperInfoEnum.TypeInfo.ENTITY.getCode()));
            List<PaperEntity> paperEntities = paperEntityService.selectByExample(paperExample);
            if (CollectionUtils.isNotEmpty(paperEntities)) {
                paperNameList.addAll(paperEntities.stream().map(i -> i.getName()).collect(Collectors.toList()));
            }
        }
        if (listMap.get(PaperInfoEnum.TypeInfo.SIMULATION.getCode()) != null) {
            List<PaperActivity> paperActivities = paperActivityService.selectByIds(listMap.get(PaperInfoEnum.TypeInfo.SIMULATION.getCode()));
            if (CollectionUtils.isNotEmpty(paperActivities)) {
                paperNameList.addAll(paperActivities.stream().map(i -> i.getName()).collect(Collectors.toList()));
            }
        }
        if (CollectionUtils.isNotEmpty(paperNameList)) {
            StringBuffer paperNameStr = new StringBuffer();
            paperNameStr.append("该试题被 ");
            paperNameStr.append(paperNameList.stream().collect(Collectors.joining(",")));
            paperNameStr.append(" 绑定,请先将该试题从试卷中解绑!");
            throw new BizException(ErrorResult.create(1000012, paperNameStr.toString()));
        }
    }


    /**
     * 批量发布试题,并且同步到Mongo库
     *
     * @param
     * @param status
     * @return
     */
    @Override
    @Transactional
    public Object updateQuestionBizStatusBatch(List<Long> questionIds, Integer status) {
        if (CollectionUtils.isEmpty(questionIds)) {
            return null;
        }
        //修改试题的发布状态
        BaseQuestion updateModel = BaseQuestion.builder()
                .bizStatus(status)
                .build();
        Example example = new Example(BaseQuestion.class);
        example.and().andIn("id", questionIds);
        updateByExampleSelective(updateModel, example);
        //mysql->mongo
        importService.sendQuestion2Mongo(questionIds.stream().map(Long::intValue).collect(Collectors.toList()));
        return SuccessMessage.create("试题发布成功");
    }

    @Override
    @Transactional
    public List<String> clearDuplicate(long newId, long oldId) {
        BaseQuestion newQuestion = selectByPrimaryKey(newId);
        if (null == newQuestion) {
            throwBizException("试题" + newId + "不存在");
        }
        BaseQuestion oldQuestion = selectByPrimaryKey(oldId);
        if (null == oldQuestion) {
            throwBizException("试题" + oldId + "不存在");
        }
        //科目校验
        if (!newQuestion.getSubjectId().equals(oldQuestion.getSubjectId())) {
            throwBizException("去重的两道题的所属科目不同");
        }
        QuestionInfoEnum.QuestionPartTypeEnum questionPartTypeEnumNew = QuestionInfoEnum.QuestionPartTypeEnum.create(newQuestion);
        QuestionInfoEnum.QuestionPartTypeEnum questionPartTypeEnumOld = QuestionInfoEnum.QuestionPartTypeEnum.create(oldQuestion);
        //试题类型校验
        List<String> paperNames = Lists.newArrayList();
        switch (questionPartTypeEnumNew) {
            case SINGLE: {
                if (questionPartTypeEnumNew.equals(questionPartTypeEnumOld)) {
                    //替换绑定关系，并获得所有需要调序是试卷名称
                    replaceSingleQuestion(oldId, newId, paperNames);
                } else {
                    throwBizException("不能用" + questionPartTypeEnumNew.getValue() + "去替换" + questionPartTypeEnumOld.getValue());
                }
                break;
            }
            case COMPOSITE: {
                if (questionPartTypeEnumNew.equals(questionPartTypeEnumOld)) {
                    //替换复合题部分，子题合并到一起，但是不影响试卷题序
                    replaceCompositeQuestion(oldId, newId);
                } else {
                    throwBizException("不能用" + questionPartTypeEnumNew.getValue() + "去替换" + questionPartTypeEnumOld.getValue());
                }
                break;
            }
            case CHILD: {
                if (questionPartTypeEnumNew.equals(questionPartTypeEnumOld)) {
                    if (newQuestion.getMultiId().equals(oldQuestion.getMultiId())) {
                        //替换复合题部分，子题合并到一起，但是不影响试卷题序
                        replaceSingleQuestion(oldId, newId, paperNames);
                    } else {
                        throwBizException("不能在不同的复合题之间做子题替换,请先将复合题" + newQuestion.getMultiId() + "和" + oldQuestion.getMultiId() + "实现替换和子题合并");
                    }
                } else if (QuestionInfoEnum.QuestionPartTypeEnum.SINGLE.equals(questionPartTypeEnumOld)) {
                    //单题可以被子题替换（适用于某些复合题的子题错录成单题的情况）
                    replaceSingleQuestion(oldId, newId, paperNames);
                } else {
                    throwBizException("不能用" + questionPartTypeEnumNew.getValue() + "去替换" + questionPartTypeEnumOld.getValue());
                }
                break;
            }

            default:
                throwBizException("其他类型去重还未开发，请联系开发");
        }

        //替换试题映射表
        reflectQuestionDao.insertRelation(new Long(oldId).intValue(), newId);
        importService.sendQuestion2Mongo(new Long(oldId).intValue());
        importService.sendQuestion2Mongo(new Long(newId).intValue());
        return paperNames;
    }

    /**
     * 复合题ID之间的替换
     *
     * @param oldId
     * @param newId
     */
    private void replaceCompositeQuestion(long oldId, long newId) {
        Example example = new Example(BaseQuestion.class);
        example.and().andEqualTo("multiId", oldId);
        List<BaseQuestion> baseQuestions = selectByExample(example);
        //切换子题的父节点，并重新同步所有子题
        if (CollectionUtils.isNotEmpty(baseQuestions)) {
            for (BaseQuestion baseQuestion : baseQuestions) {
                baseQuestion.setMultiId(newId);
                save(baseQuestion);
            }
            importService.sendQuestion2Mongo(baseQuestions.stream().map(BaseQuestion::getId).map(Long::intValue).collect(Collectors.toList()));
        }
        //删除被替换的试题
        updateQuestionBizStatus(oldId, BizStatusEnum.NO_PUBLISH.getValue());
        deleteQuestion(oldId, -1L, false);
    }

    /**
     * 替换试卷试题绑定关系，并得到需要调整题序的试卷名称
     *
     * @param oldId
     * @param newId
     * @param paperNames
     */
    private void replaceSingleQuestion(long oldId, long newId, List<String> paperNames) {
        List<PaperQuestion> oldRelations = paperQuestionService.findByQuestionId(oldId);
        //旧的关联关系是否存在
        if (CollectionUtils.isNotEmpty(oldRelations)) {
            List<PaperQuestion> newRelations = paperQuestionService.findByQuestionId(newId);
            ArrayList<Long> entityIds = Lists.newArrayList();
            ArrayList<Long> activityIds = Lists.newArrayList();
            for (PaperQuestion oldRelation : oldRelations) {
                Long id = replacePaperBinds(oldRelation, newRelations, newId);
                if (id > 0 && oldRelation.getPaperType().equals(PaperInfoEnum.TypeInfo.ENTITY.getCode())) {
                    entityIds.add(id);
                } else if (id > 0 && oldRelation.getPaperType().equals(PaperInfoEnum.TypeInfo.SIMULATION.getCode())) {
                    activityIds.add(id);
                }
            }
            if (CollectionUtils.isNotEmpty(entityIds)) {
                Example entityExample = new Example(PaperEntity.class);
                entityExample.and().andIn("id", entityIds);
                List<PaperEntity> paperEntities = paperEntityService.selectByExample(entityExample);
                if (CollectionUtils.isNotEmpty(paperEntities)) {
                    paperNames.addAll(paperEntities.stream().map(PaperEntity::getName).map(i -> i + "（实体卷）").collect(Collectors.toList()));
                }
            }
            if (CollectionUtils.isNotEmpty(activityIds)) {
                Example example = new Example(PaperActivity.class);
                example.and().andIn("id", activityIds);
                List<PaperActivity> paperActivities = paperActivityService.selectByExample(example);
                if (CollectionUtils.isNotEmpty(paperActivities)) {
                    paperNames.addAll(paperActivities.stream().map(PaperActivity::getName).map(i -> i + "（活动卷）").collect(Collectors.toList()));
                }
            }
        }
        //删除被替换的试题
        updateQuestionBizStatus(oldId, BizStatusEnum.NO_PUBLISH.getValue());
        deleteQuestion(oldId, -1L, false);
    }

    /**
     * 替换旧的绑定关系
     *
     * @param oldRelation  被替换的试题的绑定关系
     * @param newRelations 替换试题的绑定关系
     * @param newId
     * @return
     */
    private Long replacePaperBinds(PaperQuestion oldRelation, List<PaperQuestion> newRelations, long newId) {
        //去重的两道试题存在相同的试卷下，则删除旧的试题的绑定关系即可
        Optional<PaperQuestion> first = newRelations.stream().filter(i -> i.getPaperId().equals(oldRelation.getPaperId()))
                .filter(i -> i.getPaperType().equals(oldRelation.getPaperType())).findFirst();
        //解绑试题
        paperQuestionService.deletePaperQuestionInfo(oldRelation.getPaperId(), PaperInfoEnum.TypeInfo.create(oldRelation.getPaperType()), oldRelation.getQuestionId());
        //如果不存在统一试卷的试题，则新增绑定关系
        if (!first.isPresent()) {
            paperQuestionService.savePaperQuestionWithSort(newId, oldRelation.getPaperId(), oldRelation.getModuleId(), oldRelation.getSort(),
                    oldRelation.getScore(), null, PaperInfoEnum.TypeInfo.create(oldRelation.getPaperType()));
        } else {
            return first.get().getPaperId();
        }
        return -1L;
    }


    @Override
    public List<BaseQuestion> findByIds(List<Long> questionIds) {
        Example questionExample = new Example(BaseQuestion.class);
        questionExample.and().andIn("id", questionIds);
        List<BaseQuestion> baseQuestions = selectByExample(questionExample);
        return baseQuestions;
    }

    /**
     * 判断类（单选,多选,不定选项）必须为大写字母
     * 单选题,答案只有一个
     * 多选题,答案必须大于等于两个
     */
    private void checkJudgeQuestion(Map<String, Object> resultMap, int questionTye) {

        if (null == resultMap.get("答案") | null == resultMap) {
            return;
        }
        String answer = resultMap.get("答案").toString();
        if (QuestionInfoEnum.getSaveTypeByQuestionType(questionTye)
                == QuestionInfoEnum.QuestionSaveTypeEnum.OBJECTIVE) {
            String regex = "^[A-Z]+$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(answer);
            if (!matcher.find()) {
                throwBizException("选择类题目答案必须为大写字母");
            }
            if (questionTye == QuestionInfoEnum.QuestionTypeEnum.SINGLE.getKey()) {
                if (answer.length() > 1) {
                    throwBizException("单选题,答案必须为一个");
                }
            }
            if (questionTye == QuestionInfoEnum.QuestionTypeEnum.MULTI.getKey()) {
                if (answer.length() < 2)
                    throwBizException("多选题,答案必须大于等于两个");
            }
        }
    }
}

