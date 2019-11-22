package com.huatu.ztk.knowledge.servicePandora.impl;

import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.ztk.commons.Module;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.daoPandora.KnowledgeMapper;
import com.huatu.ztk.knowledge.service.v1.QuestionPointServiceV1;
import com.huatu.ztk.knowledge.servicePandora.KnowledgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import service.impl.BaseServiceHelperImpl;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/8/21
 */
@Service
public class KnowledgeServiceImplV2 extends BaseServiceHelperImpl<Knowledge> implements KnowledgeService {

    public KnowledgeServiceImplV2() {
        super(Knowledge.class);
    }
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeServiceImplV2.class);

    @Autowired
    private KnowledgeMapper mapper;

    @Autowired
    @Qualifier("questionPointServiceImplV2")
    private QuestionPointServiceV1 questionPointServiceV2;

    @Override
    public List<Module> findModule(int subjectId) {
        List<Knowledge> knowledgeList = mapper.getFirstLevelBySubjectId(subjectId);
        List<Module> moduleList = knowledgeList.stream()
                .map(knowledge ->
                        Module.builder()
                                .id(knowledge.getId().intValue())
                                .name(knowledge.getName())
                                .build()
                )
                .collect(Collectors.toList());
        return moduleList;
    }

    @Override
    public QuestionPoint findById(int knowledgeId) {
        return questionPointServiceV2.findById(knowledgeId);
    }


}
