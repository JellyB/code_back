package com.huatu.tiku.teacher.mode;

import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.request.question.v1.InsertCommonQuestionReqV1;
import com.huatu.tiku.teacher.enums.DifficultyLevelEnum;
import org.assertj.core.util.Lists;

import java.util.List;

/**
 * Created by huangqp on 2018\7\17 0017.
 */
public class JudgeQuestionT implements QuestionT{
    private final static Long subject = 1L;
    private final static Integer questionType = QuestionInfoEnum.QuestionTypeEnum.JUDGE.getCode();
    private final static Integer difficult = DifficultyLevelEnum.GENERAL.getValue();
    private final static List<Long> tags = Lists.newArrayList(156L,157L);
    private final static List<Long> knowledgeIds = Lists.newArrayList(394L,395L);
    private final static String stem = "题干内容";
    private final static String analysis = "解析内容";
    private final static String extend = "扩展内容";
    private final static String answer = "0";
//    private final static List<String> choices =  Lists.newArrayList("<p>选项A</p>","<p>选项B</p>","<p>选项C</p>","<p>选项D</p>");
    @Override
    public InsertCommonQuestionReqV1 assertInsertCommonQuestion(){
        InsertCommonQuestionReqV1 insertCommonQuestionReqV1 = new InsertCommonQuestionReqV1();
        insertCommonQuestionReqV1.setQuestionType(questionType);
        insertCommonQuestionReqV1.setSubject(subject);
        insertCommonQuestionReqV1.setDifficultyLevel(difficult);
        insertCommonQuestionReqV1.setTags(tags);
        insertCommonQuestionReqV1.setKnowledgeIds(knowledgeIds);
        insertCommonQuestionReqV1.setStem(stem);
//        insertCommonQuestionReqV1.setChoices(choices);
        insertCommonQuestionReqV1.setAnalysis(analysis);
        insertCommonQuestionReqV1.setExtend(extend);
        insertCommonQuestionReqV1.setAnswer(answer);
        return insertCommonQuestionReqV1;
    }
}

