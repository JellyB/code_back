package com.huatu.ztk.backend.question.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.huatu.ztk.backend.constant.RedisKeyConstant;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.paper.service.CreatePaperWordServiceV2;
import com.huatu.ztk.backend.question.dao.PointDao;
import com.huatu.ztk.backend.question.dao.QuestionDao;
import com.huatu.ztk.backend.subject.dao.SubjectDao;
import com.huatu.ztk.backend.util.FunFileUtils;
import com.huatu.ztk.backend.util.RestTemplateUtil;
import com.huatu.ztk.backend.util.UploadFileUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.common.PaperType;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionStatus;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\6\11 0011.
 */
@Service
public class QuestionServiceV1 {
    private static final Logger logger = LoggerFactory.getLogger(QuestionServiceV1.class);
    @Autowired
    QuestionDao questionDao;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    PointDao pointDao;
    @Autowired
    QuestionDubboService questionDubboService;
    @Autowired
    CreatePaperWordServiceV2 createPaperWordService;
    @Autowired
    UploadFileUtil uploadFileUtil;
    @Autowired
    SubjectDao subjectDao;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    PaperDao paperDao;
    /**
     * 创建试卷按照试题的
     * @param subjectId
     * @param pointId
     * @param isReNew
     * @return
     */
    public List<Question> findPageQuestions(Integer subjectId, Integer pointId, Integer isReNew) {
        List<Integer> questions = questionDao.findAllExportQuestion();
        int cursor = 0;
        String startKey = RedisKeyConstant.getDownloadId(subjectId,pointId);
        //如果不重新实现
        if (isReNew!=1) {
            Object obj = redisTemplate.opsForValue().get(startKey);
            if(obj!=null){
                cursor = Integer.parseInt(obj.toString());
                logger.info("redis:get:cursor:{}",cursor);
            }
        }
        long start = System.currentTimeMillis();
        int size = 100;
        List<Question> result = Lists.newArrayList();
        while(true){
            List<Question> questionList = questionDao.findQuestionsByPointForPage(cursor,size,subjectId,pointId);
            if(CollectionUtils.isEmpty(questionList)){
                logger.info("已无试题需要处理，进程结束");
                break;
            }
            cursor = questionList.get(questionList.size()-1).getId();
            questionList.removeIf(i->i.getStatus()== QuestionStatus.DELETED);
            questionList.removeIf(i->questions.contains(i));
            if(CollectionUtils.isNotEmpty(questionList)){
                logger.info("ids={}",questionList.stream().filter(i->((GenericQuestion)i).getPoints().contains(pointId)).map(i->i.getId()).collect(Collectors.toList()));
                result.addAll(questionList.stream().filter(i->((GenericQuestion)i).getPoints().contains(pointId)).collect(Collectors.toList()));
                if(result.size()>=size){
                    result =  result.subList(0,size);
                    cursor = result.get(result.size()-1).getId();
                    break;
                }
            }
        }
        long end = System.currentTimeMillis();
        logger.info("处理需要时间：{}",(end-start)/1000);
        redisTemplate.opsForValue().set(startKey,cursor+"");
        logger.info("redis:set:cursor:{}",cursor);
        return result;
    }

    public Object deleteUserCollect(Long userId, Integer questionId) throws BizException {
        List<GenericQuestion> questions = questionDao.findById(questionId);
        if(CollectionUtils.isEmpty(questions)){
            throw new BizException(ErrorResult.create(1000012,"题目不存在"));
        }
        int subject = questions.get(0).getSubject();
        List<Integer> pointIds = pointDao.findAllPonitsBySubject(subject).stream().map(i->i.getId()).collect(Collectors.toList());
        final ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        final HashOperations<String,String,String> hashOperations = redisTemplate.opsForHash();
        for(Integer pointId:pointIds){
            String collectSetKey = RedisKnowledgeKeys.getCollectSetKey(userId, pointId);
            if(zSetOperations.remove(collectSetKey,String.valueOf(questionId))>0){
                logger.info("试题绑定知识点-{},被取消",pointId);
                final String collectCountKey = RedisKnowledgeKeys.getCollectCountKey(userId);
                //当前知识点收藏的试题数量
                final Long total = zSetOperations.size(collectSetKey);
                //保存知识点->试题数量关系
                hashOperations.put(collectCountKey,String.valueOf(pointId),String.valueOf(total.intValue()));
            }
        }
        return null;
    }

    /**
     * 将map 转换类型
     * @param source
     * @return
     */
    private Map<Integer,Integer> convert(Map<String,String> source){
        final HashMap<Integer, Integer> target = Maps.newHashMapWithExpectedSize(source.size());
        if (target == null) {
            return Maps.newHashMap();
        }

        for (String key : source.keySet()) {
            Integer newValue = Ints.tryParse(source.get(key));
            Integer newKey = Ints.tryParse(key);
            if (newValue == null || newKey == null) {
                logger.error("illegal key or value,can`t convert to int,key={},value={}",key,source.get(key));
                continue;
            }

            target.put(newKey,newValue);
        }
        return target;
    }

    public Object createQuestionFileByModule(Integer moduleId) {
        List<Question> questions = Lists.newArrayList();
        if(moduleId==19){
            List<Integer> multiIds = questionDao.findByModuleId1(moduleId);
            List<Integer> ids = questionDao.sortMultiIds(multiIds);
            multiIds = ids.subList(0,ids.size()/4);
            Collections.shuffle(multiIds);
            List<Integer> questionIds = questionDao.findByMultiIds(multiIds.subList(0,multiIds.size()>40?40:multiIds.size()));
            questions.addAll(questionDubboService.findBath(questionIds));
        }else{
            List<Integer> questionIds = questionDao.findByModuleId(moduleId);
            questionIds = questionIds.subList(0,questionIds.size()/4);
            Collections.shuffle(questionIds);
            List<Integer> resultIds = questionIds.subList(0,questionIds.size()>200?200:questionIds.size());
            logger.info("muduleId={},size={}",moduleId,resultIds.size());
            questions.addAll(questionDubboService.findBath(resultIds));
            logger.info("muduleId={},size={}",moduleId,questions.size(),questions.stream().map(i->i.getId()).collect(Collectors.toList()));
            questions.sort((a,b)->comparePercent(a,b));
        }
        String moduleName = questionDao.findMoudleName(moduleId);
        File file = new File(FunFileUtils.TMP_WORD_SOURCE_FILEPATH+moduleName+".doc");
        createPaperWordService.createFileWord(file,questions.stream().map(i->i.getId()).collect(Collectors.toList()), 1);
//        File file = new File(FunFileUtils.TMP_WORD_SOURCE_FILEPATH+moduleName+"题干.doc");
//        createPaperWordService.createFileWord(file,questions.stream().map(i->i.getId()).collect(Collectors.toList()), 2);
//        File file1 = new File(FunFileUtils.TMP_WORD_SOURCE_FILEPATH+moduleName+"答案.doc");
//        createPaperWordService.createFileWord(file1,questions.stream().map(i->i.getId()).collect(Collectors.toList()), 3);
        return SuccessMessage.create("成功");
    }

    private int comparePercent(Question a, Question b) {
        GenericQuestion a1 = (GenericQuestion) a;
        GenericQuestion b1 = (GenericQuestion) b;
        return a1.getMeta().getCount()-b1.getMeta().getCount();
    }

    public Object createQuestionFile(Integer subject, Integer pointId, Integer isReNew) {
        String name = subjectDao.findAll().stream().filter(i->i.getId()==subject).findFirst().get().getName()+"_"+pointDao.findPointById(pointId).getName();
        if(isReNew==1){
            File drFile =new File(FunFileUtils.TMP_WORD_SOURCE_FILEPATH);

            Collection<File> files = Arrays.stream(drFile.listFiles()).filter(i->i.getName().indexOf(name)!=-1).collect(Collectors.toList());
            logger.info("files = {}", files.stream().map(i->i.getName()).collect(Collectors.toList()));
            files.stream().forEach(file -> file.delete());
        }
        int index = 0;
        int total = 0;
        while(true){
            List<Question> questionList = findPageQuestions(subject,pointId,isReNew);
            if(CollectionUtils.isEmpty(questionList)){
                break;
            }
            File file1 = new File(FunFileUtils.TMP_WORD_SOURCE_FILEPATH +name+"_"+index+".doc");
            createPaperWordService.createFileWord(file1,questionList.stream().map(i->i.getId()).collect(Collectors.toList()), 1);
            index++;
            total+=questionList.size();
            isReNew = 11;
            logger.info("{}_{}:{}",subject,pointId,total);
        }

        File drFile =new File(FunFileUtils.TMP_WORD_SOURCE_FILEPATH);
        Collection<File> files = Arrays.stream(drFile.listFiles()).filter(i->i.getName().indexOf(name)!=-1).collect(Collectors.toList());
        try {
            String url = unzipFile(files.stream().map(i->i.getName().replace(".doc","")).collect(Collectors.toList()));
            return SuccessMessage.create(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ErrorResult.create(1000006,"失败！！！");

    }
    private String unzipFile(List<String> names) throws Exception {

        String zipName = DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
        boolean bln = FunFileUtils.unzipFile(zipName, 1, names);
        File fileZip = new File(FunFileUtils.TMP_WORD_SOURCE_FILEPATH + zipName + ".zip");
        if (bln) {
            //ftp上传文件  ?  windows 系统 ，上次服务器放开注释
            uploadFileUtil.ftpUploadFile(fileZip, new String(fileZip.getName().getBytes("UTF-8"), "iso-8859-1"), FunFileUtils.WORD_FILE_SAVE_PATH);
            //删除临时文件
            FunFileUtils.deleteFile(fileZip);
            //下载地址
            return FunFileUtils.WORD_FILE_SAVE_URL + zipName + ".zip";
        } else {
            //删除临时文件
            FunFileUtils.deleteFile(fileZip);
            throw new BizException(ErrorResult.create(1000107, "下载试卷失败"));
        }
    }

    /**
     * 同步所有的试题到mysql(teacher)
     * @param subjectId
     */
    public void syncQuestionsBySubject(int subjectId) {
        StopWatch stopWatcht = new StopWatch();
        stopWatcht.start("findPaper");
        List<Paper> papers = paperDao.findBySubject(subjectId, Lists.newArrayList(PaperType.TRUE_PAPER, PaperType.MATCH));
        stopWatcht.stop();
        for (Paper paper : papers) {
            stopWatcht.start("paper.sync："+paper.getName());
            RestTemplateUtil.sync2Mysql(paper);
            stopWatcht.stop();
        }
        logger.info(stopWatcht.prettyPrint());

    }
}

