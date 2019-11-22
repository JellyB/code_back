package com.huatu.tiku.teacher.service.impl.question;

import com.google.common.collect.Lists;
import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.entity.question.QuestionSimpleInfo;
import com.huatu.tiku.enums.EnumUtil;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.request.question.v1.BaseQuestionSearchReq;
import com.huatu.tiku.teacher.dao.question.BaseQuestionSearchMapper;
import com.huatu.tiku.teacher.enums.BizStatusEnum;
import com.huatu.tiku.teacher.enums.StatusEnum;
import com.huatu.tiku.teacher.service.impl.knowledge.KnowledgeComponent;
import com.huatu.tiku.teacher.service.question.QuestionSearchService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.tiku.util.html.HtmlConvertUtil;
import com.huatu.ztk.question.common.QuestionStatus;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/7/16
 */
@Service
public class QuestionSearchServiceImpl implements QuestionSearchService {

    @Autowired
    public BaseQuestionSearchMapper searchMapper;

    @Autowired
    public CommonQuestionServiceV1 commonQuestionServiceV1;

    @Autowired
    public KnowledgeComponent knowledgeComponent;

    @Override
    public List<HashMap<String, Object>> list(BaseQuestionSearchReq baseQuestionSearchReq) {
        return searchMapper.list(baseQuestionSearchReq);
}

    @Override
    public HashMap getQuestionSimpleInfo(long questionId, int questionType) {
        List<HashMap<String, Object>> searchMapperQuestionSimpleInfo = searchMapper.getQuestionSimpleInfo(questionId, questionType);
        HashMap<String, Object> questionSimpleInfo = mergeData(searchMapperQuestionSimpleInfo);
        spanInfo2Img(questionSimpleInfo);
        questionSimpleInfo.put("children", getMultiChildren(questionId, questionType));
        return questionSimpleInfo;
    }

    @Override
    public List<HashMap<String, Object>> getQuestionSimpleInfoListReturnMap(List<BaseQuestion> params) {
        if (null == params || params.size() == 0) {
            return Lists.newArrayList();
        }
        List<Long> baseQuestionIdList = params.stream().map(BaseQuestion::getId).collect(Collectors.toList());
        return getQuestionSimpleInfoListReturnMap(params, baseQuestionIdList);
    }

    @Override
    public List<HashMap<String, Object>> getQuestionSimpleInfoListReturnMap(final List<BaseQuestion> params, List<Long> baseQuestionId) {
        if (null == params || params.size() == 0) {
            return Lists.newArrayList();
        }
        List<HashMap<String, Object>> simpleInfoForList = searchMapper.getQuestionSimpleInfoForList(params);
        Map<String, List<HashMap<String, Object>>> listMap = simpleInfoForList.stream()
                .collect(Collectors.groupingBy(map -> map.get("id").toString()));
        List<HashMap<String, Object>> collect = listMap.values().stream()
                .map(dataList -> {
                    //处理字段信息
                    HashMap<String, Object> map = mergeData(dataList);
                    spanInfo2Img(map);
                    //knowledgeId 转换成 名称
                    if (StringUtils.isNotBlank(MapUtils.getString(map, ("knowledgeIds")))) {
                        List<String> knowledgeName = Arrays.stream(MapUtils.getString(map, ("knowledgeIds")).split(","))
                                .map(Long::valueOf)
                                .map(knowledgeComponent::getParentUtilRoot)
                                .map((knowledgeList) -> {
                                            Collections.reverse(knowledgeList);
                                            return knowledgeList.stream()
                                                    .map(Knowledge::getName)
                                                    .collect(Collectors.joining("-"));

                                        }
                                )
                                .collect(Collectors.toList());
                        map.put("knowledgeName", knowledgeName);
                    }
                    return map;
                })
                .map(questionInfo -> {
                    //处理子题情况
                    Optional<BaseQuestion> question = params.stream().filter(baseQuestion ->
                            baseQuestion.getId().equals(MapUtils.getLong(questionInfo, "id", 0L))
                                    && baseQuestion.getQuestionType().equals(MapUtils.getInteger(questionInfo, "questionType", -1))
                    ).findFirst();
                    if (question.isPresent()) {
                        BaseQuestion baseQuestion = question.get();
                        List<HashMap<String, Object>> multiChildren = getMultiChildren(baseQuestion.getId(), baseQuestion.getQuestionType());
                        //1.如果当前的复合题ID存在查询的试题ID列表中 则默认查询该复合题的所有子题，否则会根据子题过滤
                        if (baseQuestionId.stream().anyMatch(questionId -> questionId.equals(MapUtils.getLong(questionInfo, "id", -1L)))) {
                            questionInfo.put("children", multiChildren);
                        } else {
                            questionInfo.put("children", multiChildren.stream()
                                    .filter(children -> baseQuestionId.stream().anyMatch(questionId -> questionId.equals(MapUtils.getLong(children, "id", -1L))))
                                    .collect(Collectors.toList()));
                        }
                    } else {
                        questionInfo.put("children", Lists.newArrayList());
                    }
                    return questionInfo;
                })
                .collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<QuestionSimpleInfo> getQuestionSimpleInfoListReturnObject(List<BaseQuestion> params, List<Long> baseQuestionId) {
        List<HashMap<String, Object>> infoListReturnMap = getQuestionSimpleInfoListReturnMap(params, baseQuestionId);

        Function<HashMap, QuestionSimpleInfo> trans = (map) -> {
            QuestionSimpleInfo questionSimpleInfo = QuestionSimpleInfo.builder()
                    .id(MapUtils.getLong(map, "id"))
                    .questionType(MapUtils.getInteger(map, "questionType"))
                    .bizStatus(MapUtils.getInteger(map, "bizStatus"))
                    .availFlag(MapUtils.getInteger(map, "availFlag"))
                    .missFlag(MapUtils.getInteger(map, "missFlag"))
                    .source(MapUtils.getString(map, "source"))
                    .stem(MapUtils.getString(map, "stem"))
                    .answer(MapUtils.getString(map, "answer"))
                    .mode(MapUtils.getInteger(map, "mode"))
                    .analyze(MapUtils.getString(map, "analyze"))
                    .extend(MapUtils.getString(map, "extend"))
                    .status(StatusEnum.NORMAL.getValue())
                    .build();
            //拼接成为新的内容
            //根据试题类型ID 获取试题类型名称
            if (null != questionSimpleInfo.getQuestionType()) {
                String questionTypeName = EnumUtil.valueOf(questionSimpleInfo.getQuestionType(), QuestionInfoEnum.QuestionTypeEnum.class);
                questionSimpleInfo.setQuestionTypeName(questionTypeName);
            }
            //获取是否是费题,有用费题
            if (null != questionSimpleInfo.getAvailFlag()) {
                String availableName = EnumUtil.valueOf(questionSimpleInfo.getAvailFlag(), QuestionInfoEnum.AvailableEnum.class);
                questionSimpleInfo.setAvailFlagName(availableName);
            }
            //残缺
            if (null != questionSimpleInfo.getMissFlag()) {
                String missFlagName = EnumUtil.valueOf(questionSimpleInfo.getMissFlag(), QuestionInfoEnum.CompleteEnum.class);
                questionSimpleInfo.setMissFlagName(missFlagName);
            }
            //发布，未发布
            if (null != questionSimpleInfo.getBizStatus()) {
                BizStatusEnum bizStatusEnum = BizStatusEnum.create(questionSimpleInfo.getBizStatus());
                questionSimpleInfo.setBizStatusName(bizStatusEnum != null ? bizStatusEnum.getTitle() : "未知");
            }
            //选项内容
            if (null != map.get("choices")) {
                questionSimpleInfo.setChoices((List<String>) map.get("choices"));
            }
            //材料内容
            if (null != map.get("materialContent")) {
                questionSimpleInfo.setMaterialContent((List<String>) map.get("materialContent"));
            }
            //处理知识点相关信息
            if (StringUtils.isNotBlank(MapUtils.getString(map, ("knowledgeIds")))) {
                List<Long> knowledgeIdList = Arrays.stream(MapUtils.getString(map, ("knowledgeIds")).split(","))
                        .map(Long::valueOf)
                        .collect(Collectors.toList());
                questionSimpleInfo.setKnowledgeIds(knowledgeIdList);

                List<String> knowledgeName = (List<String>) map.getOrDefault("knowledgeName", new ArrayList<String>());
                questionSimpleInfo.setKnowledgeName(knowledgeName);
            }

            return questionSimpleInfo;
        };

        List<QuestionSimpleInfo> result = infoListReturnMap.parallelStream()
                .map(map -> {
                    QuestionSimpleInfo questionSimpleInfo = trans.apply(map);
                    if (null != map.get("children")) {
                        List<QuestionSimpleInfo> children = ((List<HashMap<String, Object>>) map.get("children")).stream()
                                .map(trans)
                                .collect(Collectors.toList());
                        questionSimpleInfo.setChildren(children);
                    }
                    return questionSimpleInfo;
                })
                .collect(Collectors.toList());
        return result;
    }

    @Override
    public List<QuestionSimpleInfo> getQuestionSimpleInfoListReturnObject(List<BaseQuestion> params) {
        if (null == params || params.size() == 0) {
            return Lists.newArrayList();
        }
        List<Long> baseQuestionIdList = params.stream().map(BaseQuestion::getId).collect(Collectors.toList());
        return getQuestionSimpleInfoListReturnObject(params, baseQuestionIdList);
    }

    /**
     * 解决多材料问题 - 多个材料合并成一个材料
     */
    private static HashMap<String, Object> mergeData(List<HashMap<String, Object>> mapList) {
        if (mapList.size() == 0) {
            return HashMapBuilder.<String, Object>newBuilder().build();
        }
        HashMap<String, Object> map = mapList.get(0);
        if (mapList.size() == 1) {
            ArrayList<Object> list = Lists.newArrayList();
            if (null != map.get("materialContent")) {
                list.add(HtmlConvertUtil.span2Img(map.get("materialContent").toString(), false));
            }
            map.put("materialContent", list);
        } else {
            List<String> materialContentList = mapList.stream()
                    .filter(data -> data.get("materialContent") != null)
                    .map(data -> data.get("materialContent").toString())
                    .map(stem -> HtmlConvertUtil.span2Img(stem, false))
                    .collect(Collectors.toList());
            map.put("materialContent", materialContentList);
        }
        return map;
    }

    @Override
    public List<QuestionSimpleInfo> listAllByQuestionId(List<Long> questionIds) {
        if (CollectionUtils.isEmpty(questionIds)) {
            return Lists.newArrayList();
        }
        //此处的查询会把子题ID 转换成父类ID
        List<BaseQuestion> params = searchMapper.listAllByQuestionId(questionIds).stream()
                .map(question ->
                        BaseQuestion.builder()
                                .id(Long.parseLong(question.get("qid").toString()))
                                .questionType(Integer.valueOf(question.get("questionType").toString()))
                                .multiId(Long.parseLong(question.get("multiId").toString()))
                                .build()
                )
                .collect(Collectors.toList());
        return getQuestionSimpleInfoListReturnObject(params, questionIds);
    }

    @Override
    public List<HashMap<String, Object>> findQuestionSource(List<Long> questionIds) {
        List<HashMap<String, Object>> simpleInfoForList = searchMapper.getQuestionSourceForList(questionIds);
        return simpleInfoForList;
    }

    @Override
    public String findSingleQuestionSource(Long questionId) {
        List<HashMap<String, Object>> questionSourceInfo = searchMapper.getQuestionSourceForList(Lists.newArrayList(questionId));
        if(CollectionUtils.isEmpty(questionSourceInfo)){
            return "";
        }
        String source = MapUtils.getString(questionSourceInfo.get(0),"source");
        System.out.println("questionId = " + questionId + "|" + "source = " + source);
        return source;
    }

    /**
     * 转换 选项 和 题干中的信息
     */
    private static void spanInfo2Img(HashMap<String, Object> map) {
        if (null != map.get("choices")) {
            List<String> choiceList = HtmlConvertUtil.parseChoices(map.get("choices").toString());
            map.put("choices", choiceList.stream().map(stem -> HtmlConvertUtil.span2Img(stem, false)).collect(Collectors.toList()));
        }
        map.computeIfPresent("stem", (key, value) -> HtmlConvertUtil.span2Img(value.toString(), false));
        map.computeIfPresent("answer", (key, value) -> HtmlConvertUtil.span2Img(value.toString(), false));
        map.computeIfPresent("analyze", (key, value) -> HtmlConvertUtil.span2Img(value.toString(), false));
        map.computeIfPresent("extend", (key, value) -> HtmlConvertUtil.span2Img(value.toString(), false));
    }

    /**
     * 处理复合题子题的情况
     */
    private List<HashMap<String, Object>> getMultiChildren(long questionId, int questionType) {
        int saveType = QuestionInfoEnum.getSaveTypeByQuestionType(questionType).getCode();
        boolean isMulti = saveType == QuestionInfoEnum.QuestionSaveTypeEnum.COMPOSITE.getCode();
        if (isMulti) {
            WeekendSqls<BaseQuestion> weekendSql = WeekendSqls.custom();
            weekendSql.andEqualTo(BaseQuestion::getMultiId, questionId);
            Example example = Example.builder(BaseQuestion.class)
                    .where(weekendSql)
                    .build();
            List<BaseQuestion> questionList = commonQuestionServiceV1.selectByExample(example);
            List<HashMap<String, Object>> questionSimpleInfoForList = getQuestionSimpleInfoListReturnMap(questionList);
            return questionSimpleInfoForList;
        }
        return Lists.newArrayList();
    }
}
