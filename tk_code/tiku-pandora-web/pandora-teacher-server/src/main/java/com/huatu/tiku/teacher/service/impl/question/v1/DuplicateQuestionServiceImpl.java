package com.huatu.tiku.teacher.service.impl.question.v1;

import com.huatu.tiku.dto.DuplicateQuestionVo;
import com.huatu.tiku.entity.duplicate.QuestionDuplicate;
import com.huatu.tiku.entity.subject.Subject;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.enums.SubjectInfoEnum;
import com.huatu.tiku.response.question.DuplicatePartResp;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.springboot.basic.subject.SubjectService;
import com.huatu.tiku.teacher.dao.question.DuplicatePartProviderMapper;
import com.huatu.tiku.teacher.dao.subject.SubjectMapper;
import com.huatu.tiku.teacher.enums.ActivityTagEnum;
import com.huatu.tiku.teacher.enums.StatusEnum;
import com.huatu.tiku.teacher.service.question.DuplicateQuestionService;
import com.huatu.tiku.teacher.service.question.QuestionSearchService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.tiku.util.html.HtmlConvertUtil;
import com.huatu.tiku.util.http.ResponseMsg;
import com.huatu.ztk.commons.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/9/10
 * @描述 处理试题查重相关逻辑
 */
@Slf4j
@Service
public class DuplicateQuestionServiceImpl extends BaseServiceImpl<QuestionDuplicate>
        implements DuplicateQuestionService {

    public DuplicateQuestionServiceImpl() {
        super(QuestionDuplicate.class);
    }


    @Autowired
    private QuestionSearchService questionSearchService;

    @Autowired
    CommonQuestionServiceV1 teacherQuestionService;

    @Value("${duplicateEsUrl}")
    private String duplicateEsUrl;

    @Autowired
    DuplicatePartProviderMapper duplicatePartProvider;

    @Autowired
    SubjectMapper subjectMapper;


    /**
     * 根据题型做去重查询
     *
     * @return 复用内容
     */
    public List<DuplicatePartResp> findDuplicatePartFromEs(DuplicateQuestionVo duplicateQuestionVo,
                                                           int page, int size, Long subjectId, int score) {
        if (null == duplicateQuestionVo) {
            throwBizException("参数不能为空");
        }
        HashMap<String, Object> params = new HashMap<>();
        if (duplicateQuestionVo.getQuestionType() == null) {
            throwBizException("试题类型不能为空");
        }
        int type = duplicateQuestionVo.getQuestionType();
        String sendChoices = "";
        if (StringUtils.isNotEmpty(duplicateQuestionVo.getChoices())) {
            sendChoices = duplicateQuestionVo.getChoices();
        }
        QuestionInfoEnum.QuestionDuplicateTypeEnum duplicateType = QuestionInfoEnum.getDuplicateTypeByQuestionType(type);
        if (duplicateType.equals(QuestionInfoEnum.QuestionDuplicateTypeEnum.JUDGE_OBJECT)) {
            params.put("choices", sendChoices);
            params.put("analysis", duplicateQuestionVo.getAnalysis());
            //pc暂时未传递
            params.put("answer", duplicateQuestionVo.getAnswerComment());
        } else if (duplicateType.equals(QuestionInfoEnum.QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE)) {
            params.put("analysis", duplicateQuestionVo.getAnalyzeQuestion());
            params.put("answer", duplicateQuestionVo.getAnswerComment());
        } else {
            throwBizException("题型参数异常：type=" + type);
        }

        //组装查重条件,试题类型,(题干，选项，答案，解析),试题状态
        params.put("stem", duplicateQuestionVo.getStem());
        params.put("questionType", type);
        params.put("status", StatusEnum.NORMAL.getValue());
        params.put("page", page);
        params.put("size", size);
        //评分
        params.put("score", score);

        RestTemplate restTemplate = new RestTemplate();
        log.info("发送ES参数是：{}", params);
        ResponseMsg<LinkedHashMap> responseMsg = restTemplate.postForObject(duplicateEsUrl, params, ResponseMsg.class);
        log.info("url is :{}", duplicateEsUrl);
        if (responseMsg.getCode() != 1000000) {
            throwBizException(responseMsg.getMsg());
        }
        //log.info("数据内容是：{}", responseMsg.getData());
        String content = responseMsg.getData().get("content").toString();
        List<Long> questionIds = JsonUtil.toList(content, Long.class);
        return assembleQuestionInfo(questionIds, type);
    }


    /**
     * 根据试题ID获取试题信息
     *
     * @param
     * @param questionIds
     * @return
     */
    public List<DuplicatePartResp> assembleQuestionInfo(List<Long> questionIds, int questionType) {
        List<DuplicatePartResp> objectiveInfo = new ArrayList<>();
        if (CollectionUtils.isEmpty(questionIds)) {
            return objectiveInfo;
        }

        String ids = questionIds.stream().map(questionId -> String.valueOf(questionId)).collect(Collectors.joining(","));
        QuestionInfoEnum.QuestionDuplicateTypeEnum duplicateType = QuestionInfoEnum.getDuplicateTypeByQuestionType(questionType);
        //复合题不支持查重
        if (questionType == QuestionInfoEnum.QuestionTypeEnum.COMPOSITE.getCode() ||
                questionType == QuestionInfoEnum.QuestionTypeEnum.COMPOSITE_SUBJECTIVE.getCode()) {
            throwBizException("试题类型" + questionType + "暂未处理");
        }
        if (duplicateType.equals(QuestionInfoEnum.QuestionDuplicateTypeEnum.JUDGE_OBJECT)) {
            objectiveInfo = duplicatePartProvider.buildObjectiveInfo(ids, questionType);
        } else if (duplicateType.equals(QuestionInfoEnum.QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE)) {
            objectiveInfo = duplicatePartProvider.buildSubjectiveInfo(ids, questionType);
        } else {
            throwBizException("试题类型" + questionType + "暂未处理");
        }

        log.info("试题信息是:{}", objectiveInfo);
        objectiveInfo.stream().map(question -> {
            String choices = question.getChoicesStr();
            String answer = question.getAnswer();
            question.setStemShow(HtmlConvertUtil.span2Img(question.getStem(), false));
            question.setExtendShow(HtmlConvertUtil.span2Img(question.getExtend(), false));
            question.setAnalysisShow(HtmlConvertUtil.span2Img(question.getAnalysis(), false));
            question.setAnswerShow(HtmlConvertUtil.span2Img(question.getAnswer(), false));

            //选项
            if (null != choices && !choices.equals("")) {
                List<String> choicesList = HtmlConvertUtil.parseChoices(choices);
                question.setChoices(choicesList);
                question.setChoicesShow(choicesList.stream().map(j -> HtmlConvertUtil.span2Img(j, false)).collect(Collectors.toList()));
            }
            //答案
            if (null != question.getAnswer()) {
                //如果是判断题，改变答案为0/1
                if (questionType == QuestionInfoEnum.QuestionTypeEnum.JUDGE.getCode()) {
                    answer = "A".equals(answer) ? "1" : "0";
                    question.setAnswer(answer);
                }
            }
            question.setSubjectShow(getSubjectName(question));
            return question;
        }).collect(Collectors.toList());

        return getOrderQuestion(objectiveInfo, questionIds);

    }

    /**
     * 科目名称
     *
     * @return
     */
    public String getSubjectName(DuplicatePartResp question) {
        Example subjectExample = new Example(Subject.class);
        StringBuffer subjectName = new StringBuffer();
        if (question != null) {
            subjectExample.and().andEqualTo("id", Long.parseLong(question.getParent()));
            subjectExample.and().andEqualTo("level", SubjectInfoEnum.SubjectLevel.LEVEL_ONE.getCode());
            Subject subject = subjectMapper.selectOneByExample(subjectExample);
            subjectName.append(subject.getName()).append("-");
        }
        subjectName.append(question.getSubjectName());
        return subjectName.toString();
    }

    /**
     * 按照入参排序
     *
     * @param objectiveInfo
     * @param questionIds
     * @return
     */
    private List<DuplicatePartResp> getOrderQuestion(List<DuplicatePartResp> objectiveInfo,
                                                     List<Long> questionIds) {
        //按照入参排序
        List<DuplicatePartResp> resultList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(objectiveInfo)) {
            for (Long questionId : questionIds) {
                List<DuplicatePartResp> collect = objectiveInfo.stream().filter(i -> i.getQuestionId().equals(questionId)).collect(Collectors.toList());
                resultList.addAll(collect);
            }
        }
        return resultList;

    }

}
