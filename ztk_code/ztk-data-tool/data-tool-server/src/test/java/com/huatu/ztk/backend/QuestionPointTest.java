package com.huatu.ztk.backend;

import com.alibaba.dubbo.rpc.RpcException;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huatu.ztk.backend.question.dao.PointDao;
import com.huatu.ztk.backend.question.dao.QuestionDao;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.exception.IllegalQuestionException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\3\16 0016.
 */
public class QuestionPointTest extends BaseTestW {
    private final static Logger logger = LoggerFactory.getLogger(QuestionPointTest.class);
    @Autowired
    private PointDao pointDao;
    @Autowired
    private QuestionDao questionDao;
    @Autowired
    private QuestionDubboService questionDubboService;
    @Test
    public void test() throws InterruptedException {
        int subjectId = 2;
        final Map<Integer,String> mapData = pointDao.findAllPonitsBySubject(subjectId).stream().collect(Collectors.toMap(i->i.getId(),i->i.getName()));
        int cursor = 0;
        int size = 100;
        int total = 0;
        Set<Integer> errorIds = Sets.newHashSet();
        Set<Integer> questionIds = Sets.newHashSet();
        while(true){
            List<Question> questionList = questionDao.findQuestionsForPage(cursor,size,subjectId);
            if(CollectionUtils.isEmpty(questionList)){
                logger.info("已无试题需要处理，进程结束");
                break;
            }
            cursor = questionList.get(questionList.size()-1).getId();
            for (Question question : questionList) {
                test2(question,mapData,errorIds,questionIds);
            }
            total += questionList.size();
            logger.info("total = {}",total);
        }
        logger.info("知识点有问题的题目有{}个：{}",questionIds.size(),questionIds);
        logger.info("有问题的知识点id有{}个：{}",errorIds.size(),errorIds);


    }
    public void test2(Question question, final Map<Integer,String> mapData, Set<Integer> errorIds, Set<Integer> questionIds) throws InterruptedException {
        if(question instanceof GenericQuestion){
            List<Integer> pointId = ((GenericQuestion) question).getPoints();
            List<String> pointName = Lists.newArrayListWithCapacity(3);
            for(Integer id:pointId){
                String temp = mapData.get(id);
                if(StringUtils.isNotBlank(temp)){
                    pointName.add(temp);
                }else{
                    errorIds.add(id);
                    questionIds.add(question.getId());
                    return;
                }
            }
            ((GenericQuestion) question).setPointsName(pointName);
            try {
                questionDubboService.update(question);
                logger.info("------->{}",question.getId());
            } catch (IllegalQuestionException e) {
                e.printStackTrace();
            }catch (RpcException e){
                Thread.currentThread().sleep(1000);
                test2(question,mapData,errorIds,questionIds);
            }
        }

    }
}
