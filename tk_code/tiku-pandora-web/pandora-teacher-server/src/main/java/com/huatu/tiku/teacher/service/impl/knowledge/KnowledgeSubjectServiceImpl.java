package com.huatu.tiku.teacher.service.impl.knowledge;

import com.google.common.collect.Lists;
import com.huatu.common.exception.BizException;
import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.tiku.constants.teacher.TeacherErrors;
import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.tiku.entity.knowledge.KnowledgeSubject;
import com.huatu.tiku.entity.subject.Subject;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.dao.knowledge.KnowledgeSubjectMapper;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeSubjectService;
import com.huatu.tiku.teacher.service.subject.TeacherSubjectService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\6\12 0012.
 */
@Slf4j
@Service
public class KnowledgeSubjectServiceImpl extends BaseServiceImpl<KnowledgeSubject> implements KnowledgeSubjectService {
    public KnowledgeSubjectServiceImpl() {
        super(KnowledgeSubject.class);
    }

    @Autowired
    KnowledgeSubjectMapper knowledgeSubjectMapper;

    @Autowired
    KnowledgeService knowledgeService;

    @Autowired
    TeacherSubjectService subjectService;

    @Override
    public List<Long> choicesKnowledgeBySubject(List<Long> knowledgeIds, Long subjectId) {
        if (CollectionUtils.isEmpty(knowledgeIds)) {
            return Lists.newArrayList();
        }
        Example example = new Example(KnowledgeSubject.class);
        example.and().andIn("knowledgeId", knowledgeIds);
        List<KnowledgeSubject> knowledgeSubjects = selectByExample(example);

        List<Long> results = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(knowledgeSubjects)) {
            results.addAll(knowledgeSubjects.stream().filter(i -> i.getSubjectId().equals(subjectId)).map(KnowledgeSubject::getKnowledgeId).distinct().collect(Collectors.toList()));
        }
        if (CollectionUtils.isEmpty(results)) {
            log.error("error={},subjectId={},knowledgeIds={}", TeacherErrors.NO_EXISTED_SUBJECT_KNOWLEDGE.getMessage(), subjectId, knowledgeIds);
            throw new BizException(TeacherErrors.NO_EXISTED_SUBJECT_KNOWLEDGE);
        }
        return results;
    }

    @Override
    @Transactional
    public void deleteByKnowledge(long knowledgeId) {
        Example example = new Example(KnowledgeSubject.class);
        example.and().andEqualTo("knowledgeId", knowledgeId);
        knowledgeSubjectMapper.deleteByExample(example);
    }

    @Override
    public List<Knowledge> getFirstLevelKnowledgeBySubjectId(Long subjectId) {
        ArrayList<Long> subjectIdList = Lists.newArrayList(subjectId);
        return getFirstLevelKnowledgeBySubjectId(subjectIdList);
    }

    private List<Knowledge> getFirstLevelKnowledgeBySubjectId(List<Long> subjectIdList) {
        WeekendSqls<KnowledgeSubject> weekendSql = WeekendSqls.<KnowledgeSubject>custom()
                .andIn(KnowledgeSubject::getSubjectId, subjectIdList);
        final Example KnowledgeSubjectExample = Example.builder(KnowledgeSubject.class)
                .where(weekendSql)
                .build();
        List<KnowledgeSubject> knowledgeSubjects = selectByExample(KnowledgeSubjectExample);
        if (CollectionUtils.isEmpty(knowledgeSubjects)) {
            return Lists.newArrayList();
        }
        WeekendSqls<Knowledge> knowledgeWeekendSql = WeekendSqls.<Knowledge>custom()
                .andIn(Knowledge::getId, knowledgeSubjects.stream().map(KnowledgeSubject::getKnowledgeId).collect(Collectors.toList()))
                .andEqualTo(Knowledge::getLevel, 1);
        final Example knowledgeExample = Example.builder(Knowledge.class)
                .where(knowledgeWeekendSql)
                .build();
        return knowledgeService.selectByExample(knowledgeExample);
    }

    @Override
    public List<HashMap<String, Object>> getAllFriendKnowledgeBySubjectId(Long subjectId) {
        final List<Subject> friendNodesExcludeStaticName = findFriendNodesExcludeStaticName(subjectId);
        //获取所有的兄弟节点科目ID

        //获取所有兄弟节点科目对应的知识点信息
        List<Knowledge> allFriendKnowledgeBySubject = knowledgeSubjectMapper.selectKnowledgeBySubjectId(friendNodesExcludeStaticName);
        final List<Knowledge> allFriendKnowledgeFirstLevelBySubject = allFriendKnowledgeBySubject.stream()
                .filter(knowledge -> knowledge.getLevel() == 1)
                .collect(Collectors.toList());
        //获取当前节点对应的科目节点信息
        List<Knowledge> firstLevelKnowledgeBySubjectId = getFirstLevelKnowledgeBySubjectId(subjectId);
        final Set<Long> selectedKnowledgeIdSet = firstLevelKnowledgeBySubjectId.stream()
                .map(Knowledge::getId)
                .collect(Collectors.toSet());

        List<HashMap<String, Object>> mapList = allFriendKnowledgeFirstLevelBySubject.stream()
                .map(knowledge ->
                        HashMapBuilder.<String, Object>newBuilder()
                                .put("id", knowledge.getId())
                                .put("name", knowledge.getName())
                                .put("selected", selectedKnowledgeIdSet.contains(knowledge.getId()))
                                .build()
                )
                .collect(Collectors.toList());
        return mapList;
    }

    private List<Subject> findFriendNodesExcludeStaticName(Long subjectId) {
        final List<Subject> friendNodes = subjectService.findFriendNodes(subjectId);
        if (CollectionUtils.isEmpty(friendNodes)) {
            return Lists.newArrayList();
        }
        //此处是 选择的基础知识点
        final List<Long> baseIdList = KnowledgeSubjectInstance.getInstance().getBaseSubjectList();
        log.info("baseIdList:{}", baseIdList);
        final List<Subject> friendNodesNotIncludeSelf = friendNodes.stream()
                .filter(subject -> baseIdList.stream().anyMatch(id -> subject.getId().equals(id)))
                .collect(Collectors.toList());
        return friendNodesNotIncludeSelf;
    }

    @Override
    @Transactional
    public void editRelation(final Long subjectId, List<Long> knowledgeIdList) {
        WeekendSqls<KnowledgeSubject> knowledgeSubjectWeekendSql = WeekendSqls.<KnowledgeSubject>custom()
                .andEqualTo(KnowledgeSubject::getSubjectId, subjectId);
        final Example deleteExample = Example.builder(KnowledgeSubject.class)
                .where(knowledgeSubjectWeekendSql)
                .build();
        knowledgeSubjectMapper.deleteByExample(deleteExample);

        //此处传入的是一级知识点信息，需要关联所有的子知识点信息
        final List<Subject> friendNodes = findFriendNodesExcludeStaticName(subjectId);
        knowledgeIdList.stream()
                .flatMap(knowledgeId ->
                        friendNodes.stream()
                                .flatMap(subject -> {
                                    final List<Knowledge> allChildrenKnowledge = knowledgeService.getAllChildrenKnowledge(subject.getId(), knowledgeId);
                                    return allChildrenKnowledge.stream().map(Knowledge::getId);
                                })
                                .distinct()
                )
                .map(knowledgeId -> KnowledgeSubject.builder()
                        .knowledgeId(knowledgeId)
                        .subjectId(subjectId)
                        .build()
                )
                .forEach(this::save);
    }
}

