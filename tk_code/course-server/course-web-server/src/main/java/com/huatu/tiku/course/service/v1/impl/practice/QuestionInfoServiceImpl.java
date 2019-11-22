package com.huatu.tiku.course.service.v1.impl.practice;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.huatu.tiku.course.bean.practice.QuestionInfo;
import com.huatu.tiku.course.service.v1.practice.QuestionInfoService;
import com.huatu.tiku.course.util.ZTKResponseUtil;
import com.huatu.tiku.course.ztk.api.v1.question.QuestionServiceV1;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2019/2/27
 */
@Service
@RequiredArgsConstructor
public class QuestionInfoServiceImpl implements QuestionInfoService {

    private final QuestionServiceV1 questionService;

    /**
     * 试题缓存信息
     */
    private static final Cache<Long, QuestionInfo> QUESTION_INFO_CACHE =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(1, TimeUnit.DAYS)
                    .maximumSize(300)
                    .build();

    @Override
    public List<QuestionInfo> getBaseQuestionInfo(List<Long> questionIdList) {
        if (CollectionUtils.isEmpty(questionIdList)) {
            Lists.newArrayList();
        }
        final List<Long> noCacheIdList = questionIdList.parallelStream()
                .filter(questionId -> QUESTION_INFO_CACHE.getIfPresent(questionId) == null)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(noCacheIdList)) {
            //获取所有的试题信息
            final String questionIds = noCacheIdList.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            Object listQuestionByIds = questionService.listQuestionByIds(questionIds);
            List<Map<String, Object>> result;
            try {
                result = (List<Map<String, Object>>) ZTKResponseUtil.build(listQuestionByIds);
            } catch (Exception e) {
                return Lists.newArrayList();
            }
            if (CollectionUtils.isNotEmpty(result)) {
                //通过返回的结果 构建 QuestionInfo
                Function<Map, QuestionInfo> buildBaseQuestion = map -> {
                    final QuestionInfo questionInfo = QuestionInfo.builder()
                            .id(MapUtils.getLong(map, "id"))
                            .stem(MapUtils.getString(map, "stem"))
                            .choiceList((List<String>) MapUtils.getObject(map, "choices"))
                            .answer(MapUtils.getString(map, "answer"))
                            .analysis(MapUtils.getString(map, "analysis"))
                            .extend(MapUtils.getString(map, "extend"))
                            .from(MapUtils.getString(map, "from"))
                            .build();
                    //材料
                    List<String> materialList = (List<String>) MapUtils.getObject(map, "materials");
                    if (CollectionUtils.isEmpty(materialList)) {
                        String material = MapUtils.getString(map, "material");
                        if (!StringUtils.isEmpty(material)) {
                            materialList = Lists.newArrayList(material);
                        }
                    }
                    //知识点信息
                    List<String> pointNameList = (List<String>) MapUtils.getObject(map, "pointsName");
                    if (CollectionUtils.isNotEmpty(pointNameList)) {
                        questionInfo.setPointName(pointNameList.stream().collect(Collectors.joining("-")));
                    }
                    questionInfo.setMaterialList(materialList);
                    Integer type = MapUtils.getInteger(map, "type");
                    questionInfo.setType(type);
                    questionInfo.setTypeName(questionService.getQuestionTypeName(type));
                    return questionInfo;
                };

                List<QuestionInfo> questionInfoList = result.stream()
                        .map(buildBaseQuestion)
                        .collect(Collectors.toList());
                //构建缓存信息
                questionInfoList.forEach(questionInfo -> QUESTION_INFO_CACHE.put(questionInfo.getId(), questionInfo));
            }
        }
        //最终ID 转换成试题信息
        List<QuestionInfo> resultList = questionIdList.stream()
                .map(questionId -> QUESTION_INFO_CACHE.getIfPresent(questionId))
                .filter(questionInfo -> null != questionInfo)
                .collect(Collectors.toList());
        return resultList;
    }
}
