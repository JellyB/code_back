package com.huatu.ztk.knowledge.service;

import com.google.common.collect.ArrayListMultimap;
import com.huatu.ztk.knowledge.bean.QuestionGeneticBean;
import com.huatu.ztk.knowledge.bean.QuestionStrategy;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.knowledge.dao.InitDao;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.PracticePaper;
import com.huatu.ztk.question.bean.*;
import com.huatu.ztk.question.common.QuestionMode;
import com.huatu.ztk.question.common.QuestionStatus;
import com.huatu.ztk.question.common.QuestionType;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Author: xuhuiqiang
 * Time: 2017-09-13  20:29 .
 * 智能抽题池维护
 */
@Service
public class InitService {
    private static final Logger logger = LoggerFactory.getLogger(InitService.class);

    @Autowired
    private InitDao initDao;

    @Autowired
    private QuestionStrategyDubboServiceImpl questionStrategyDubboService;

    @Resource(name = "redisObjectTemplate")
    private RedisTemplate<String,QuestionGeneticBean> redisTemplate2;
    /**
     * mogo中的行测题放到redis中
     */
    public Map<String,Integer> questionToRedis(){
        int startId = 0;

        deleteAllFromRedis();

        List<GenericQuestion> questions = initDao.findById(startId,100000);

        if(CollectionUtils.isNotEmpty(questions)){
            int n = questions.size();
            logger.info("试题总数={}",n);
            return addAllToRedis(questions);
        }
        return null;
    }

    private void deleteAllFromRedis(){
        String keystr = "subject_1_year_2017_module_418, subject_1_year_2017_module_417, subject_100100126_year_2017_module_8660, subject_1_year_2016_module_754, subject_2_year_2009_module_3250, subject_2_year_2014_module_770, subject_1001_year_2017_module_6747, subject_1_year_2010_module_482, subject_1_year_2017_module_425, subject_1_year_2009_module_754, subject_1_year_2017_module_422, subject_1_year_2017_module_661, subject_1_year_2017_module_709, subject_1_year_2016_module_482, subject_1_year_2017_module_429, subject_1_year_2008_module_392, subject_1_year_2011_module_392, subject_1_year_2017_module_702, subject_1_year_2017_module_428, subject_1_year_2016_module_642, subject_1_year_2008_module_435, subject_1_year_2017_module_395, subject_1_year_2017_module_392, subject_1_year_2017_module_437, subject_1_year_2017_module_712, subject_1_year_2017_module_435, subject_1_year_2017_module_674, subject_2_year_2011_module_770, subject_1_year_2017_module_430, subject_1_year_2017_module_431, subject_2_year_2016_module_3250, subject_1_year_2017_module_438, subject_1_year_2011_module_482, subject_2_year_2017_module_3250, subject_1_year_2017_module_439, subject_1_year_2017_module_714, subject_1_year_2014_module_482, subject_2_year_2010_module_3125, subject_2_year_2012_module_3250, subject_1_year_2014_module_642, subject_2_year_2011_module_3250, subject_1_year_2017_module_681, subject_1_year_2014_module_754, subject_1_year_2017_module_440, subject_2_year_2017_module_398, subject_2_year_2013_module_3250, subject_2_year_2017_module_393, subject_1_year_2017_module_447, subject_1_year_2017_module_689, subject_2_year_2014_module_3250, subject_1_year_2015_module_754, subject_1_year_2017_module_443, subject_1_year_2017_module_441, subject_2_year_2015_module_3250, subject_1_year_2013_module_642, subject_1017_year_2016_module_7001, subject_1017_year_2016_module_7002, subject_24_year_2017_module_10155, subject_1_year_2013_module_482, subject_1_year_2017_module_449, subject_1017_year_2016_module_7000, subject_1_year_2010_module_642, subject_2_year_2017_module_3125, subject_2_year_2016_module_3125, subject_2_year_2013_module_3125, subject_1_year_2017_module_451, subject_2_year_2014_module_3125, subject_2_year_2012_module_3125, subject_2_year_2010_module_3250, subject_1_year_2012_module_482, subject_2_year_2011_module_3125, subject_2_year_2015_module_3125, subject_1003_year_2017_module_6647, subject_1_year_2017_module_456, subject_1_year_2017_module_698, subject_1_year_2017_module_454, subject_1_year_2011_module_642, subject_1_year_2017_module_455, subject_1_year_2016_module_392, subject_2_year_2016_module_3195, subject_1_year_2008_module_482, subject_2_year_2017_module_3195, subject_1_year_2013_module_392, subject_1_year_2016_module_435, subject_2_year_2012_module_3195, subject_1_year_2008_module_642, subject_1_year_2010_module_435, subject_1_year_2012_module_642, subject_2_year_2011_module_3195, subject_1_year_2009_module_392, subject_2_year_2013_module_3195, subject_2_year_2014_module_3195, subject_1_year_2009_module_435, subject_1_year_2017_module_741, subject_2_year_2015_module_3195, subject_1_year_2017_module_749, subject_1_year_2017_module_625, subject_2_year_2012_module_770, subject_1_year_2010_module_392, subject_2_year_2010_module_3195, subject_1_year_2015_module_482, subject_1_year_2017_module_754, subject_1_year_2015_module_642, subject_1000_year_2017_module_6649, subject_1000_year_2017_module_6648, subject_2_year_2016_module_3298, subject_2_year_2016_module_3332, subject_2_year_2017_module_3332, subject_2_year_2017_module_3298, subject_2_year_2012_module_3332, subject_1_year_2014_module_435, subject_2_year_2013_module_3332, subject_2_year_2011_module_3332, subject_2_year_2013_module_3298, subject_2_year_2011_module_3298, subject_1_year_2017_module_482, subject_2_year_2010_module_3280, subject_1003_year_2017_module_6712, subject_1_year_2012_module_392, subject_1_year_2015_module_392, subject_2_year_2012_module_3298, subject_1003_year_2017_module_6711, subject_1_year_2017_module_525, subject_2_year_2015_module_3332, subject_2_year_2015_module_3298, subject_1_year_2017_module_402, subject_2_year_2014_module_3332, subject_1_year_2015_module_435, subject_1_year_2017_module_642, subject_1_year_2011_module_435, subject_2_year_2014_module_3298, subject_2_year_2013_module_770, subject_2_year_2016_module_3280, subject_1005_year_2017_module_6692, subject_2_year_2015_module_3280, subject_2_year_2017_module_3280, subject_2_year_2014_module_3280, subject_1_year_2017_module_407, subject_1_year_2017_module_408, subject_1_year_2017_module_405, subject_1_year_2014_module_392, subject_1_year_2017_module_406, subject_2_year_2012_module_3280, subject_2_year_2013_module_3280, subject_2_year_2011_module_3280, subject_1_year_2012_module_435, subject_2_year_2010_module_3332, subject_2_year_2010_module_3298, subject_1_year_2009_module_482, subject_1_year_2017_module_414, subject_1017_year_2017_module_6999, subject_14_year_2017_module_432, subject_1_year_2017_module_411, subject_1_year_2013_module_435, subject_1_year_2009_module_642, subject_1017_year_2016_module_6999";

        String[] keys = keystr.split(", ");
        for(int i=0,len=keys.length;i<len;i++){
            String key = keys[i];
            Set<QuestionGeneticBean> questionGeneticBeen1 = redisTemplate2.opsForSet().members(key);
            redisTemplate2.delete(key);
            Set<QuestionGeneticBean> questionGeneticBeen = redisTemplate2.opsForSet().members(key);
            logger.info("原总数={}，key={}，删除后后总数={}",questionGeneticBeen1.size(),key,questionGeneticBeen.size());
        }
        /*List<Integer> modules = Arrays.asList(392,435,482,642,754);
        for(int i=2008;i<2018;i++){
            for(int j=0,size=modules.size();j<size;j++){
                final String key = RedisKnowledgeKeys.getYearModuleQuestions(i,modules.get(j),subject);
                Set<QuestionGeneticBean> questionGeneticBeen1 = redisTemplate2.opsForSet().members(key);
                redisTemplate2.delete(key);
                Set<QuestionGeneticBean> questionGeneticBeen = redisTemplate2.opsForSet().members(key);
                logger.info("原总数={}，key={}，删除后后总数={}",questionGeneticBeen1.size(),key,questionGeneticBeen.size());
            }
        }*/
    }

    private Map<String,Integer> addAllToRedis(List<GenericQuestion> questions){
        final ArrayListMultimap<String, QuestionGeneticBean> multimap = ArrayListMultimap.create();
        for(Question question:questions){
            if(question.getId()==65177){
                logger.info("我就在这");
            }
            //不处理主观题和复合题及非公务员行测题
            if (question instanceof GenericSubjectiveQuestion || question instanceof CompositeSubjectiveQuestion || question instanceof CompositeQuestion) {
                logger.info("第一种情况不符合",question.getClass());
                continue;
            }
            GenericQuestion genericQuestion = (GenericQuestion)question;
            QuestionGeneticBean questionGeneticBean = QuestionGeneticBean.builder()
                    .id(genericQuestion.getId())
                    .difficulty(genericQuestion.getDifficult())
                    .moduleId(genericQuestion.getPoints().get(0))
                    .year(genericQuestion.getYear())
                    .build();
            final String yearModuleKey = RedisKnowledgeKeys.getYearModuleQuestionsV3(questionGeneticBean.getYear(),questionGeneticBean.getModuleId(),genericQuestion.getSubject());
            if(genericQuestion.getId()==65177){
                logger.info("我就在这");
            }
            if (!CollectionUtils.isNotEmpty(genericQuestion.getPoints()) || genericQuestion.getYear() < 2008 || genericQuestion.getParent() > 0 || genericQuestion.getMode() != QuestionMode.QUESTION_TRUE ||
                    (question.getType() != QuestionType.WRONG_RIGHT && question.getType() != QuestionType.SINGLE_CHOICE && question.getType() != QuestionType.MULTIPLE_CHOICE)||
                    question.getStatus() == QuestionStatus.DELETED || question.getStatus() == QuestionStatus.AUDIT_REJECT|| question.getStatus() == QuestionStatus.CREATED) {
                redisTemplate2.opsForSet().remove(yearModuleKey, questionGeneticBean);
            }


            //年份小于2008，复合题的子题，模拟题不做处理
            if(!CollectionUtils.isNotEmpty(genericQuestion.getPoints()) || genericQuestion.getYear() < 2008 || genericQuestion.getParent() > 0 || genericQuestion.getMode() != QuestionMode.QUESTION_TRUE ||
                    (question.getType() != QuestionType.WRONG_RIGHT && question.getType() != QuestionType.SINGLE_CHOICE && question.getType() != QuestionType.MULTIPLE_CHOICE)||
                    question.getStatus() == QuestionStatus.DELETED || question.getStatus() == QuestionStatus.AUDIT_REJECT|| question.getStatus() == QuestionStatus.CREATED){
                logger.info("第二种情况不符合",question.getClass());
                continue;
            }



            multimap.put(yearModuleKey,questionGeneticBean);
            logger.info("yearModuleKey={}，question的id={},multimap长度={}",yearModuleKey,question.getId(),multimap.size());
        }
        Map<String,Integer> map = new HashMap<>();
        Map<String,Integer> map1 = new HashMap<>();
        String ids = "";

        //遍历多值map,组装QuestionStrategy
        for (String key : multimap.keySet()) {
            List<QuestionGeneticBean> keyQuestions = multimap.get(key);
            QuestionGeneticBean[] qArray = new QuestionGeneticBean[keyQuestions.size()];
            QuestionGeneticBean[] keyQuestionsArray = keyQuestions.toArray(qArray);
            Set<QuestionGeneticBean> questionGeneticBeen1 = redisTemplate2.opsForSet().members(key);
            redisTemplate2.opsForSet().add(key, keyQuestionsArray);
            Set<QuestionGeneticBean> questionGeneticBeen = redisTemplate2.opsForSet().members(key);

            String moduleKey = key.split("module_")[1];



            logger.info("key={}，总数={}",key,questionGeneticBeen.size());
            logger.info("原先总数={}，key={}，存进去后总数={}",questionGeneticBeen1.size(),key,questionGeneticBeen.size());
            map.put(key+"_数量："+questionGeneticBeen1.size(),questionGeneticBeen.size());
            if(map1.containsKey(moduleKey)){
                int num = map1.get(moduleKey)+questionGeneticBeen.size();
                map1.put(moduleKey,num);
            }else{
                map1.put(moduleKey,questionGeneticBeen.size());
            }
        }
        logger.info("multimap.keySet()={}",multimap.keySet());
        map.putAll(map1);
        map.put(ids,1);
        return map;
    }

    /**
     * 将paper中的所有试题添加近redis中
     * @param pids
     */
    public void addPaperInnerQuestionsToRedis(List<Integer> pids){
        for(int i=0,size=pids.size();i<size;i++){
            logger.info("第几个={}--------------------------pid={}",i,pids.get(i));
            Paper paper = initDao.findPaperById(pids.get(i));
            logger.info("paper={}",paper);
            List<Integer> qids = paper.getQuestions();
            List<GenericQuestion> questions = new ArrayList<>();
            for(int qid:qids){
                GenericQuestion question = initDao.findById(qid);
                if(question!=null){
                    questions.add(question);
                }
            }
            addAllToRedis(questions);
        }
    }

    public void redisNum(){
        String keystr = "subject_1_year_2017_module_418, subject_1_year_2017_module_417, subject_100100126_year_2017_module_8660, subject_1_year_2016_module_754, subject_2_year_2009_module_3250, subject_2_year_2014_module_770, subject_1001_year_2017_module_6747, subject_1_year_2010_module_482, subject_1_year_2017_module_425, subject_1_year_2009_module_754, subject_1_year_2017_module_422, subject_1_year_2017_module_661, subject_1_year_2017_module_709, subject_1_year_2016_module_482, subject_1_year_2017_module_429, subject_1_year_2008_module_392, subject_1_year_2011_module_392, subject_1_year_2017_module_702, subject_1_year_2017_module_428, subject_1_year_2016_module_642, subject_1_year_2008_module_435, subject_1_year_2017_module_395, subject_1_year_2017_module_392, subject_1_year_2017_module_437, subject_1_year_2017_module_712, subject_1_year_2017_module_435, subject_1_year_2017_module_674, subject_2_year_2011_module_770, subject_1_year_2017_module_430, subject_1_year_2017_module_431, subject_2_year_2016_module_3250, subject_1_year_2017_module_438, subject_1_year_2011_module_482, subject_2_year_2017_module_3250, subject_1_year_2017_module_439, subject_1_year_2017_module_714, subject_1_year_2014_module_482, subject_2_year_2010_module_3125, subject_2_year_2012_module_3250, subject_1_year_2014_module_642, subject_2_year_2011_module_3250, subject_1_year_2017_module_681, subject_1_year_2014_module_754, subject_1_year_2017_module_440, subject_2_year_2017_module_398, subject_2_year_2013_module_3250, subject_2_year_2017_module_393, subject_1_year_2017_module_447, subject_1_year_2017_module_689, subject_2_year_2014_module_3250, subject_1_year_2015_module_754, subject_1_year_2017_module_443, subject_1_year_2017_module_441, subject_2_year_2015_module_3250, subject_1_year_2013_module_642, subject_1017_year_2016_module_7001, subject_1017_year_2016_module_7002, subject_24_year_2017_module_10155, subject_1_year_2013_module_482, subject_1_year_2017_module_449, subject_1017_year_2016_module_7000, subject_1_year_2010_module_642, subject_2_year_2017_module_3125, subject_2_year_2016_module_3125, subject_2_year_2013_module_3125, subject_1_year_2017_module_451, subject_2_year_2014_module_3125, subject_2_year_2012_module_3125, subject_2_year_2010_module_3250, subject_1_year_2012_module_482, subject_2_year_2011_module_3125, subject_2_year_2015_module_3125, subject_1003_year_2017_module_6647, subject_1_year_2017_module_456, subject_1_year_2017_module_698, subject_1_year_2017_module_454, subject_1_year_2011_module_642, subject_1_year_2017_module_455, subject_1_year_2016_module_392, subject_2_year_2016_module_3195, subject_1_year_2008_module_482, subject_2_year_2017_module_3195, subject_1_year_2013_module_392, subject_1_year_2016_module_435, subject_2_year_2012_module_3195, subject_1_year_2008_module_642, subject_1_year_2010_module_435, subject_1_year_2012_module_642, subject_2_year_2011_module_3195, subject_1_year_2009_module_392, subject_2_year_2013_module_3195, subject_2_year_2014_module_3195, subject_1_year_2009_module_435, subject_1_year_2017_module_741, subject_2_year_2015_module_3195, subject_1_year_2017_module_749, subject_1_year_2017_module_625, subject_2_year_2012_module_770, subject_1_year_2010_module_392, subject_2_year_2010_module_3195, subject_1_year_2015_module_482, subject_1_year_2017_module_754, subject_1_year_2015_module_642, subject_1000_year_2017_module_6649, subject_1000_year_2017_module_6648, subject_2_year_2016_module_3298, subject_2_year_2016_module_3332, subject_2_year_2017_module_3332, subject_2_year_2017_module_3298, subject_2_year_2012_module_3332, subject_1_year_2014_module_435, subject_2_year_2013_module_3332, subject_2_year_2011_module_3332, subject_2_year_2013_module_3298, subject_2_year_2011_module_3298, subject_1_year_2017_module_482, subject_2_year_2010_module_3280, subject_1003_year_2017_module_6712, subject_1_year_2012_module_392, subject_1_year_2015_module_392, subject_2_year_2012_module_3298, subject_1003_year_2017_module_6711, subject_1_year_2017_module_525, subject_2_year_2015_module_3332, subject_2_year_2015_module_3298, subject_1_year_2017_module_402, subject_2_year_2014_module_3332, subject_1_year_2015_module_435, subject_1_year_2017_module_642, subject_1_year_2011_module_435, subject_2_year_2014_module_3298, subject_2_year_2013_module_770, subject_2_year_2016_module_3280, subject_1005_year_2017_module_6692, subject_2_year_2015_module_3280, subject_2_year_2017_module_3280, subject_2_year_2014_module_3280, subject_1_year_2017_module_407, subject_1_year_2017_module_408, subject_1_year_2017_module_405, subject_1_year_2014_module_392, subject_1_year_2017_module_406, subject_2_year_2012_module_3280, subject_2_year_2013_module_3280, subject_2_year_2011_module_3280, subject_1_year_2012_module_435, subject_2_year_2010_module_3332, subject_2_year_2010_module_3298, subject_1_year_2009_module_482, subject_1_year_2017_module_414, subject_1017_year_2017_module_6999, subject_14_year_2017_module_432, subject_1_year_2017_module_411, subject_1_year_2013_module_435, subject_1_year_2009_module_642, subject_1017_year_2016_module_6999";

        String[] keys = keystr.split(", ");
        for(int i=0,len=keys.length;i<len;i++){
            String key = "new_"+keys[i];
            Set<QuestionGeneticBean> questionGeneticBeen = redisTemplate2.opsForSet().members(key);
            logger.info("key={}，总数={}",key,questionGeneticBeen.size());
        }
    }

    public Map<String,Integer> redisNum1(){
        List<GenericQuestion> questions = initDao.findById(0,100000);
        final ArrayListMultimap<String, QuestionGeneticBean> multimap = ArrayListMultimap.create();
        for(Question question:questions){
            //不处理主观题和复合题及非公务员行测题
            if (question instanceof GenericSubjectiveQuestion || question instanceof CompositeSubjectiveQuestion || question instanceof CompositeQuestion|| question.getSubject() != 1) {
                logger.info("第一种情况不符合",question.getClass());
                continue;
            }
            GenericQuestion genericQuestion = (GenericQuestion)question;
            //年份小于2008，复合题的子题，模拟题不做处理
            if(CollectionUtils.isEmpty(genericQuestion.getPoints()) || genericQuestion.getYear() < 2008 || genericQuestion.getParent() > 0 || genericQuestion.getMode() != QuestionMode.QUESTION_TRUE ||
                    (question.getType() != QuestionType.WRONG_RIGHT && question.getType() != QuestionType.SINGLE_CHOICE && question.getType() != QuestionType.MULTIPLE_CHOICE)){
                logger.info("第二种情况不符合",question.getClass());
                continue;
            }

            if(!CollectionUtils.isNotEmpty(genericQuestion.getPoints())){
                continue;
            }

            QuestionGeneticBean questionGeneticBean = QuestionGeneticBean.builder()
                    .id(genericQuestion.getId())
                    .difficulty(genericQuestion.getDifficult())
                    .moduleId(genericQuestion.getPoints().get(0))
                    .year(genericQuestion.getYear())
                    .build();
            final String yearModuleKey = RedisKnowledgeKeys.getYearModuleQuestionsV3(questionGeneticBean.getYear(),questionGeneticBean.getModuleId(),genericQuestion.getSubject());

            multimap.put(yearModuleKey,questionGeneticBean);
            logger.info("yearModuleKey={}，question的id={},multimap长度={}",yearModuleKey,question.getId(),multimap.size());
        }
        Map<String,Integer> map = new HashMap<>();
        //遍历多值map,组装QuestionStrategy
        for (String key : multimap.keySet()) {
            Set<QuestionGeneticBean> questionGeneticBeen = redisTemplate2.opsForSet().members(key);
            String moduleKey = key.split("module_")[1];
            logger.info("key={}，总数={}",key,questionGeneticBeen.size());
            if(map.containsKey(moduleKey)){
                int num = map.get(moduleKey)+questionGeneticBeen.size();
                map.put(moduleKey,num);
            }else{
                map.put(moduleKey,questionGeneticBeen.size());
            }

        }
        return map;
    }

    /**
     * 在对应的年份，模块列表中添加或删除试题
     * @param question
     */
    private void updateIdsYearModule(Question question){
        //不处理主观题和复合题
        if (question instanceof GenericSubjectiveQuestion || question instanceof CompositeSubjectiveQuestion || question instanceof CompositeQuestion ) {
            logger.info("第一种情况不符合",question.getClass());
            return;
        }

        GenericQuestion genericQuestion = (GenericQuestion)question;
        //年份小于2008，复合题的子题，模拟题不做处理
        if(genericQuestion.getYear()>2017 ||genericQuestion.getYear()<2008 || genericQuestion.getParent()>0 || genericQuestion.getMode()!=QuestionMode.QUESTION_TRUE ||
                (question.getType()!=QuestionType.WRONG_RIGHT && question.getType()!=QuestionType.SINGLE_CHOICE && question.getType()!=QuestionType.MULTIPLE_CHOICE)){
            logger.info("第二种情况不符合",question.getClass());
            return;
        }


        QuestionGeneticBean questionGeneticBean = QuestionGeneticBean.builder()
                .id(genericQuestion.getId())
                .difficulty(genericQuestion.getDifficult())
                .moduleId(genericQuestion.getPoints().get(0))
                .year(genericQuestion.getYear())
                .build();
        final String yearModuleKey = RedisKnowledgeKeys.getYearModuleQuestionsV3(questionGeneticBean.getYear(),questionGeneticBean.getModuleId(),genericQuestion.getSubject());

        if (question.getStatus() == QuestionStatus.DELETED || question.getStatus() == QuestionStatus.AUDIT_REJECT) {//删除则从推荐里面去掉
            redisTemplate2.opsForSet().remove(yearModuleKey,questionGeneticBean);
        }else {
            //redisTemplate2.delete(yearModuleKey);
            redisTemplate2.opsForSet().add(yearModuleKey,questionGeneticBean);
            //Set<QuestionGeneticBean> questionGeneticBeen = redisTemplate2.opsForSet().members("dfkasd");
            //redisTemplate.opsForSet().add(yearModuleKey,questionGeneticBean.getId()+"");
        }
    }

    public Map<String,Integer> deleteQuestionFromRedis(int qid){
        GenericQuestion genericQuestion = initDao.findById(qid);
        if(genericQuestion==null){
            return null;
        }
        QuestionGeneticBean questionGeneticBean = QuestionGeneticBean.builder()
                .id(genericQuestion.getId())
                .difficulty(genericQuestion.getDifficult())
                .moduleId(genericQuestion.getPoints().get(0))
                .year(genericQuestion.getYear())
                .build();
        final String yearModuleKey = RedisKnowledgeKeys.getYearModuleQuestionsV3(questionGeneticBean.getYear(),questionGeneticBean.getModuleId(),genericQuestion.getSubject());
        Set<QuestionGeneticBean> questionGeneticBeen1 = redisTemplate2.opsForSet().members(yearModuleKey);
        redisTemplate2.opsForSet().remove(yearModuleKey,questionGeneticBean);
        Set<QuestionGeneticBean> questionGeneticBeen2 = redisTemplate2.opsForSet().members(yearModuleKey);
        logger.info("原先总数={}，删除后总数={}",questionGeneticBeen1.size(),questionGeneticBeen2.size());
        Map<String,Integer> map = new HashMap<>();
        map.put("新池子中的原先总数",questionGeneticBeen1.size());
        map.put("新池子中的删除后总数",questionGeneticBeen2.size());
        final String yearModuleKey1 = RedisKnowledgeKeys.getYearModuleQuestionsV3(questionGeneticBean.getYear(),questionGeneticBean.getModuleId(),genericQuestion.getSubject());
        Set<QuestionGeneticBean> questionGeneticBeen11 = redisTemplate2.opsForSet().members(yearModuleKey1);
        redisTemplate2.opsForSet().remove(yearModuleKey1,questionGeneticBean);
        Set<QuestionGeneticBean> questionGeneticBeen21 = redisTemplate2.opsForSet().members(yearModuleKey1);
        logger.info("原先总数={}，删除后总数={}",questionGeneticBeen11.size(),questionGeneticBeen21.size());
        map.put("旧池子中的原先总数",questionGeneticBeen11.size());
        map.put("旧池子中的删除后总数",questionGeneticBeen21.size());
        return map;
    }

    /**
     * 智能出题
     * @param size
     * @param uid
     * @param subject
     * @return
     */
    public PracticePaper createSmartPaper(int size, long uid, int subject) {

        long stime = System.currentTimeMillis();
        QuestionStrategy questionStrategy = new QuestionStrategy();
        questionStrategy = questionStrategyDubboService.smartStrategy(uid,subject, size);

        logger.info("get questionStrategy time={},uid={}", System.currentTimeMillis() - stime, uid);

        long stime2 = System.currentTimeMillis();
        final PracticePaper practicePaper = PracticePaper.builder().build();
        practicePaper.setQuestions(questionStrategy.getQuestions());
        practicePaper.setDifficulty(questionStrategy.getDifficulty());
        practicePaper.setQcount(questionStrategy.getQuestions().size());
        practicePaper.setModules(questionStrategy.getModules());
        practicePaper.setSubject(subject);
        return practicePaper;
    }


}
