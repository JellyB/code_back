package com.huatu.tiku.teacher.controller.admin.question;

import com.google.common.collect.Maps;
import com.huatu.common.ErrorResult;
import com.huatu.common.SuccessMessage;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.constants.teacher.TeacherErrors;
import com.huatu.tiku.dto.DuplicateQuestionVo;
import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.request.question.v1.InsertCommonQuestionReqV1;
import com.huatu.tiku.request.question.v1.UpdateCommonQuestionReqV1;
import com.huatu.tiku.response.question.DuplicatePartResp;
import com.huatu.tiku.response.question.v1.SelectQuestionRespV1;
import com.huatu.tiku.teacher.service.impl.knowledge.KnowledgeComponent;
import com.huatu.tiku.teacher.service.paper.PaperQuestionService;
import com.huatu.tiku.teacher.service.question.DuplicateQuestionService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.tiku.util.log.LogPrint;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.huatu.tiku.service.impl.BaseServiceImpl.throwBizException;

/**
 * Created by huangqp on 2018\6\27 0027.
 */
@Slf4j
@RestController
@RequestMapping("question/v1")
public class TeacherQuestionControllerV1 {

    @Autowired
    CommonQuestionServiceV1 teacherQuestionService;

    @Autowired
    DuplicateQuestionService duplicateQuestionService;

    @Autowired
    PaperQuestionService paperQuestionService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    private KnowledgeComponent knowledgeComponent;

    /**
     * 新增试题
     *
     * @param insertCommonQuestionReqV1 添加试题对象
     * @param bindingResult             参数格式校验处理对象
     * @return
     * @throws BizException
     */
    @LogPrint
    @PostMapping(value = "")
    public Object createQuestion(@Valid @RequestBody InsertCommonQuestionReqV1 insertCommonQuestionReqV1,
                                 BindingResult bindingResult) throws BizException {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BizException(ErrorResult.create(1000001, bindingResult.getAllErrors().get(0).getDefaultMessage()));
        }
        //校验公式合法性
      /*  boolean isLegal = checkFormula(insertCommonQuestionReqV1);
        if (!isLegal) {
            throw new BizException(ErrorResult.create(1000001, "请检查录入公式是否合法"));
        }*/

        insertCommonQuestionReqV1.setCreatorId(0L);
        return teacherQuestionService.insertQuestion(insertCommonQuestionReqV1);
    }

    private boolean checkFormula(Object obj) {

        for (Field f : obj.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            try {
                if (f.get(obj) != null && f.get(obj).toString().contains("placeholder")) {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 修改试题
     *
     * @param updateCommonQuestionReqV1 修改对象
     * @param bindingResult
     * @return
     * @throws BizException
     */
    @LogPrint
    @PutMapping(value = "")
    public Object updateQuestion(@Valid @RequestBody UpdateCommonQuestionReqV1 updateCommonQuestionReqV1,
                                 BindingResult bindingResult) throws BizException {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BizException(ErrorResult.create(1000001, bindingResult.getAllErrors().get(0).getDefaultMessage()));
        }
     /*   //校验公式合法性
        boolean isLegal = checkFormula(updateCommonQuestionReqV1);
        if (!isLegal) {
            throw new BizException(ErrorResult.create(1000001, "请检查录入公式是否合法"));
        }
*/
        updateCommonQuestionReqV1.setModifierId(1L);

        //过滤知识点上下级重复 A-B-C、A-B-C-D ==》A-B-C-D
        List<Long> knowledgeIds = updateCommonQuestionReqV1.getKnowledgeIds();
        if (CollectionUtils.isNotEmpty(knowledgeIds) && knowledgeIds.size() > 1) {
            Set<Long> set = knowledgeIds.stream()
                    .flatMap(knowledgeId -> {
                        List<Knowledge> parentUtilRoot = knowledgeComponent.getParentUtilRoot(knowledgeId);
                        return parentUtilRoot.subList(1, parentUtilRoot.size())
                                .stream()
                                .map(Knowledge::getId);
                    })
                    .collect(Collectors.toSet());
            List<Long> newKnowledgeIdList = knowledgeIds.stream()
                    .filter(knowledgeId -> !set.contains(knowledgeId))
                    .collect(Collectors.toList());
            updateCommonQuestionReqV1.setKnowledgeIds(newKnowledgeIdList);
        }
        return teacherQuestionService.updateQuestion(updateCommonQuestionReqV1);
    }

    /**
     * 查询试题属性
     *
     * @param id
     * @param withParent false 返回值不含父节点（针对复合题的材料部分）,只为展示使用，返回的表达式的转换过的img；
     *                   true  返回值修改回显使用，需要材料部分，返回的表达式不做转换
     * @return
     * @throws BizException
     */
    @LogPrint
    @GetMapping(value = "")
    public SelectQuestionRespV1 findById(@RequestParam Long id,
                                         @RequestParam(defaultValue = "true") boolean withParent) throws BizException {
        SelectQuestionRespV1 questionInfo = teacherQuestionService.findQuestionInfo(id, withParent);
        //转换表达式成img
      /* if (!withParent) {
        teacherQuestionService.convertQuestionSpan2Img(questionInfo);
       }*/
        return questionInfo;
    }

    /**
     * 删除试题
     *
     * @param questionId
     * @return
     * @throws BizException
     */
    @LogPrint
    @DeleteMapping(value = "/{questionId}")
    public Object deleteQuestion(@PathVariable Long questionId,
                                 @RequestParam(defaultValue = "false") Boolean copyFlag) throws BizException {
        Long modifierId = 0L;
        return teacherQuestionService.deleteQuestionByFlag(questionId, modifierId, copyFlag);
    }

    /**
     * 发布状态修改（1未发布2发布）
     *
     * @param questionIds
     * @param status
     * @return
     */
    @LogPrint
    @PostMapping(value = "publish")
    public Object changeQuestionBizStatus(@RequestParam String questionIds,
                                          @RequestParam Integer status) {
        if (status == null) {
            throw new BizException(TeacherErrors.ILLEGAL_PARAM);
        }
        String[] split = null;
        if (StringUtils.isNotEmpty(questionIds)) {
            split = questionIds.split(",");
        }
        if (split.length == 1) {
            return teacherQuestionService.updateQuestionBizStatus(Long.parseLong(split[0]), status);
        }
        int length = split.length;
        int count = 0;
        for (String s : split) {
            try {
                teacherQuestionService.updateQuestionBizStatus(Long.parseLong(s), status);
                count++;
            } catch (Exception e) {
                log.info("试题{}修改发布状态失败", s);
            }
        }
        if (count == 0) {
            throw new BizException(ErrorResult.create(1000002, "批量修改失败"));
        }
        return SuccessMessage.create("批量修改发布状态" + length + "道试题，有" + count + "成功");
    }

    /**
     * 试题作废或者取消作废（1取消作废2作废）
     *
     * @param questionIds
     * @param availFlag
     * @return
     */
    @LogPrint
    @PostMapping(value = "available")
    public Object changeQuestionAvailable(@RequestParam String questionIds,
                                          @RequestParam(defaultValue = "-1") Integer availFlag) {
        //作废枚举类转换
        QuestionInfoEnum.AvailableEnum availFlagEnum = QuestionInfoEnum.AvailableEnum.create(availFlag);
        if (QuestionInfoEnum.AvailableEnum.UNKNOWN_FLAG.equals(availFlag)) {
            throw new BizException(TeacherErrors.ILLEGAL_PARAM);
        }
        String[] split = questionIds.split(",");
        if (split.length == 1) {
            return teacherQuestionService.updateQuestionAvailable(Long.parseLong(split[0]), availFlagEnum);
        }
        int length = split.length;
        int count = 0;
        for (String s : split) {
            try {
                teacherQuestionService.updateQuestionAvailable(Long.parseLong(s), availFlagEnum);
                count++;
            } catch (Exception e) {
                log.info("试题{}修改废弃状态失败", s);
            }
        }
        if (count == 0) {
            throw new BizException(ErrorResult.create(1000002, "批量修改失败"));
        }
        return SuccessMessage.create("批量修改废弃状态" + length + "道试题，有" + count + "成功");
    }

    /**
     * 试题残缺标识修改（1残缺2正常）
     *
     * @param questionIds
     * @param missFlag
     * @return
     */
    @LogPrint
    @PostMapping(value = "incomplete")
    public Object changeQuestionMissFlag(@RequestParam String questionIds,
                                         @RequestParam(defaultValue = "-1") Integer missFlag) {
        QuestionInfoEnum.CompleteEnum completeEnum = QuestionInfoEnum.CompleteEnum.create(missFlag);
        if (completeEnum.equals(QuestionInfoEnum.CompleteEnum.UNKNOWN_FLAG)) {
            throw new BizException(TeacherErrors.ILLEGAL_PARAM);
        }
        String[] split = questionIds.split(",");
        if (split.length == 1) {
            return teacherQuestionService.updateQuestionStatus(Long.parseLong(split[0]), completeEnum);
        }
        int length = split.length;
        int count = 0;
        for (String s : split) {
            try {
                teacherQuestionService.updateQuestionStatus(Long.parseLong(s), completeEnum);
                count++;
            } catch (Exception e) {
                log.info("试题{}修改残缺标识失败", s);
            }
        }
        if (count == 0) {
            throw new BizException(ErrorResult.create(1000002, "批量修改失败"));
        }
        return SuccessMessage.create("批量修改残缺标识" + length + "道试题，有" + count + "成功");
    }

    /**
     * 根据题型做去重查询
     *
     * @return
     * @throws BizException
     */
    @LogPrint
    @PostMapping(value = "duplicate/list/es")
    public List<DuplicatePartResp> findDuplicates(@RequestBody DuplicateQuestionVo duplicateQuestionVo
    ) throws BizException {
        if (duplicateQuestionVo == null) {
            throw new BizException(TeacherErrors.ILLEGAL_PARAM);
        }
        Integer questionType = duplicateQuestionVo.getQuestionType();
        String choices = duplicateQuestionVo.getChoices();
        String stem = duplicateQuestionVo.getStem();
        String analysis = duplicateQuestionVo.getAnalysis();
        String extend = duplicateQuestionVo.getExtend();
        String answerComment = duplicateQuestionVo.getAnswerComment();
        String analyzeQuestion = duplicateQuestionVo.getAnalyzeQuestion();
        String answerRequest = duplicateQuestionVo.getAnswerRequest();
        String bestowPointExplain = duplicateQuestionVo.getBestowPointExplain();
        String trainThought = duplicateQuestionVo.getTrainThought();
        String omnibusRequirements = duplicateQuestionVo.getOmnibusRequirements();

        return teacherQuestionService.findDuplicatePart(choices, stem, analysis, extend, answerComment, analyzeQuestion,
                answerRequest, bestowPointExplain, trainThought, omnibusRequirements, questionType);

    }

    /**
     * 传入批量导入试题的文本，解析文本，返回试题对应的属性
     *
     * @param content
     * @param subjectId
     * @return
     */
    @LogPrint
    @PostMapping(value = "parse")
    public Object parseQuestionInfo(@RequestBody(required = false) HashMap<String, String> content,
                                    @RequestParam Long subjectId,
                                    @RequestParam(defaultValue = "-1") Long questionId) {
        String text = content.get("text");
        if (StringUtils.isBlank(text)) {
            throw new BizException(ErrorResult.create(10001231, "请录入需要解析的内容"));
        }
        return teacherQuestionService.parseQuestionInfo(text, subjectId,questionId);
    }

    /**
     * 通过试题ID，返回批量导入试题格式（批量编辑回显试题信息用到）
     *
     * @param questionId
     * @return
     */
    @LogPrint
    @GetMapping(value = "format")
    public Object formatQuestionInfo(@RequestParam Long questionId) {
        String result = teacherQuestionService.formatQuestionInfo(questionId);
        HashMap<Object, Object> map = Maps.newHashMap();
        map.put("content", result);
        return map;
    }

    /**
     * 替换试题（去重操作）
     * 如果是同一个试卷中两道题目做去重，且都占用了题序，则还需提示对试卷题序做调整
     *
     * @param newId
     * @param oldId
     * @return
     */
    @PostMapping(value = "duplicate/clear")
    public Object clearDuplicate(@RequestParam long newId,
                                 @RequestParam long oldId) {
        List<String> paperInfos = teacherQuestionService.clearDuplicate(newId, oldId);
        if (CollectionUtils.isNotEmpty(paperInfos)) {
            return SuccessMessage.create("去重成功，还需要对\"" + StringUtils.join(paperInfos, ",") + "\"试卷做题序调整");
        } else {
            return SuccessMessage.create("去重操作成功");
        }
    }


    /**
     * 根据题型做去重查询
     *
     * @return
     * @throws BizException
     */
    @LogPrint
    @PostMapping(value = "duplicate/list")
    public List<DuplicatePartResp> findDuplicates(@RequestBody DuplicateQuestionVo duplicateQuestionVo,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "5") int size,
                                                  @RequestParam(defaultValue = "-1") Long subject,
                                                  @RequestParam(defaultValue = "90") int score)
            throws BizException {

        if (duplicateQuestionVo == null) {
            throw new BizException(TeacherErrors.ILLEGAL_PARAM);
        }
        //暂时不按照科目来查询
        subject = -1L;
        return duplicateQuestionService.findDuplicatePartFromEs(duplicateQuestionVo, page, size, subject, score);
    }

    /**
     * 查询试题信息，并返回固定结构
     *
     * @param id
     * @return
     */
    @LogPrint
    @GetMapping(value = "duplicatesInfo")
    public DuplicatePartResp getDuplicatesInfo(@RequestParam Long id) {
        if (id == null) {
            throw new BizException(TeacherErrors.ILLEGAL_PARAM);
        }
        List<Long> questionIds = new ArrayList<>();
        questionIds.add(id);
        BaseQuestion baseQuestion = teacherQuestionService.selectByPrimaryKey(id);
        if (baseQuestion == null) {
            throwBizException("试题信息不存在");
        }
        List<DuplicatePartResp> duplicatePartResps = duplicateQuestionService.assembleQuestionInfo(questionIds, baseQuestion.getQuestionType().intValue());
        if (CollectionUtils.isNotEmpty(duplicatePartResps)) {
            return duplicatePartResps.get(0);
        }
        return null;
    }

}

