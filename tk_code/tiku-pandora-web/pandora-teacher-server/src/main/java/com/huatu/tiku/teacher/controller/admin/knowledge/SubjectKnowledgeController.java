package com.huatu.tiku.teacher.controller.admin.knowledge;

import com.google.common.collect.Lists;
import com.huatu.common.SuccessMessage;
import com.huatu.tiku.entity.knowledge.KnowledgeSubject;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeSubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2019/1/29
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("subjectKnowledge")
@Slf4j
public class SubjectKnowledgeController {

    final KnowledgeSubjectService knowledgeSubjectService;

    /**
     * 获取已选中的 一级节点信息
     *
     * @param subjectId 科目ID
     */
    @GetMapping("{subjectId}/getFirstLevelKnowledgeBySubjectId")
    public Object getSelectedBySubjectId(@PathVariable("subjectId") Long subjectId) {
        return knowledgeSubjectService.getFirstLevelKnowledgeBySubjectId(subjectId);
    }

    /**
     * 获取当前科目下 兄弟节点和本身的 所有一级节点信息
     *
     * @param subjectId 科目ID
     */
    @GetMapping("{subjectId}/getAllKnowledgeBySubjectId")
    public Object getAllKnowledgeBySubjectId(@PathVariable("subjectId") Long subjectId) {
        return knowledgeSubjectService.getAllFriendKnowledgeBySubjectId(subjectId);
    }

    /**
     * 新增关联关系
     *
     * @param subjectId   科目ID
     * @param knowledgeId 一级知识点ID
     */
    @PutMapping("{subjectId}/{knowledgeId}/editRelation")
    public Object addRelation(@PathVariable("subjectId") Long subjectId, @PathVariable("knowledgeId") Long knowledgeId) {
        final KnowledgeSubject knowledgeSubject = KnowledgeSubject.builder()
                .knowledgeId(knowledgeId)
                .subjectId(subjectId)
                .build();
        knowledgeSubjectService.save(knowledgeSubject);
        return SuccessMessage.create();
    }

    /**
     * 删除关联关系
     *
     * @param subjectId   科目ID
     * @param knowledgeId 一级知识点ID
     */
    @DeleteMapping("{subjectId}/{knowledgeId}/deleteRelation")
    public Object deleteRelation(@PathVariable("subjectId") Long subjectId, @PathVariable("knowledgeId") Long knowledgeId) {
        WeekendSqls<KnowledgeSubject> knowledgeSubjectWeekendSql = WeekendSqls.<KnowledgeSubject>custom()
                .andEqualTo(KnowledgeSubject::getSubjectId, subjectId)
                .andEqualTo(KnowledgeSubject::getKnowledgeId, knowledgeId);
        final Example deleteExample = Example.builder(KnowledgeSubject.class)
                .where(knowledgeSubjectWeekendSql)
                .build();
        knowledgeSubjectService.deleteByExample(deleteExample);
        return SuccessMessage.create();
    }

    /**
     * 编辑关联信息
     */
    @PostMapping("{subjectId}/editRelation")
    public Object editRelation(@PathVariable("subjectId") Long subjectId, @RequestParam String knowledgeIds) {
        List<Long> knowledgeIdList = Lists.newArrayList();
        if (StringUtils.isNotBlank(knowledgeIds)) {
            knowledgeIdList.addAll(Arrays.stream(knowledgeIds.split(","))
                    .map(Long::valueOf)
                    .collect(Collectors.toList()));
        }
        knowledgeSubjectService.editRelation(subjectId, knowledgeIdList);
        return SuccessMessage.create();
    }
}
