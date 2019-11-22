package com.huatu.tiku.teacher.service.impl;

import com.google.common.collect.Lists;
import com.huatu.tiku.entity.teacher.PaperActivitySubject;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.dao.paper.PaperActivitySubjectMapper;
import com.huatu.tiku.teacher.service.PaperActivitySubjectService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\6\23 0023.
 */
@Service
public class PaperActivitySubjectServiceImpl extends BaseServiceImpl<PaperActivitySubject> implements PaperActivitySubjectService {
    public PaperActivitySubjectServiceImpl() {
        super(PaperActivitySubject.class);
    }
    @Autowired
    PaperActivitySubjectMapper paperActivitySubjectMapper;

    @Override
    @Transactional
    public void insertPaperSubject(Long paperId, List<Long> subjectIds) {
        List<PaperActivitySubject> results = Lists.newArrayList();
        for (Long subjectId : subjectIds) {
            PaperActivitySubject paperActivitySubject = new PaperActivitySubject();
            paperActivitySubject.setPaperId(paperId);
            paperActivitySubject.setSubjectId(subjectId);
            results.add(paperActivitySubject);
        }
        insertAll(results);
    }

    @Override
    @Transactional
    public void deleteByPaperId(Long paperId) {
        Example example = new Example(PaperActivitySubject.class);
        example.and().andEqualTo("paperId",paperId);
        paperActivitySubjectMapper.deleteByExample(example);
    }

    @Override
    public List<Long> findSubjectByPaperId(Long paperId) {
        Example example = new Example(PaperActivitySubject.class);
        example.and().andEqualTo("paperId",paperId);
        List<PaperActivitySubject> paperActivitySubjects = selectByExample(example);
        if(CollectionUtils.isNotEmpty(paperActivitySubjects)){
            return paperActivitySubjects.stream().map(i->i.getSubjectId()).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }
}

