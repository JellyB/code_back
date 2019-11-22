package com.huatu.tiku.teacher.service.impl.question;

import com.google.common.collect.Lists;
import com.huatu.tiku.teacher.service.question.QuestionCheckService;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.teacher.dao.mongo.NewQuestionDao;
import com.huatu.tiku.teacher.dao.question.BaseQuestionMapper;
import com.huatu.ztk.question.bean.Question;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhaoxi
 * @Description: TODO
 * @date 2018/9/17下午5:36
 */
@Slf4j
@Service
public class QuestionCheckServiceImpl  implements QuestionCheckService {

    @Autowired
    private NewQuestionDao newQuestionDao;
    @Autowired
    private BaseQuestionMapper baseQuestionMapper;

    @Override
    public void check() {

        List<String> resultList = Lists.newArrayList();

        //分片查询mysql和mongo的数据比较
        int startIndex = 0;
        int offset = 1000;
        while(true){
            //查询MONGO复合条件的id（左开右闭）
            List<Question> mongoQuestionList= newQuestionDao.findByIdGtAndLimit(startIndex,offset);
            List<String> mongoQuestionIdList = Lists.newArrayList();
            if(CollectionUtils.isEmpty(mongoQuestionList)){
                break;
            }
            mongoQuestionList.forEach(question -> mongoQuestionIdList.add(question.getId()+""));
            int endIndex = mongoQuestionList.stream().map(Question::getId).max(Comparator.comparing(Integer::intValue)).get();

            //查询MYSQL复合条件的id（左开右闭）
            List<String> sqlQuestionIdList = Lists.newArrayList();
            List<Map<String,Long>> sqlQuestionList = baseQuestionMapper.findIdBetweenAnd(startIndex, endIndex);
            if(CollectionUtils.isNotEmpty(sqlQuestionList)){
                sqlQuestionList.forEach(question -> sqlQuestionIdList.add(question.get("id")+""));
            }
            //差集
            List<String> disjunction = (List<String>)CollectionUtils.disjunction(mongoQuestionIdList , sqlQuestionIdList);

            resultList.addAll(disjunction);
            log.info("("+startIndex+","+endIndex+"]disju size "+disjunction.size()+"===="+disjunction);
            startIndex = endIndex;
        }
        Example example = new Example(BaseQuestion.class);
        example.and().andGreaterThan("id",startIndex);
        List<BaseQuestion> baseQuestions = baseQuestionMapper.selectByExample(example);
        if(CollectionUtils.isNotEmpty(baseQuestions)){
            resultList.addAll(baseQuestions.stream().map(BaseQuestion::getId).map(String::valueOf).collect(Collectors.toList()));
        }
        log.info("resultList size "+resultList.size()+"===="+resultList);

    }




}
