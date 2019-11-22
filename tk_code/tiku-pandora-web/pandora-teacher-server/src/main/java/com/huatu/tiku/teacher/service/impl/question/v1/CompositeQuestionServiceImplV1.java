package com.huatu.tiku.teacher.service.impl.question.v1;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.SuccessMessage;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.constants.teacher.TeacherErrors;
import com.huatu.tiku.entity.duplicate.QuestionDuplicate;
import com.huatu.tiku.entity.duplicate.SubjectiveDuplicatePart;
import com.huatu.tiku.entity.material.Material;
import com.huatu.tiku.entity.material.QuestionMaterial;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.enums.BaseInfo;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.request.material.MaterialReq;
import com.huatu.tiku.request.question.v1.*;
import com.huatu.tiku.response.question.v1.SelectCompositeQuestionRespV1;
import com.huatu.tiku.response.question.v1.SelectQuestionRespV1;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.service.duplicate.SubjectiveDuplicatePartService;
import com.huatu.tiku.teacher.service.material.QuestionMaterialService;
import com.huatu.tiku.teacher.service.material.TeacherMaterialService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.tiku.teacher.service.question.v1.QuestionServiceV1;
import com.huatu.tiku.util.file.HtmlFileUtil;
import com.huatu.tiku.util.html.HtmlConvertUtil;
import com.huatu.ztk.question.bean.CompositeQuestion;
import com.huatu.ztk.question.bean.CompositeSubjectiveQuestion;
import com.huatu.ztk.question.bean.Question;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\6\28 0028.
 */
@Slf4j
@Service
public class CompositeQuestionServiceImplV1 extends BaseServiceImpl<QuestionDuplicate> implements QuestionServiceV1 {
    @Autowired
    SubjectiveDuplicatePartService subjectiveDuplicatePartService;
    @Autowired
    HtmlFileUtil htmlFileUtil;
    @Autowired
    CommonQuestionServiceV1 commonQuestionServiceV1;
    @Autowired
    TeacherMaterialService materialService;
    @Autowired
    QuestionMaterialService questionMaterialService;

    public CompositeQuestionServiceImplV1() {
        super(QuestionDuplicate.class);
    }

    /**
     * 修改复合题部分信息
     *
     * @param question
     */
    @Override
    @Transactional
    public void updateQuestion(UpdateQuestionReqV1 question) {
        UpdateCommonQuestionReqV1 updateCommonQuestionReqV1 = (UpdateCommonQuestionReqV1) question;
        UpdateCompositeQuestionReqV1 compositeQuestionRep = new UpdateCompositeQuestionReqV1();
        BeanUtils.copyProperties(updateCommonQuestionReqV1, compositeQuestionRep);

        checkUpdateQuestionStyle(compositeQuestionRep);
        //格式转换
        compositeQuestionRep.setOmnibusRequirements(htmlFileUtil.html2DB(compositeQuestionRep.getOmnibusRequirements(), false));
        for (MaterialReq materialReq : compositeQuestionRep.getMaterials()) {
            if (StringUtils.isNotBlank(materialReq.getContent())) {
                materialReq.setContent(htmlFileUtil.html2DB(materialReq.getContent(), true));
            }
        }

        //复用表信息处理(如果没有复用id，直接添加，否则不动复用数据（添加试题的时候不允许复用数据修改）)
        SubjectiveDuplicatePart subjectiveDuplicatePart = new SubjectiveDuplicatePart();
        BeanUtils.copyProperties(compositeQuestionRep, subjectiveDuplicatePart);
        if (compositeQuestionRep.getDuplicateId() == null || compositeQuestionRep.getDuplicateId() <= 0) {
            subjectiveDuplicatePartService.insertWithFilter(subjectiveDuplicatePart);
            compositeQuestionRep.setDuplicateId(subjectiveDuplicatePart.getId());
        } else {
            subjectiveDuplicatePart.setId(compositeQuestionRep.getDuplicateId());
            subjectiveDuplicatePartService.updateWithFilter(subjectiveDuplicatePart);
        }
        QuestionDuplicate questionDuplicate = QuestionDuplicate.builder().duplicateType(QuestionInfoEnum.QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE.getCode()).duplicateId(compositeQuestionRep.getDuplicateId()).questionId(compositeQuestionRep.getId()).build();
        Example example = new Example(QuestionDuplicate.class);
        example.and().andEqualTo("questionId", question.getId());
        //试题复用表关联关系
        updateByExampleSelective(questionDuplicate, example);

        //修改材料相关内容
        materialService.updateQuestionMaterial(updateCommonQuestionReqV1.getMaterials(), question.getId());
    }

    /**
     * 修改参数检查
     *
     * @param compositeQuestionRep
     */
    private void checkUpdateQuestionStyle(UpdateCompositeQuestionReqV1 compositeQuestionRep) {
        List<MaterialReq> materials = compositeQuestionRep.getMaterials();
        if (CollectionUtils.isEmpty(materials)) {
            throwBizException("材料不能为空");
        }
    }

    /**
     * 复合题材料和复用数据(总括要求)插入
     *
     * @param insertQuestionReq
     * @return
     * @throws BizException
     */
    @Override
    @Transactional
    public Object insertQuestion(InsertQuestionReqV1 insertQuestionReq) throws BizException {

        //对象转化，格式判断
        InsertCommonQuestionReqV1 insertCommonQuestionReqV1 = (InsertCommonQuestionReqV1) insertQuestionReq;
        InsertCompositeQuestionReqV1 insertCompositeQuestionReqV1 = new InsertCompositeQuestionReqV1();
        BeanUtils.copyProperties(insertCommonQuestionReqV1, insertCompositeQuestionReqV1);

        checkInsertQuestionStyle(insertCompositeQuestionReqV1);
        insertCompositeQuestionReqV1.setOmnibusRequirements(htmlFileUtil.html2DB(insertCompositeQuestionReqV1.getOmnibusRequirements(), false));
        for (MaterialReq materialReq : insertCompositeQuestionReqV1.getMaterials()) {
            if (StringUtils.isNotBlank(materialReq.getContent())) {
                materialReq.setContent(htmlFileUtil.html2DB(materialReq.getContent(), true));
            }
        }

        //复用表信息处理(如果没有复用id，直接添加，否则不动复用数据（添加试题的时候不允许复用数据修改）)
        if (insertCompositeQuestionReqV1.getDuplicateId() == null || insertCompositeQuestionReqV1.getDuplicateId() <= 0) {
            SubjectiveDuplicatePart subjectiveDuplicatePart = new SubjectiveDuplicatePart();
            BeanUtils.copyProperties(insertCompositeQuestionReqV1, subjectiveDuplicatePart);
            subjectiveDuplicatePart.setId(null);
            subjectiveDuplicatePartService.insertWithFilter(subjectiveDuplicatePart);
            insertCompositeQuestionReqV1.setDuplicateId(subjectiveDuplicatePart.getId());
        }
        //复合题目的主题部分都保存在主观题目表中
        QuestionDuplicate questionDuplicate = QuestionDuplicate.builder().
                duplicateType(QuestionInfoEnum.QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE.getCode()).duplicateId(insertCompositeQuestionReqV1.getDuplicateId()).
                questionId(insertCompositeQuestionReqV1.getId()).build();
        //试题复用表关联关系
        insert(questionDuplicate);
        //材料部分逻辑逻辑处理
        materialService.insertQuestionMaterial(insertCompositeQuestionReqV1.getMaterials()
                , insertQuestionReq.getId());
        return SuccessMessage.create("复合题部分添加成功");
    }

    /**
     * 复合题添加逻辑校验
     *
     * @param insertCompositeQuestionReqV1
     */
    private void checkInsertQuestionStyle(InsertCompositeQuestionReqV1 insertCompositeQuestionReqV1) {
        List<MaterialReq> materials = insertCompositeQuestionReqV1.getMaterials();
        if (CollectionUtils.isEmpty(materials)) {
            throwBizException("材料不能为空");
        }
    }

    /**
     * 复合题迁移的过程中不会保留id,id重新生成
     *
     * @param question
     * @return
     */
    @Override
    @Transactional
    public InsertQuestionReqV1 assertInsertReq(Question question) {
        if (!(question instanceof CompositeQuestion || question instanceof CompositeSubjectiveQuestion)) {
            log.error("question not match GenericQuestion:>>>{}", question);
            throw new BizException(TeacherErrors.NOT_MATCH_QUESTION_SAVE_TYPE);
        }
        InsertCompositeQuestionReqV1 insertQuestionReq = new InsertCompositeQuestionReqV1();
        if (question instanceof CompositeQuestion) {
            CompositeQuestion compositeQuestion = (CompositeQuestion) question;
            insertQuestionReq.setQuestionType(compositeQuestion.getType());
            if (CollectionUtils.isNotEmpty(compositeQuestion.getMaterials())) {
                insertQuestionReq.setMaterials(compositeQuestion.getMaterials().stream().map(i -> MaterialReq.builder().content(i).build()).collect(Collectors.toList()));
            } else {
                insertQuestionReq.setMaterials(Lists.newArrayList(MaterialReq.builder().content(compositeQuestion.getMaterial()).build()));
            }
            insertQuestionReq.setDifficultyLevel(compositeQuestion.getDifficult());
            insertQuestionReq.setSubject(new Long(compositeQuestion.getSubject()));
            insertQuestionReq.setKnowledgeIds(Lists.newArrayList());
            insertQuestionReq.setStatus(question.getStatus() == 4 ? -1 : 1);
            insertQuestionReq.setCreatorId(question.getCreateBy());
        } else if (question instanceof CompositeSubjectiveQuestion) {
            CompositeSubjectiveQuestion compositeQuestion = (CompositeSubjectiveQuestion) question;
            insertQuestionReq.setQuestionType(compositeQuestion.getType());
            if (CollectionUtils.isNotEmpty(compositeQuestion.getMaterials())) {
                insertQuestionReq.setMaterials(compositeQuestion.getMaterials().stream().map(i -> MaterialReq.builder().content(i).build()).collect(Collectors.toList()));
            } else {
                insertQuestionReq.setMaterials(Lists.newArrayList(MaterialReq.builder().content(compositeQuestion.getMaterial()).build()));
            }
            insertQuestionReq.setOmnibusRequirements(compositeQuestion.getRequire());
            insertQuestionReq.setDifficultyLevel(compositeQuestion.getDifficult());
            insertQuestionReq.setSubject(new Long(compositeQuestion.getSubject()));
            insertQuestionReq.setKnowledgeIds(Lists.newArrayList());
            insertQuestionReq.setStatus(question.getStatus() == 4 ? -1 : 1);
            insertQuestionReq.setCreatorId(question.getCreateBy());
        }
        return insertQuestionReq;
    }

    /**
     * 查询复合题信息
     *
     * @param baseQuestion
     * @return
     */
    @Override
    @Transactional
    public SelectQuestionRespV1 findQuestion(BaseQuestion baseQuestion) {
        Long id = baseQuestion.getId();
        SelectCompositeQuestionRespV1 selectCompositeQuestionRespV1 = new SelectCompositeQuestionRespV1();
        QuestionDuplicate questionDuplicate = selectByQuestionId(id);
        if (questionDuplicate == null) {
            throw new BizException(TeacherErrors.NO_EXISTED_QUESTION);
        }
        BeanUtils.copyProperties(questionDuplicate, selectCompositeQuestionRespV1);
        SubjectiveDuplicatePart subjectiveDuplicatePart = subjectiveDuplicatePartService.selectByPrimaryKey(questionDuplicate.getDuplicateId());
        if (subjectiveDuplicatePart != null) {
            BeanUtils.copyProperties(subjectiveDuplicatePart, selectCompositeQuestionRespV1);
        }
        BeanUtils.copyProperties(baseQuestion, selectCompositeQuestionRespV1);
        selectCompositeQuestionRespV1.setId(baseQuestion.getId());
        List<SelectQuestionRespV1> children = commonQuestionServiceV1.findChildren(selectCompositeQuestionRespV1.getId());
        selectCompositeQuestionRespV1.setChildren(children);
        List<Material> materials = materialService.findByQuestionId(baseQuestion.getId());
        if (CollectionUtils.isNotEmpty(materials)) {
            List<MaterialReq> materialReqs = materials.stream()
                    .map(i -> MaterialReq.builder().content(i.getContent()).materialId(i.getId()).build())
                    .collect(Collectors.toList());
            selectCompositeQuestionRespV1.setMaterials(materialReqs);
        }
        return selectCompositeQuestionRespV1;
    }

    /**
     * 复用关系删除，材料关系删除
     *
     * @param questionId
     */
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
        return null;
    }

    @Override
    public SelectCompositeQuestionRespV1 assertQuestionReq(Long questionType, StringBuilder sb, Long subjectId, StringBuilder content) {
        return null;
    }

    @Override
    public SelectQuestionRespV1 convertMongoQuestion(Question question) {
        SelectCompositeQuestionRespV1 questionRespV1 = new SelectCompositeQuestionRespV1();
        questionRespV1.setMultiId(0L);
        //材料
        List<MaterialReq> materialReqs = Lists.newArrayList();
        if (CollectionUtils.isEmpty(question.getMaterials())) {
            question.setMaterials(Lists.newArrayList(question.getMaterial()));
        }
        for (String s : question.getMaterials()) {
            MaterialReq temp = MaterialReq.builder().content(s).build();
            materialReqs.add(temp);
        }
        questionRespV1.setMaterials(materialReqs);
        if (question instanceof CompositeSubjectiveQuestion) {
            questionRespV1.setOmnibusRequirements(((CompositeSubjectiveQuestion) question).getRequire());
        }
        return questionRespV1;
    }

    @Override
    public Object findDuplicateQuestion(Question question, Integer subjectFlag) {
        if (!(question instanceof CompositeQuestion || question instanceof CompositeSubjectiveQuestion)) {
            return null;
        }
        List<String> materials = question.getMaterials();
        if (CollectionUtils.isEmpty(materials)) {
            return null;
        }
        //材料和关联的复合题关系
        Map<Long, Double> questionSimilarMap = Maps.newHashMap();
        //所有查询到的材料与试题的绑定关系
        List<QuestionMaterial> questionMaterialList = Lists.newArrayList();
        //相似的材料数据处理
        for (String content : materials) {
            Map<Long, Double> materialSimilar = materialService.findDuplicate(content);
            if (materialSimilar != null && materialSimilar.size() != 0) {
                List<Long> ids = materialSimilar.keySet().stream().collect(Collectors.toList());
                Example example = new Example(QuestionMaterial.class);
                example.and().andIn("materialId", ids);
                List<QuestionMaterial> questionMaterials = questionMaterialService.selectByExample(example);
                if (CollectionUtils.isNotEmpty(questionMaterials)) {
                    questionMaterialList.addAll(questionMaterials);
                    for (QuestionMaterial questionMaterial : questionMaterials) {
                        Double percent = questionSimilarMap.getOrDefault(questionMaterial.getQuestionId(), 0D);
                        percent += materialSimilar.getOrDefault(questionMaterial.getMaterialId(), 0D);
                        questionSimilarMap.put(questionMaterial.getQuestionId(), percent);
                    }
                }
            }
        }
        if (CollectionUtils.isEmpty(questionMaterialList)) {
            return null;
        }
        List<Long> materialIds = questionMaterialList.stream().map(i -> i.getMaterialId()).distinct().collect(Collectors.toList());
        List<Material> materialList = materialService.findByIds(materialIds);
        if (CollectionUtils.isEmpty(materialList)) {
            return null;
        }
        Map<Long, Material> materialMap = materialList.stream().collect(Collectors.toMap(i -> i.getId(), i -> i));
        List<Long> questionIds = questionMaterialList.stream().map(i -> i.getQuestionId()).distinct().collect(Collectors.toList());
        Example example = new Example(BaseQuestion.class);
        example.and().andIn("id", questionIds);
        List<BaseQuestion> questions = commonQuestionServiceV1.selectByExample(example);
        //如果科目范围内搜索，则根据科目做筛选
        if (subjectFlag.equals(BaseInfo.YESANDNO.YES.getCode())) {
            questionIds = questions.stream().filter(i -> i.getSubjectId().intValue() == question.getSubject()).map(BaseQuestion::getId).collect(Collectors.toList());
        }
        List<SelectCompositeQuestionRespV1> selectCompositeQuestionRespV1s = Lists.newArrayList();
        for (Long questionId : questionIds) {
            SelectCompositeQuestionRespV1 selectCompositeQuestionRespV1 = new SelectCompositeQuestionRespV1();
            selectCompositeQuestionRespV1.setId(questionId);
            //筛选单个试题相关的材料
            List<QuestionMaterial> tempList = questionMaterialList.stream().filter(i -> i.getQuestionId().equals(questionId)).collect(Collectors.toList());
            tempList.sort(Comparator.comparing(i -> i.getSort() == null ? -1 : i.getSort()));
            selectCompositeQuestionRespV1.setMaterials(tempList.stream().map(i -> {
                Material material = materialMap.get(i.getMaterialId());
                String content = HtmlConvertUtil.span2Img(material.getContent(), false);
                return MaterialReq.builder().content(content).materialId(material.getId()).build();
            }).collect(Collectors.toList()));
            selectCompositeQuestionRespV1s.add(selectCompositeQuestionRespV1);
        }
        //按照相似度大小倒序排列
        selectCompositeQuestionRespV1s.sort(Comparator.comparing(i -> -questionSimilarMap.get(i.getId())));
        List<Map<String, Object>> mapData = selectCompositeQuestionRespV1s.stream()
                .map(BeanMap::new)
                .map(map -> {
                    HashMap<String, Object> tempMap = Maps.newHashMap();
                    tempMap.putAll(map);
                    tempMap.put("questionId", map.getOrDefault("id", ""));
                    List<String> delKey = Lists.newArrayList("id", "class");
                    //删除id和value为空的映射关系
                    for (Map.Entry<String, Object> entry : tempMap.entrySet()) {
                        if (entry.getValue() == null || "null".equals(entry.getValue())) {
                            delKey.add(entry.getKey());
                        }
                    }
                    delKey.forEach(key -> tempMap.remove(key));
                    return tempMap;
                })
                .collect(Collectors.toList());
        return mapData;
    }


    /**
     * 复合题部分解析成mongo数据
     *
     * @param question mysql-question
     * @return mongo-question (父类属性在下一步实现)
     */
    @Override
    public Question parseQuestion2MongoInfo(BaseQuestion question) {
        Example example = new Example(BaseQuestion.class);
        example.and().andEqualTo("multiId", question.getId());
        List<BaseQuestion> children = commonQuestionServiceV1.selectByExample(example);
        children.sort(Comparator.comparing(q -> q.getId()));

        QuestionInfoEnum.QuestionTypeEnum questionTypeEnum = QuestionInfoEnum.QuestionTypeEnum.COMPOSITE;
        for (BaseQuestion child : children) {
            QuestionInfoEnum.QuestionDuplicateTypeEnum duplicateTypeEnum = QuestionInfoEnum.getDuplicateTypeByQuestionType(child.getQuestionType());
            if (duplicateTypeEnum.equals(QuestionInfoEnum.QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE)) {
                questionTypeEnum = QuestionInfoEnum.QuestionTypeEnum.COMPOSITE_SUBJECTIVE;
                break;
            }
        }
        Integer type = questionTypeEnum.getCode();
        /**
         * 插入共有数据（材料和题型）
         */
        Function<Question, Question> transData = (q -> {
            q.setType(type);
            List<Material> materials = materialService.findByQuestionId(question.getId());
            if (CollectionUtils.isEmpty(materials)) {
                return q;
            }
            List<String> list = materials.stream().map(i -> i.getContent()).map(i -> HtmlConvertUtil.span2Img(i, true)).collect(Collectors.toList());
            q.setMaterials(list);
            q.setMaterial(StringUtils.join(list, "</br>"));
            return q;
        });
        //子题ID
        List<Integer> childIds = children.stream().map(i -> i.getId().intValue()).collect(Collectors.toList());
        if (questionTypeEnum.equals(QuestionInfoEnum.QuestionTypeEnum.COMPOSITE)) {
            CompositeQuestion compositeQuestion = new CompositeQuestion();
            compositeQuestion.setQuestions(childIds);
            compositeQuestion.setType(type);
            return transData.apply(compositeQuestion);
        } else {
            CompositeSubjectiveQuestion compositeSubjectiveQuestion = new CompositeSubjectiveQuestion();
            compositeSubjectiveQuestion.setQuestions(childIds);
            compositeSubjectiveQuestion.setType(type);
            return transData.apply(compositeSubjectiveQuestion);
        }
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

