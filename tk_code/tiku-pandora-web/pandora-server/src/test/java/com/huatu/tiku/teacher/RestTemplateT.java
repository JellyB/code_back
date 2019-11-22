package com.huatu.tiku.teacher;

import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.tiku.entity.knowledge.KnowledgeSubject;
import com.huatu.tiku.entity.subject.Subject;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeSubjectService;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import com.huatu.tiku.teacher.service.subject.TeacherSubjectService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Created by huangqp on 2018\7\2 0002.
 */
@Slf4j
public class RestTemplateT extends TikuBaseTest{
    @Autowired
    TeacherSubjectService teacherSubjectService;
    @Autowired
    KnowledgeService knowledgeService;
    @Autowired
    KnowledgeSubjectService knowledgeSubjectService;

    @Test
    public void testSubject2() {
        List<Map> subjects = RestTemplateUtil.findSubjects();
        for (Map subject : subjects) {
            Subject temp = new Subject();
            temp.setId(Long.parseLong(String.valueOf(subject.get("id"))));
            temp.setParent(Long.parseLong(String.valueOf(subject.get("catgory"))));
            temp.setLevel(2);
            temp.setName(String.valueOf(subject.get("name")));
            temp.setStatus(1);
            teacherSubjectService.insert(temp);
        }
    }

    @Test
    public void testSubject1() {
        List<Map> subjects = RestTemplateUtil.findCateGories();
        for (Map subject : subjects) {
            Subject temp = new Subject();
            temp.setId(Long.parseLong(String.valueOf(subject.get("id"))));
            temp.setParent(Long.parseLong(String.valueOf(subject.get("catgory"))));
            temp.setLevel(1);
            temp.setName(String.valueOf(subject.get("name")));
            temp.setStatus(1);
            teacherSubjectService.insert(temp);
        }
    }

    @Test
    @Transactional
    public void testKnowledge(){
        List<Map> knowledge = RestTemplateUtil.findKnowledge(24);
        for (Map map : knowledge) {
            Knowledge knowledge1 = new Knowledge();
            KnowledgeSubject knowledgeSubject = new KnowledgeSubject();
            knowledge1.setId(Long.parseLong(String.valueOf(map.get("id"))));
            knowledge1.setName(String.valueOf(map.get("name")));
            knowledge1.setLevel(Integer.parseInt(String.valueOf(map.get("level")))+1);
            knowledge1.setParentId(Long.parseLong(String.valueOf(map.get("parent"))));
            if(knowledge1.getLevel()==3){
                knowledge1.setIsLeaf(true);
            }
            knowledgeService.insert(knowledge1);
            knowledgeSubject.setKnowledgeId(knowledge1.getId());
            knowledgeSubject.setSubjectId(Long.parseLong(String.valueOf(map.get("subject"))));
            knowledgeSubjectService.insert(knowledgeSubject);
        }
    }
}

