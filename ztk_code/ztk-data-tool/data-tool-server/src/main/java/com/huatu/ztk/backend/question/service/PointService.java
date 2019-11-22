package com.huatu.ztk.backend.question.service;

import com.alibaba.dubbo.rpc.RpcException;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huatu.ztk.backend.question.bean.QuestionPointTreeMin;
import com.huatu.ztk.backend.question.dao.PointDao;
import com.huatu.ztk.backend.question.dao.QuestionDao;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.exception.IllegalQuestionException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Author: xuhuiqiang
 * Time: 2017-02-07  14:53 .
 */
@Service
public class PointService {
    private static final Logger logger = LoggerFactory.getLogger(PointService.class);
    @Autowired
    private PointDao pointDao;
    @Autowired
    private QuestionDubboService questionDubboService;
    @Autowired
    private QuestionDao questionDao;
    /**
     * 根据json中的name、parentId、level，插入新知识点
     * @param str
     * @return
     */
    public Object addPoint(String str){
        logger.info("传输过来的数据={}",str);
        Map<String,Object> result = JsonUtil.toMap(str);
        String name = String.valueOf(result.get("name")) ;
        int parentId = Integer.parseInt(String.valueOf(result.get("parentId"))) ;
        int level = Integer.parseInt(String.valueOf(result.get("level"))) ;
        int subject = Integer.parseInt(String.valueOf(result.get("subject"))) ;
        return pointDao.insertPoint(name,parentId,level,subject);
        //QuestionPointTreeMin qpt = pointDao.findPointByDetail(name,parentId,level,subject);
        //return qpt.getId();
    }

    /**
     * 根据id，nam，修改知识点
     */
    public Object editPoint(int id,String name){
        if(pointDao.editPoint(id,name)){
            return 1;
        }else {
            return 0;
        }
    }

    /**
     * 根据id，删除知识点
     * @param id
     * @return
     */
    public Object deletePoint(int id){
        if(pointDao.deletePoint(id)){
            return 1;
        }else {
            return 0;
        }
    }

    /**
     * 刷新事业单位的知识点名称修改问题
     * @param subjectId
     */
    public void updateQuestionPoint(int subjectId)  {
        //知识点id -> 知识点name
        final Map<Integer,String> mapData = pointDao.findAllPonitsBySubject(subjectId).stream().collect(Collectors.toMap(i->i.getId(), i->i.getName()));
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
                try {
                    updateQuestion(question,mapData,errorIds,questionIds);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            total += questionList.size();
            logger.info("total = {}",total);
        }
        logger.info("知识点有问题的题目有{}个：{}",questionIds.size(),questionIds);
        logger.info("有问题的知识点id有{}个：{}",errorIds.size(),errorIds);


    }
    public void updateQuestion(Question question,final Map<Integer,String> mapData,Set<Integer> errorIds,Set<Integer> questionIds) throws InterruptedException {
        if(question instanceof GenericQuestion){
            List<Integer> pointId = ((GenericQuestion) question).getPoints();
            List<String> oldNames = ((GenericQuestion) question).getPointsName();
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
            if(CollectionUtils.isEmpty(pointName)){
                return;
            }
            boolean commonFlag = true;
            if(CollectionUtils.isNotEmpty(oldNames)&&CollectionUtils.size(oldNames)==3){
                for(int i = 0;i<oldNames.size();i++){
                    if(!oldNames.get(i).equals(pointName.get(i))){
                        commonFlag =false;
                        break;
                    }
                }
            }else{
                commonFlag = false;
            }
            if(!commonFlag){
                ((GenericQuestion) question).setPointsName(pointName);
                questionDao.updateQuestion(question);   //只对mongo做操作，因为不影响缓存，但是需要清理rocksdb数据
//                try {
//                    questionDubboService.update(question);
//                    logger.info("------->{}",question.getId());
//                } catch (IllegalQuestionException e) {
//                    e.printStackTrace();
//                }catch (RpcException e){
//                    Thread.currentThread().sleep(1000);
//                    updateQuestion(question,mapData,errorIds,questionIds);
//                }
            }
        }

    }

    public Object findAll(int subject) {
        if(subject<=0){
            return  pointDao.findAllPonits();
        }
        return pointDao.findAllPonitsBySubject(subject);
    }
}
