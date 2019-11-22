package com.huatu.tiku.teacher;

import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.entity.subject.Subject;
import com.huatu.tiku.entity.teacher.PaperActivity;
import com.huatu.tiku.entity.teacher.PaperEntity;
import com.huatu.tiku.teacher.service.PaperActivityService;
import com.huatu.tiku.teacher.service.paper.PaperEntityService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.tiku.teacher.service.subject.TeacherSubjectService;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.question.bean.Question;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import tk.mybatis.mapper.entity.Example;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\6\23 0023.
 */
public class MongoT extends TikuBaseTest {
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    CommonQuestionServiceV1 commonQuestionServiceV1;
    @Autowired
    PaperEntityService paperEntityService;
    @Autowired
    PaperActivityService paperActivityService;
    @Autowired
    TeacherSubjectService teacherSubjectService;
    @Test
    public void test(){
        long count = mongoTemplate.count(new Query(),"ztk_question");
        System.out.println("查询结果："+count);
    }

    @Test
    public void test2(){
        Long subject = 100100175L;
        List<Question> questions = findBySubject(subject.intValue());
        if(CollectionUtils.isEmpty(questions)){
            System.out.println("无数据");
            return;
        }
        List<Long> collect = questions.stream().map(Question::getId).map(Long::new).collect(Collectors.toList());
        Example example = new Example(BaseQuestion.class);
        example.and().andEqualTo("subjectId",subject).andNotIn("id",collect);
        List<BaseQuestion> baseQuestions = commonQuestionServiceV1.selectByExample(example);
        if(CollectionUtils.isNotEmpty(baseQuestions)){
            System.out.println("未同步数据："+ StringUtils.join(baseQuestions.stream().map(BaseQuestion::getId).collect(Collectors.toList()),","));
            return;
        }
        System.out.println("无未同步数据");
    }

    public List<Question> findBySubject(int subject){
        Criteria criteria = Criteria.where("subject").gte(subject);
        Query query = new Query(criteria);
        return mongoTemplate.find(query,Question.class,"ztk_question_new");
    }

    /**
     * 查询哪些试卷在pandora题库中不存在
     */
    @Test
    public void paperSyncTest(){
        Criteria criteria = Criteria.where("status").is(2).and("type").in(Lists.newArrayList(1,9));
        Query query = new Query(criteria);
        List<Paper> papers = mongoTemplate.find(query, Paper.class, "ztk_paper");
        List<Long> subjectIds = getSubjectIds();
        papers.removeIf(i->!subjectIds.contains(new Long(i.getCatgory())));
        List<Long> ids = papers.stream().map(Paper::getId).map(Long::new).collect(Collectors.toList());
        System.out.println("papers.size() = " + papers.size());
        List<PaperEntity> entityByIds = findEntityByIds(ids);
        List<PaperActivity> activityByIds = findActivityByIds(ids);
        List<Long> paperEntityIds = entityByIds.stream().map(PaperEntity::getId).collect(Collectors.toList());
        List<Long> paperActivityIds = activityByIds.stream().map(PaperActivity::getId).collect(Collectors.toList());
        Collection<Long> activitySet = CollectionUtils.disjunction(ids, paperActivityIds);
        activitySet.removeAll(paperActivityIds);
        System.out.println("activitySet.size() = " + activitySet.size());
        System.out.println("activitySet = " + JsonUtil.toJson(activitySet));
        activitySet.removeAll(paperEntityIds);
        System.out.println("activitySet-entity.size() = " + activitySet.size());
        System.out.println("activitySet-entity = " + JsonUtil.toJson(activitySet));
        for (Long paperId : activitySet) {
            Paper paper = papers.stream().filter(i -> i.getId() == paperId.intValue()).findAny().get();
            System.out.println("paper.getId() = " + paper.getId()+ ">:paper.getName() = " + paper.getName());
        }
//        paperEntityIds.removeAll(paperActivityIds);
//        System.out.println("paperEntityIds.size() = " + paperEntityIds.size());
//        System.out.println("paperEntityIds = " + JsonUtil.toJson(paperEntityIds));
//        for (Long paperEntityId : paperEntityIds) {
//            Paper paper = papers.stream().filter(i -> i.getId() == paperEntityId.intValue()).findAny().get();
//            System.out.println("paper.getId() = " + paper.getId()+ ">:paper.getName() = " + paper.getName());
//        }
    }

    private List<PaperActivity> findActivityByIds(List<Long> ids) {
        Example example = new Example(PaperActivity.class);
        example.and().andIn("id",ids);
        return paperActivityService.selectByExample(example);
    }

    private List<PaperEntity> findEntityByIds(List<Long> ids) {
        Example example = new Example(PaperEntity.class);
        example.and().andIn("id",ids);
        return paperEntityService.selectByExample(example);
    }

    public List<Long> getSubjectIds(){
        Example example = new Example(Subject.class);
        example.and().andEqualTo("level",2);
        List<Subject> subjectList = teacherSubjectService.selectByExample(example);
        return subjectList.stream().map(Subject::getId).collect(Collectors.toList());
    }
}

