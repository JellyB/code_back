package com.huatu.tiku.teacher.controller;

import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.dto.DuplicateQuestionVo;
import com.huatu.tiku.request.question.v1.InsertCommonQuestionReqV1;
import com.huatu.tiku.request.question.v1.UpdateCommonQuestionReqV1;
import com.huatu.tiku.response.question.DuplicatePartResp;
import com.huatu.tiku.response.question.v1.SelectObjectiveQuestionRespV1;
import com.huatu.tiku.teacher.controller.admin.question.TeacherQuestionControllerV1;
import com.huatu.tiku.teacher.mode.BindingResultVO;
import com.huatu.tiku.teacher.mode.CompositeQuestionT;
import com.huatu.tiku.teacher.mode.QuestionT;
import com.huatu.ztk.commons.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Created by huangqp on 2018\7\14 0014.
 */
@Slf4j
public class TeacherQuestionControllerT extends TikuBaseTest {
    @Autowired
    TeacherQuestionControllerV1 teacherQuestionControllerV1;
    private final static Long subject = 1L;
    private static BindingResultVO bindingResultVO = new BindingResultVO();

    @Test
    public void testTotal() {
        try {
            createObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @Transactional
    public void createObject() throws Exception {
        System.out.println("====================开始添加======================");
        QuestionT questionT = new CompositeQuestionT();
        InsertCommonQuestionReqV1 insertCommonQuestionReqV1 = questionT.assertInsertCommonQuestion();
        log.info("subject={},insertCommonQuestionReqV1={}", subject, insertCommonQuestionReqV1);
        DuplicateQuestionVo duplicateQuestionVo = new DuplicateQuestionVo();

        duplicateQuestionVo.setQuestionType(insertCommonQuestionReqV1.getQuestionType());
        duplicateQuestionVo.setChoices(null);
        duplicateQuestionVo.setStem(insertCommonQuestionReqV1.getStem());
        duplicateQuestionVo.setAnalysis(insertCommonQuestionReqV1.getAnalysis());
        duplicateQuestionVo.setAnalysis(insertCommonQuestionReqV1.getExtend());
        duplicateQuestionVo.setAnswerComment(insertCommonQuestionReqV1.getAnswerComment());
        duplicateQuestionVo.setAnalyzeQuestion(insertCommonQuestionReqV1.getAnalyzeQuestion());
        duplicateQuestionVo.setAnswerRequest(insertCommonQuestionReqV1.getAnswerRequest());
        duplicateQuestionVo.setBestowPointExplain(insertCommonQuestionReqV1.getBestowPointExplain());
        duplicateQuestionVo.setTrainThought(insertCommonQuestionReqV1.getTrainThought());
        duplicateQuestionVo.setOmnibusRequirements(insertCommonQuestionReqV1.getOmnibusRequirements());

        //List<DuplicatePartResp> duplicates = teacherQuestionControllerV1.findDuplicates(duplicateQuestionVo);
        System.out.println("+++++++++++++++++++++查重++++++++++++++++++++");
        //System.out.println("duplicates=:" + JsonUtil.toJson(duplicates));
        Object question = teacherQuestionControllerV1.createQuestion(insertCommonQuestionReqV1, bindingResultVO);
        System.out.println("====================添加完成======================");
        String id = ((Map) question).get("questionId").toString();
        log.info("subject={},questionId={}", subject, id);
        log.info("subject={},createQuestion={}", subject, JsonUtil.toJson(question));
        Long questionId = Long.parseLong(id);
        findQuestion(questionId);
        System.out.println("====================开始修改======================");
        UpdateCommonQuestionReqV1 updateCommonQuestionReqV1 = new UpdateCommonQuestionReqV1();
        BeanUtils.copyProperties(insertCommonQuestionReqV1, updateCommonQuestionReqV1);
        System.out.println("====================修改难度======================");
        updateCommonQuestionReqV1.setDifficultyLevel(4);
        log.info("subject={},old={},new={}", subject, insertCommonQuestionReqV1.getDifficultyLevel(), updateCommonQuestionReqV1.getDifficultyLevel());
        Object object = teacherQuestionControllerV1.updateQuestion(updateCommonQuestionReqV1, bindingResultVO);
        System.out.println("====================修改难度完成======================");
        log.info("subject={},createQuestion={}", subject, JsonUtil.toJson(object));

        System.out.println("====================修改标签======================");
        updateCommonQuestionReqV1.setTags(Lists.newArrayList(156L, 157L, 160L));
        log.info("subject={},old={},new={}", subject, insertCommonQuestionReqV1.getTags(), updateCommonQuestionReqV1.getTags());
        object = teacherQuestionControllerV1.updateQuestion(updateCommonQuestionReqV1, bindingResultVO);
        System.out.println("====================修改标签完成======================");
        log.info("subject={},createQuestion={}", subject, JsonUtil.toJson(object));

        System.out.println("====================修改知识点======================");
        updateCommonQuestionReqV1.setKnowledgeIds(Lists.newArrayList(394L, 395L, 396L));
        log.info("subject={},old={},new={}", subject, insertCommonQuestionReqV1.getKnowledgeIds(), updateCommonQuestionReqV1.getKnowledgeIds());
        object = teacherQuestionControllerV1.updateQuestion(updateCommonQuestionReqV1, bindingResultVO);
        System.out.println("====================修改标签完成======================");
        log.info("subject={},createQuestion={}", subject, JsonUtil.toJson(object));

        System.out.println("====================修改复用数据======================");
        updateCommonQuestionReqV1.setStem("题干被修改");
        log.info("subject={},old={},new={}", subject, insertCommonQuestionReqV1.getStem(), updateCommonQuestionReqV1.getStem());
        object = teacherQuestionControllerV1.updateQuestion(updateCommonQuestionReqV1, bindingResultVO);
        System.out.println("====================修改复用数据完成======================");
        log.info("subject={},createQuestion={}", subject, JsonUtil.toJson(object));

        System.out.println("====================核对试题状态，并切换成另一种状态======================");
        Object question1 = findQuestion(questionId);
        if (question1 instanceof SelectObjectiveQuestionRespV1) {
            Integer bizStatus = ((SelectObjectiveQuestionRespV1) question1).getBizStatus();
            Integer availFlag = ((SelectObjectiveQuestionRespV1) question1).getAvailFlag();
            Integer missFlag = ((SelectObjectiveQuestionRespV1) question1).getMissFlag();
            log.info("subject={},bizStatus={},availFlag={},missFlag={}", subject, bizStatus, availFlag, missFlag);
            teacherQuestionControllerV1.changeQuestionBizStatus(id, bizStatus == 1 ? 2 : 1);
//            teacherQuestionControllerV1.changeQuestionAvailable(id,availFlag==1?2:1);
            teacherQuestionControllerV1.changeQuestionMissFlag(id, missFlag == 1 ? 2 : 1);
        }
        System.out.println("====================检查试题状态，是否切换成另一种状态======================");
        question1 = findQuestion(questionId);
        if (question1 instanceof SelectObjectiveQuestionRespV1) {
            Integer bizStatus = ((SelectObjectiveQuestionRespV1) question1).getBizStatus();
            Integer availFlag = ((SelectObjectiveQuestionRespV1) question1).getAvailFlag();
            Integer missFlag = ((SelectObjectiveQuestionRespV1) question1).getMissFlag();
            log.info("subject={},bizStatus={},availFlag={},missFlag={}", subject, bizStatus, availFlag, missFlag);
        }

        System.out.println("====================删除试题======================");

        //teacherQuestionControllerV1.deleteQuestion(questionId,false);
        findQuestion(questionId);
    }

    public Object findQuestion(Long questionId) {
        System.out.println("====================查询结果======================");
        Object question = teacherQuestionControllerV1.findById(questionId, true);
        log.info("subject={},question={}", subject, JsonUtil.toJson(question));
        return question;
    }

}

