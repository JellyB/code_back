package com.huatu.tiku.line;

import com.google.common.collect.Maps;
import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.banckend.dao.manual.OldQuestionMapper;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.request.material.MaterialReq;
import com.huatu.tiku.request.question.v1.InsertCommonQuestionReqV1;
import com.huatu.tiku.teacher.dao.mongo.ReflectQuestionDao;
import com.huatu.tiku.teacher.dao.question.BaseQuestionSearchMapper;
import com.huatu.tiku.teacher.enums.ActivityTypeAndStatus;
import com.huatu.tiku.teacher.service.question.QuestionSearchService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.ztk.commons.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/10/29.
 */
@Slf4j
public class BackendMapperTest extends TikuBaseTest {
    @Autowired
    OldQuestionMapper oldQuestionMapper;

    @Autowired
    CommonQuestionServiceV1 commonQuestionServiceV1;

    @Autowired
    BaseQuestionSearchMapper searchMapper;

    @Autowired
    ReflectQuestionDao reflectQuestionDao;

    @Autowired
    QuestionSearchService questionSearchService;

    @Test
    public void test() {
        List<Long> questionIds = Lists.newArrayList();
        StopWatch stopWatch = new StopWatch("修改mutlti");
        stopWatch.start("新题库查询待修改复合题");
        List<HashMap<String, Object>> multiQuestions = searchMapper.findQuestionIdByMultiId();
        stopWatch.stop();

        stopWatch.start("新题库查询所有待修改的子题");
        System.out.println("复合题属性" + JsonUtil.toJson(multiQuestions));
        List<Long> multiIds = multiQuestions.stream().map(i -> MapUtils.getLong(i, "multi_id")).collect(Collectors.toList());
        Function<List<Long>, List<BaseQuestion>> getQuestionInIds = (ids -> {
            Example example = new Example(BaseQuestion.class);
            example.and().andIn("multiId", ids);
            return commonQuestionServiceV1.selectByExample(example);
        });
        //新题库待修正材料的子题
        List<BaseQuestion> baseQuestions = getQuestionInIds.apply(multiIds);
        stopWatch.stop();
        stopWatch.start("旧题库查询对应的子题材料信息");
        Function<List<BaseQuestion>, List<HashMap<String, Object>>> getOldMultiInfo = (questions -> {
            List<Integer> collect = questions.stream().map(BaseQuestion::getId).map(Long::intValue).collect(Collectors.toList());
            List<HashMap<String, Object>> questionMaterial = oldQuestionMapper.findQuestionMaterial(collect);
            return questionMaterial;
        });
        //老题库子题材料信息查询
        List<HashMap<String, Object>> questionMaps = getOldMultiInfo.apply(baseQuestions);
        stopWatch.stop();
        stopWatch.start("材料新题库入库");
        System.out.println(JsonUtil.toJson(questionMaps));
        //子题根据复合题ID分组
        Map<Long, List<HashMap<String, Object>>> parentMap = questionMaps.stream().collect(Collectors.groupingBy(i -> MapUtils.getLong(i, "parent")));
        //老题库的复合题ID对应新题库建的复合题ID
        HashMap<Long, Long> reflectMap = Maps.newHashMap();
        //复合题的材料录入
        System.out.println("parentMap.size=" + parentMap.size());
        int l = 0;
        for (Map.Entry<Long, List<HashMap<String, Object>>> entry : parentMap.entrySet()) {
            l++;
            System.out.println("parentMap deal ====" + l);
            Long parent = entry.getKey();
//            if(!parent.equals(new Long(2142))){
//                continue;
//            }
            List<HashMap<String, Object>> value = entry.getValue();
            HashMap<String, Object> questionMap = value.get(0);
            String material = MapUtils.getString(questionMap, "material");
            InsertCommonQuestionReqV1 insertCommonQuestionReqV1 = new InsertCommonQuestionReqV1();
            insertCommonQuestionReqV1.setMaterials(Lists.newArrayList(MaterialReq.builder().content(MaterilContentUtil.convert2MobileLayout(material)).build()));
            insertCommonQuestionReqV1.setQuestionType(QuestionInfoEnum.QuestionTypeEnum.COMPOSITE.getCode());
            insertCommonQuestionReqV1.setSubject(1L);
            Map map = commonQuestionServiceV1.insertQuestion(insertCommonQuestionReqV1);
            Long questionId = MapUtils.getLong(map, "questionId");
            reflectQuestionDao.insertRelation(2000000 + parent.intValue(), questionId);
            reflectMap.put(parent, questionId);
        }
        System.out.println("reflectMap===" + JsonUtil.toJson(reflectMap));
        stopWatch.stop();
        stopWatch.start("新题库子题修改multi");
        //老题库试题跟复合题ID的对应关系
        Map<Long, Long> questionParentMap = questionMaps.stream().collect(Collectors.toMap(i -> MapUtils.getLong(i, "id"), i -> MapUtils.getLong(i, "parent")));
        System.out.println("baseQuestion.size=" + baseQuestions.size());
        int b = 0;
        for (BaseQuestion baseQuestion : baseQuestions) {
            b++;
            System.out.println("baseQuestion deal =====" + b);
            Long id = baseQuestion.getId();
            //对应的老题库材料ID
            Long multi = questionParentMap.get(id);
            //对应新题库的材料ID
            Long newId = reflectMap.get(multi);
            if (null == newId || newId <= 0) {
                continue;
            }
            baseQuestion.setMultiId(newId);
            commonQuestionServiceV1.save(baseQuestion);
        }
        stopWatch.stop();
        stopWatch.start("合并，归并");
        Set<Long> oldMultiIds = reflectMap.keySet();
        List<HashMap<String, Object>> oldIdList = oldQuestionMapper.findQuestionByMultiIds(oldMultiIds);
        List<Long> collect = baseQuestions.stream().map(BaseQuestion::getId).collect(Collectors.toList());
        System.out.println("其他需要归并的试题ID=" + JsonUtil.toJson(oldIdList));
        List<Long> ids = oldIdList.stream().map(i -> MapUtils.getLong(i, "id")).collect(Collectors.toList());
        Map<Long, Long> collectMap = oldIdList.stream().collect(Collectors.toMap(i -> MapUtils.getLong(i, "id"), i -> MapUtils.getLong(i, "parent")));
        ids.removeIf(i -> collect.contains(i));
        Example example = new Example(BaseQuestion.class);
        example.and().andIn("id", ids);
        List<BaseQuestion> baseQuestions1 = commonQuestionServiceV1.selectByExample(example);
        for (BaseQuestion baseQuestion : baseQuestions1) {
            Long multiId = baseQuestion.getMultiId();
            Long oldMultiId = collectMap.get(baseQuestion.getId());
            Long newMultiId = reflectMap.get(oldMultiId);
            if (!multiId.equals(newMultiId)) {
                if (multiId.intValue() == 0) {
                    questionIds.add(baseQuestion.getId());
                    continue;
                }
                BaseQuestion baseQuestion1 = new BaseQuestion();
                baseQuestion1.setMultiId(newMultiId);
                Example updateExample = new Example(BaseQuestion.class);
                updateExample.and().andEqualTo("multiId", multiId);
                commonQuestionServiceV1.updateByExampleSelective(baseQuestion1, updateExample);
            }
        }
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());
        System.out.println("baseQuestion.id ====" + baseQuestions.stream().map(BaseQuestion::getId).collect(Collectors.toList()));
    }


    @Test
    public void sourceTest() {
        Long questionId = 54377L;
        String sour = questionSearchService.findSingleQuestionSource(questionId);
        System.out.println("source = " + sour);
        List<HashMap<String, Object>> questionSourceForList = searchMapper.getQuestionSourceForList(Lists.newArrayList(questionId));
        System.out.println("questionSourceForList = " + JsonUtil.toJson(questionSourceForList));
        List<HashMap<String, Object>> questionSourceForListByType = searchMapper.getQuestionSourceForListByTypeWithEntity(Lists.newArrayList(questionId),
                Lists.newArrayList(ActivityTypeAndStatus.ActivityTypeEnum.MATCH,
                        ActivityTypeAndStatus.ActivityTypeEnum.TRUE_PAPER,
                        ActivityTypeAndStatus.ActivityTypeEnum.REGULAR_PAPER
                ),true);
        System.out.println("questionSourceForListByType = " + JsonUtil.toJson(questionSourceForListByType));
    }

}
