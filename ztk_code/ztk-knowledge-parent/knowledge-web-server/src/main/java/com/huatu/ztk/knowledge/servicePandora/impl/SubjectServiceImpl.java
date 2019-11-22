package com.huatu.ztk.knowledge.servicePandora.impl;

import com.google.common.collect.Lists;
import com.huatu.tiku.entity.subject.Subject;
import com.huatu.ztk.knowledge.bean.SubjectTree;
import com.huatu.ztk.knowledge.servicePandora.SubjectService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import service.impl.BaseServiceHelperImpl;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Created by lijun on 2018/8/22
 */
@Service
public class SubjectServiceImpl extends BaseServiceHelperImpl<Subject> implements SubjectService {

    public SubjectServiceImpl() {
        super(Subject.class);
    }


    @Override
    public int getCategoryBySubjectId(int subjectId) {
        SubjectTree subjectTree = findById(subjectId);
        return null == subjectTree ? 0 : subjectTree.getParent();
    }

    @Override
    public List<SubjectTree> findChildren(SubjectTree subjectTree) {
        Example example = Example.builder(Subject.class).build();
        example.and().andEqualTo("parent", subjectTree.getId());
        List<Subject> subjectList = selectByExample(example);
        if (CollectionUtils.isEmpty(subjectList)) {
            return Lists.newArrayList();
        }
        boolean present = subjectList.stream().filter(i -> i.getId().intValue() == subjectTree.getId()).filter(i -> i.getParent().intValue() == subjectTree.getParent()).findAny().isPresent();
        if (present) {  //如果查询结果存在父节点，删除父节点
            subjectList.removeIf(i -> i.getId().intValue() == subjectTree.getId() && i.getParent().intValue() == subjectTree.getParent());
        }else{      //如果查询结果没有父节点数据，但可能存在ID和父节点相同的节点数据，那么查询结果必然是该节点+（不存在的下级节点数据，取上级节点）
            Map<Integer, List<Subject>> tempMap = subjectList.stream().collect(Collectors.groupingBy(Subject::getLevel));
            Map.Entry<Integer, List<Subject>> listEntry = tempMap.entrySet().stream().min(Comparator.comparing(Map.Entry::getKey)).get();
            subjectList = listEntry.getValue();
        }

        return subjectList.stream()
                .map(SubjectServiceImpl::transSubjectToSubjectTree)
                .collect(Collectors.toList());
    }

    @Override
    public SubjectTree findById(int subjectId) {
        WeekendSqls<Subject> sql = WeekendSqls.custom();
        sql.andEqualTo(Subject::getId, subjectId);
        sql.andEqualTo(Subject::getLevel, SubjectInfoEnum.LEVEL.SUBJECT.getLevel());
        Example example = Example.builder(Subject.class)
                .andWhere(sql)
                .build();
        List<Subject> subjectList = selectByExample(example);
        if (CollectionUtils.isNotEmpty(subjectList)) {
            Subject subject = subjectList.get(0);
            return transSubjectToSubjectTree(subject);
        }
        return null;
    }

    @Override
    public List<Long> getSubjectIdListByCategory(int category) {
        Example example = Example.builder(Subject.class).build();
        example.and().andEqualTo("parent", category);
        List<Subject> subjectList = selectByExample(example);
        List<Long> subjectIdList = subjectList.stream()
                .map(Subject::getId)
                .collect(Collectors.toList());
        return subjectIdList;
    }

    /**
     * 数据转换
     */
    private static SubjectTree transSubjectToSubjectTree(Subject subject) {
        if (null == subject) {
            return SubjectTree.builder().build();
        }
        return SubjectTree.builder()
                .id(subject.getId().intValue())
                .name(subject.getName())
                .parent(subject.getParent().intValue())
                .tiku(true)
                .status(subject.getStatus())
                .childrens(Lists.newArrayList())
                .build();
    }
}
