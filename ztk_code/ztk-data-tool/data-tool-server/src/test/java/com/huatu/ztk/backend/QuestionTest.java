package com.huatu.ztk.backend;

import com.google.common.collect.Lists;
import com.huatu.ztk.backend.question.bean.QuestionMin;
import com.huatu.ztk.backend.question.service.QuestionService;
import com.huatu.ztk.backend.subject.dao.SubjectDao;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionExtend;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2017\11\22 0022.
 */
public class QuestionTest extends BaseTestW{
    private final static Logger logger = LoggerFactory.getLogger(QuestionTest.class);
    @Autowired
    private QuestionService questionService;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private SubjectDao subjectDao;
    /**
     * 查询某一科目下的某一模块下的总题数
     */
    @Test
    public void test(){
        Integer[] moduleIds = {276,277,282,283,284,285,286,287,288,289,290,291,370};
        int subject = 100100126;
        String area = "";
        int mode = 2;   //-1全量1真题2模拟题
        for(int module:moduleIds){
            Object questionDetails = questionService.findByDetail(subject,module,"",-1,mode,-1,"",-1);
            if(questionDetails instanceof List){
                List<QuestionMin> list = (List<QuestionMin>)questionDetails;
                logger.info("moduleId={},发布总数为：{}",module,list.size());
            }
            Criteria criteria = new Criteria().where("moduleId").is(module);
            List<QuestionExtend> extendList = mongoTemplate.find(new Query(criteria),QuestionExtend.class);
            List<Integer> ids = Lists.newArrayList();
            extendList.forEach(i->ids.add(i.getQid()));

            Criteria criteria1 = new Criteria().where("id").in(ids).and("status").ne(4);
            if(mode!=-1){
                criteria1.and("mode").is(mode);
            }
            List<Question> questions = mongoTemplate.find(new Query(criteria1),Question.class);
            logger.info("moduleId={},上传总数为：{}",module,questions.size());
        }
    }

    /**
     * 通过mongo查询统计科目及科目下的试题
     */
    @Test
    public void countSubjectWithQuestion(){
        String object = "[{\"_id\":100100196,\"num_tutorial\":22.0},{\"_id\":100100186,\"num_tutorial\":147.0},{\"_id\":1006,\"num_tutorial\":1.0},{\"_id\":100100191,\"num_tutorial\":123.0},{\"_id\":100100189,\"num_tutorial\":337.0},{\"_id\":100100187,\"num_tutorial\":123.0},{\"_id\":100100262,\"num_tutorial\":80.0},{\"_id\":100100193,\"num_tutorial\":255.0},{\"_id\":24,\"num_tutorial\":1047.0},{\"_id\":1005,\"num_tutorial\":1064.0},{\"_id\":1000,\"num_tutorial\":5895.0},{\"_id\":100100167,\"num_tutorial\":110.0},{\"_id\":100100263,\"num_tutorial\":1556.0},{\"_id\":100100190,\"num_tutorial\":261.0},{\"_id\":1009,\"num_tutorial\":604.0},{\"_id\":1017,\"num_tutorial\":1682.0},{\"_id\":1003,\"num_tutorial\":515.0},{\"_id\":100100221,\"num_tutorial\":1.0},{\"_id\":2,\"num_tutorial\":33613.0},{\"_id\":-1,\"num_tutorial\":255.0},{\"_id\":1,\"num_tutorial\":137287.0},{\"_id\":100100188,\"num_tutorial\":125.0},{\"_id\":1019,\"num_tutorial\":103.0},{\"_id\":1018,\"num_tutorial\":2240.0},{\"_id\":100100177,\"num_tutorial\":2142.0},{\"_id\":100100126,\"num_tutorial\":11226.0},{\"_id\":3,\"num_tutorial\":4776.0},{\"_id\":100100176,\"num_tutorial\":3324.0},{\"_id\":100100192,\"num_tutorial\":257.0},{\"_id\":100100145,\"num_tutorial\":391.0},{\"_id\":1020,\"num_tutorial\":268.0},{\"_id\":14,\"num_tutorial\":14.0},{\"_id\":100100175,\"num_tutorial\":1254.0},{\"_id\":100100259,\"num_tutorial\":18.0}]";
        List<Map> subjectMap = JsonUtil.toList(object,Map.class);
        Map<Integer,String> map = subjectDao.findAll().stream().collect(Collectors.toMap(i->i.getId(),i->i.getName()));
        subjectMap.stream().forEach(i->i.put("name",map.get(Integer.parseInt(i.get("_id").toString()))));
        System.out.println("cone:"+JsonUtil.toJson(subjectMap));
    }

    @Test
    public void test1(){
        int subject = 1;
        int cursor = 1;
        Query query = new Query(Criteria.where("id").gt(cursor).and("subject").is(subject).and("status").is(2));
        query.with(new Sort(Sort.Direction.ASC,"id")).limit(100);
        List<Question> questions = mongoTemplate.find(query, Question.class);
        System.out.println("cones:"+questions.stream().filter(i->i instanceof GenericQuestion).map(i->i.getId()).collect(Collectors.toList()));
    }


}
