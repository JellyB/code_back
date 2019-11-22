package com.huatu.tiku.teacher.controller.admin.question;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.enums.BaseInfo;
import com.huatu.tiku.enums.EnumUtil;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.request.question.v1.BaseQuestionSearchReq;
import com.huatu.tiku.teacher.enums.BizStatusEnum;
import com.huatu.tiku.teacher.service.impl.knowledge.KnowledgeSubjectInstance;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import com.huatu.tiku.teacher.service.question.QuestionSearchService;
import com.huatu.tiku.util.question.StringMatch;
import com.huatu.ztk.question.util.QuestionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 试题基础属性 - 提供基础试题信息
 * Created by lijun on 2018/7/17
 */
@Slf4j
@RestController
@RequestMapping("baseQuestion")
public class BaseQuestionSearchController {

    @Autowired
    private QuestionSearchService questionSearchService;

    @Autowired
    private KnowledgeService knowledgeService;


    @GetMapping(value = "find", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object queryById(@RequestParam Integer id) {
        BaseQuestionSearchReq baseQuestionSearchReq = new BaseQuestionSearchReq();
        baseQuestionSearchReq.setQuestionId(id);
        PageInfo<HashMap<String, Object>> result = list(baseQuestionSearchReq);
        List<HashMap<String, Object>> list = result.getList();
        if (!CollectionUtils.isEmpty(list)) {
            transSingleQuestionInfo(list.get(0), id);
        }
        return result;
    }

    /**
     * 如果是子题查询，则只保留材料和子题本身，其他子题信息过滤掉
     * @param questionMap
     * @param id
     */
    private void transSingleQuestionInfo(HashMap<String, Object> questionMap, Integer id) {
        Integer questionId = MapUtils.getInteger(questionMap, "id");
        if (id.equals(questionId)) {
            return;
        }
        List<HashMap> children = (List<HashMap>) MapUtils.getObject(questionMap, "children");
        if (!CollectionUtils.isEmpty(children) && children.size() > 1) {
            List<HashMap> temp = children.stream().filter(question -> id.equals(MapUtils.getInteger(question, "id")))
                    .collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(temp)){
                questionMap.put("children",temp);
            }
        }

    }

    /**
     * 试题信息列表查询  默认字段使用 -1
     * TODO 难度条件暂未用上
     *
     * @return
     */
    @GetMapping(value = "list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PageInfo<HashMap<String, Object>> list(BaseQuestionSearchReq baseQuestionSearchReq) {
        //科目转换 - 匹配事业单位 跨科目查题目
        baseQuestionSearchReq.setSubject(KnowledgeSubjectInstance.getInstance().transChildrenSubjectToBase(baseQuestionSearchReq.getSubject()));

        if (StringUtils.isNotBlank(baseQuestionSearchReq.getContent())) {
            baseQuestionSearchReq.setContent(StringMatch.replaceNotChinese(baseQuestionSearchReq.getContent()));
        }

        //knowledgeId 信息转换
        String knowledgeIds = BaseInfo.isNotDefaultSearchValue(baseQuestionSearchReq.getKnowledgeId()) ? knowledgeService.getKnowledgeInfo(baseQuestionSearchReq.getSubject(), baseQuestionSearchReq.getKnowledgeId()) : BaseInfo.SEARCH_INPUT_DEFAULT;
        baseQuestionSearchReq.setKnowledgeIds(knowledgeIds);

        PageInfo<HashMap<String, Object>> objectPageInfo = PageHelper.startPage(baseQuestionSearchReq.getPage(), baseQuestionSearchReq.getSize())
                .doSelectPageInfo(
                        () -> questionSearchService.list(baseQuestionSearchReq)
                );
        List<HashMap<String, Object>> list = objectPageInfo.getList();
        if (CollectionUtils.isEmpty(list)) {
            return objectPageInfo;
        }
        //数据信息转换
        Function<HashMap, HashMap<String, Object>> translateData = (question) -> {
            if (null != question.get("questionType")) {
                int resultQuestionType = Integer.valueOf(question.get("questionType").toString());
                String descriptionByValue = EnumUtil.valueOf(resultQuestionType, QuestionInfoEnum.QuestionTypeEnum.class);
                question.put("questionTypeName", descriptionByValue);
            }
            if (null != question.get("bizStatus")) {
                Integer bizStatusValue = Integer.valueOf(question.get("bizStatus").toString());
                BizStatusEnum bizStatusEnum = BizStatusEnum.create(bizStatusValue);
                question.put("bizStatusName", bizStatusEnum != null ? bizStatusEnum.getTitle() : "未知");
            }
            question.computeIfPresent("availFlag", (key, value) -> value.toString().equals("1") ? QuestionInfoEnum.AvailableEnum.AVAILABLE.getCode() : QuestionInfoEnum.AvailableEnum.UNAVAILABLE.getCode());
            return question;
        };
        List<BaseQuestion> params = list.stream()
                .map(question ->
                        BaseQuestion.builder()
                                .id(Long.parseLong(question.get("qid").toString()))
                                .questionType(Integer.valueOf(question.get("questionType").toString()))
                                .multiId(Long.parseLong(question.get("multiId").toString()))
                                .build()
                )
                .collect(Collectors.toList());
        List<HashMap<String, Object>> simpleInfoList = questionSearchService.getQuestionSimpleInfoListReturnMap(params);
        simpleInfoList = simpleInfoList.stream()
                .map(translateData)
                .map(question -> {
                    question.computeIfPresent("children", (key, value) ->
                            ((List<HashMap<String, Object>>) value).stream()
                                    .map(translateData)
                                    .collect(Collectors.toList())
                    );
                    return question;
                })
                .collect(Collectors.toList());
        simpleInfoList.sort(Comparator.comparingLong(data -> -Long.valueOf(String.valueOf(data.getOrDefault("id", 0)))));
        //复合题子题正排序
        simpleInfoList = simpleInfoList.stream().map(question -> {
            if (question.get("questionType").equals(QuestionInfoEnum.QuestionTypeEnum.COMPOSITE.getKey())) {
                if (null != question.get("children")) {
                    List<HashMap> mapList = (List<HashMap>) question.get("children");
                    mapList = mapList.stream().sorted(Comparator.comparing(map -> Integer.valueOf(map.get("id").toString()))).collect(Collectors.toList());
                    question.put("children", mapList);
                }
            }
            return question;
        }).collect(Collectors.toList());
        objectPageInfo.setList(simpleInfoList);
        return objectPageInfo;
    }

    /**
     * 根据ID 批量获取试题数据
     */
    @GetMapping(value = "listAllByQuestionId")
    public Object listAllByQuestionId(@RequestParam("questionIds") String questionIds) {
        List<Long> questionList = Arrays.stream(questionIds.split(","))
                .map(Long::valueOf)
                .collect(Collectors.toList());
        return questionSearchService.listAllByQuestionId(questionList);
    }
}
