package com.huatu.tiku.teacher.controller.admin.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.dto.KnowledgeVO;
import com.huatu.tiku.entity.subject.Subject;
import com.huatu.tiku.entity.teacher.EssayMockExam;
import com.huatu.tiku.enums.EnumUtil;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.SubjectInfoEnum;
import com.huatu.tiku.teacher.controller.util.InstructionUtil;
import com.huatu.tiku.teacher.dao.provider.question.TeacherBaseQuestionSearchProvider;
import com.huatu.tiku.teacher.enums.ActivityTypeAndStatus;
import com.huatu.tiku.teacher.enums.ActivityTypeAndStatus.ActivityStatusEnum;
import com.huatu.tiku.teacher.enums.ActivityTypeAndStatus.ActivityTypeEnum;
import com.huatu.tiku.teacher.enums.BizStatusEnum;
import com.huatu.tiku.teacher.enums.DifficultyLevelEnum;
import com.huatu.tiku.teacher.enums.SpecialTeacherEnum;
import com.huatu.tiku.teacher.service.common.AreaService;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import com.huatu.tiku.teacher.service.question.QuestionTypeService;
import com.huatu.tiku.teacher.service.subject.TeacherSubjectService;
import com.huatu.tiku.util.log.LogPrint;
import com.huatu.ztk.paper.common.PaperType;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 共用属性查询维护
 * Created by huangqp on 2018\7\12 0012.
 */
@RestController
@RequestMapping("common")
public class CommonController {
    @Autowired
    QuestionTypeService questionTypeService;
    @Autowired
    KnowledgeService knowledgeService;
    @Autowired
    private AreaService areaService;
    @Autowired
    private TeacherSubjectService teacherSubjectService;

    @Autowired
    private TeacherSubjectService subjectService;

    //可以按照单题算分的科目(TODO-lzj)
    @Value("${questionScoreEnableSubject}")
    private String questionScoreEnableSubject;

    /**
     * 试题题型根据科目返回
     *
     * @param id
     * @return
     */
    @LogPrint
    @GetMapping("question/type")
    public Object getQuestionType(@RequestParam Long id) {
        return questionTypeService.findTypeBySubject(id);
    }

    /**
     * 根据科目查询知识点树(携带知识点数据和全部节点)
     *
     * @param subject
     * @return
     */
    @LogPrint
    @GetMapping("knowledge/tree/count")
    public Object getKnowledgeTreeBySubject(@RequestParam(defaultValue = "1") Long subject) {
        List<KnowledgeVO> knowledgeVOS = knowledgeService.showKnowledgeTreeBySubject(subject, true);
        KnowledgeVO result = KnowledgeVO.builder().haveSub(true).knowledgeId(-1L).knowledgeName("全部").knowledgeTrees(knowledgeVOS).build();
        Integer count = 0;
        for (KnowledgeVO knowledgeVO : knowledgeVOS) {
            count += knowledgeVO.getCount();
        }
        result.setCount(count);
        return result;

    }

    /**
     * 根据科目查询知识点树
     *
     * @param subject
     * @return
     */
    @LogPrint
    @GetMapping("knowledge/tree")
    public List<KnowledgeVO> getKnowledgeTree1BySubject(@RequestParam(defaultValue = "1") Long subject) {
        return knowledgeService.showKnowledgeTreeBySubject(subject, false);
    }

    /**
     * 根据父ID 获取子科目
     *
     * @param parentId 父 ID
     * @return 子科目
     */
    @GetMapping("{subjectId}/getSubjectByParentId")
    public List<Subject> getSubjectByParentId(@PathVariable("subjectId") Long parentId) {
        return subjectService.findChildren(parentId);
    }


    /**
     * 查询试卷属性
     */
    @LogPrint
    @GetMapping("mode")
    public Object getModeType() {
        List<Map> result = Lists.newArrayList();
        for (PaperInfoEnum.ModeEnum modeEnum : PaperInfoEnum.ModeEnum.values()) {
            Map temp = Maps.newHashMap();
            temp.put("key", modeEnum.getCode());
            temp.put("value", modeEnum.getName());
            result.add(temp);
        }
        return result;
    }

    /**
     * 查询试题难度
     */
    @LogPrint
    @GetMapping("difficult")
    public Object getDifficult() {
        List<Map> result = Lists.newArrayList();
        for (DifficultyLevelEnum difficultyLevelEnum : DifficultyLevelEnum.values()) {
            Map temp = Maps.newHashMap();
            temp.put("key", difficultyLevelEnum.getValue());
            temp.put("value", difficultyLevelEnum.getTitle());
            result.add(temp);
        }
        return result;
    }

    /**
     * 查询地区列表
     *
     * @param subject
     * @return
     */
    @GetMapping("area")
    @LogPrint
    public Object getAreaList(@RequestParam Long subject) {
        return areaService.findAreaBySubject(subject);
    }

    /**
     * 查询试题状态
     */
    @LogPrint
    @GetMapping("question/status")
    public Object getQuestionStatus() {
        List<Map> result = Lists.newArrayList();
        for (BizStatusEnum bizStatusEnum : BizStatusEnum.values()) {
            Map temp = Maps.newHashMap();
            temp.put("key", bizStatusEnum.getValue());
            temp.put("value", bizStatusEnum.getTitle());
            result.add(temp);
        }
        return result;
    }

    /**
     * 科目统计信息（科目包含的子节点，及每个节点下包含的题量，试卷数量）
     *
     * @return
     */
    @LogPrint
    @GetMapping("subject")
    public Object getSubjectCount() {
        return teacherSubjectService.getSubjectCount();
    }

    /**
     * 创建招教事业单位试卷-获得所有学段信息
     *
     * @param subjectId TODO
     * @return
     */
    @LogPrint
    @GetMapping("grade")
    public Object getPeriod(@RequestParam Long subjectId,
                            @RequestParam(defaultValue = "3") int level) {
        List<Subject> children = teacherSubjectService.findChildren(subjectId, level);
        if (CollectionUtils.isEmpty(children)) {
            return null;
        }
        List<Subject> subjects = children.stream().filter(i -> i.getLevel() == SubjectInfoEnum.SubjectTypeEnum.GRADE.getCode()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(subjects)) {
            return null;
        }
        List result = Lists.newArrayList();
        for (Subject subject : subjects) {
            Map map = new HashMap();
            map.put("key", subject.getId());
            map.put("value", subject.getName());
            result.add(map);
        }
        return result;
    }

    /**
     * 活动卷——获取所有活动类型
     */
    @LogPrint
    @GetMapping("activityType")
    public Object getActivityType(@RequestParam(defaultValue = "-1") int subjectId) {
        List<Map> mapList = Lists.newArrayList();
        List<Integer> subjectIds = new ArrayList<>();
        //可以展示的科目
        if (null != questionScoreEnableSubject) {
            subjectIds = Arrays.stream(questionScoreEnableSubject.split(","))
                    .map(subject -> Integer.valueOf(subject.trim()))
                    .collect(Collectors.toList());
        }
        //可用活动类型
        List<Integer> activityTypes = ActivityTypeEnum.getEnumKeys();
        if (CollectionUtils.isNotEmpty(activityTypes))
            activityTypes.removeIf(type ->
                    type == ActivityTypeEnum.FORMATIVE_TEST_ESTIMATE.getKey());

        for (ActivityTypeEnum type : ActivityTypeEnum.values()) {
            if (type.getKey() == ActivityTypeEnum.UN_KNOW.getKey()) {
                continue;
            }
            HashMap map = new HashMap();
            map.put("key", type.getKey());
            map.put("value", type.getValue());
            if (subjectIds.contains(subjectId) && activityTypes.contains(type.getKey())) {
                map.put("questionScoreShowEnableFlag", true);
            } else {
                map.put("questionScoreShowEnableFlag", false);
            }
            mapList.add(map);
        }
        return mapList;
    }

    /**
     * 根据活动类型活动活动状态(准备废弃)
     *
     * @param activityType
     * @return
     */
    @Deprecated
    @LogPrint
    @GetMapping("activitystate")
    public Object getActivityStatusByActivityType(@RequestParam(defaultValue = PaperType.MATCH+"") Integer activityType,
                                                  @RequestParam(defaultValue = "1") int subjectId) {
        Map resultMap = new HashMap();
        if(subjectId == 14){
            resultMap.put(1,"未上线");
            resultMap.put(2,"已上线");
            resultMap.put(3,"已结束");
            resultMap.put(4,"已完成");
            return resultMap;
        }
        List<Integer> integerList = Arrays.stream(ActivityTypeAndStatus.ActivityTypeEnum.values())
                .filter(i -> i.getKey() == activityType)
                .map(j -> j.getStatus())
                .flatMap(list -> list.stream())
                .collect(Collectors.toList());

        Map<Integer, String> mapList = Arrays.stream(ActivityStatusEnum.values())
                .collect(Collectors.toMap(i -> i.getKey(), i -> i.getValue()));

        for (Integer status : integerList) {
            if (mapList.containsKey(status)) {
                resultMap.put(status, mapList.get(status));
            }
        }
        return resultMap;
    }


    /**
     * 获取是否是特岗教师
     *
     * @return
     */
    @GetMapping("specialTeacher")
    public Object getSpecialTeacher() {
        return EnumUtil.asList(SpecialTeacherEnum.class);
    }


    /**
     * 获取活动说明
     *
     * @param subjectId 科目Id
     * @param type      活动类型
     * @return
     */
    @GetMapping("instruction")
    public Object getInstruction(@RequestParam(defaultValue = "-1") int subjectId,
                                 @RequestParam(defaultValue = "1") int type) {
        ActivityTypeEnum activityTypeEnum = ActivityTypeEnum.create(type);
        switch (activityTypeEnum) {
            case MATCH:
                return InstructionUtil.getMatchDesc();
            case SMALL_ESTIMATE:
                return InstructionUtil.getSmallEstimateDesc();
        }
        return Maps.newHashMap();
    }

    /**
     * 获取活动说明
     *
     * @param type        实体卷还是活动卷判断
     * @param subType      活动卷类型或者实体卷的真题/模拟题
     * @return
     */
    @GetMapping("sourceFlag/{type}/{subType}")
    public Object getDefaultSourceFlag(@PathVariable int type,
                                       @PathVariable int subType) {
        PaperInfoEnum.TypeInfo typeInfo = PaperInfoEnum.TypeInfo.create(type);
        HashMap<Object, Object> map = Maps.newHashMap();
        switch (typeInfo){
            case ENTITY:
                map.put("sourceFlag",true);
                break;
            case SIMULATION:
                ActivityTypeEnum activityTypeEnum = ActivityTypeEnum.create(subType);
                map.put("sourceFlag",TeacherBaseQuestionSearchProvider.SEARCH_ACTIVITY_TYPES.contains(activityTypeEnum));
        }
        return map;
    }
}

