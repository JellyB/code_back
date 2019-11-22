package com.huatu.tiku.teacher.service.impl.knowledge;

import com.google.common.collect.Lists;
import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.tiku.entity.knowledge.KnowledgeSubject;
import com.huatu.tiku.entity.knowledge.OldKnowledge;
import com.huatu.tiku.entity.subject.Subject;
import com.huatu.tiku.teacher.dao.knowledge.OldKnowledgeMapper;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeSubjectService;
import com.huatu.tiku.teacher.service.knowledge.SyncKnowledgeService;
import com.huatu.tiku.teacher.service.subject.TeacherSubjectService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/8/24.
 */
@Slf4j
@Service
public class SyncKnowledgeServiceImpl implements SyncKnowledgeService {

    @Autowired
    KnowledgeService knowledgeService;
    @Autowired
    OldKnowledgeMapper oldKnowledgeMapper;
    @Autowired
    TeacherSubjectService subjectService;
    @Autowired
    KnowledgeSubjectService knowledgeSubjectService;
    @Override
    public Object syncKnowledge() {
        Example example = new Example(Subject.class);
        example.and().andEqualTo("level",2);
        List<Subject> subjectList = subjectService.selectByExample(example);
        List result = Lists.newArrayList();
        for (Subject subject : subjectList) {
            List<OldKnowledge> knowledges = findOldKnowledgeBySubject(subject.getId());
            if(CollectionUtils.isNotEmpty(knowledges)){
                List<Knowledge> list = knowledges.stream().map(i -> {
                    Knowledge build = Knowledge.builder()
                            .level(i.getNodeRank() + 1)
                            .parentId(new Long(i.getPrevKp()))
                            .name(i.getName())
                            .build();
                    build.setId(new Long(i.getPukey()));
                    return build;
                }).collect(Collectors.toList());
                insertKnowledgeBySubject(list,subject.getId());
            }
            List list = knowledgeService.treeBySubject(subject.getId());
            if(CollectionUtils.isNotEmpty(list)){
                result.add(list);
            }
        }
        return result;
    }

    private void insertKnowledgeBySubject(List<Knowledge> list, Long id) {
        List<Long> parents = list.stream().map(i -> i.getParentId()).filter(i -> i > 0).distinct().collect(Collectors.toList());
        List<KnowledgeSubject> knowledgeSubjects = Lists.newArrayList();
        for (Knowledge knowledge : list) {
            if(parents.contains(knowledge.getId())){
                knowledge.setIsLeaf(false);
            }else {
                knowledge.setIsLeaf(true);
            }
            knowledgeSubjects.add(KnowledgeSubject.builder().subjectId(id).knowledgeId(knowledge.getId()).build());
        }
        try{
            knowledgeService.insertAll(list);
            knowledgeSubjectService.insertAll(knowledgeSubjects);
        }catch (Exception e){
            for (Knowledge knowledge : list) {
                try{
                    knowledgeService.insert(knowledge);
                    KnowledgeSubject knowledgeSubject = KnowledgeSubject.builder().subjectId(id).knowledgeId(knowledge.getId()).build();
                    knowledgeSubjectService.insert(knowledgeSubject);
                }catch (Exception e1){
                    log.error("error:{}",e1.getMessage());
                    knowledgeService.save(knowledge);
                }
            }
        }
    }

    private List<OldKnowledge> findOldKnowledgeBySubject(Long id) {
        Example example = new Example(OldKnowledge.class);
        example.and().andEqualTo("blSub",id).andEqualTo("BB102",1);
        return oldKnowledgeMapper.selectByExample(example);
    }
}
