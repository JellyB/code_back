package com.huatu.ztk.backend.question.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.backend.paper.bean.TikuQuestionType;
import com.huatu.ztk.backend.question.bean.QuestionMin;
import com.huatu.ztk.backend.question.dao.QuestionScatteredDao;
import com.huatu.ztk.commons.AreaConstants;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.*;
import com.huatu.ztk.question.common.QuestionStatus;
import com.huatu.ztk.question.exception.IllegalQuestionException;
import com.itextpdf.text.BadElementException;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.RabbitAccessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: xuhuiqiang
 * Time: 2017-06-15  17:14 .
 */
@Service
public class QuestionScatteredService {
    private static final Logger logger = LoggerFactory.getLogger(QuestionScatteredService.class);

    @Autowired
    private QuestionScatteredDao questionScatteredDao;

    @Autowired
    private QuestionService questionService;

//    @Autowired
//    private QuestionDubboService questionDubboService;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 查找散题
     * @param subject
     * @param stem
     * @param pointId
     * @param startTime
     * @param endTime
     * @param flag
     * @param uid
     * @return
     */
    public Object findByDetail(int subject,String stem,List<Integer> pointId,long startTime,long endTime,int flag,long uid,int status,int module){
        if(flag==-1){
            uid = -1;
        }
        logger.info("subject={},stem={}",subject,stem);
        logger.info("pointId={},startTime={}",pointId,startTime);
        logger.info("endTime={},flag={}",endTime,flag);
        logger.info("uid={},status={}",uid,status);
        List<Question> questions = Lists.newArrayList();
        List<QuestionExtend> questionExtends = Lists.newArrayList();
        List<Integer> questionIds = Lists.newArrayList();
        questions = questionScatteredDao.findByDetail(subject,stem,pointId,startTime,endTime,uid,status,questionIds);
        if (CollectionUtils.isNotEmpty(questions)) {
            for (Question question : questions) {
                questionIds.add(question.getId());
            }
        }
        questionExtends = questionScatteredDao.findExtendByIds(questionIds,module);
        return toQuestionMins(questions,questionExtends);
    }

    public Object toQuestionMins(List<Question> questions,List<QuestionExtend> questionExtends){
        List<QuestionMin> questionMins = new ArrayList<>();
        logger.info("散题扩展表数量={}",questionExtends.size());
        Map<Integer, QuestionExtend> questionExtendMap = new HashMap<>();
        if(questionExtends!=null&&questionExtends.size()!=0){
            questionExtendMap = questionExtends.stream().collect(Collectors.toMap(QuestionExtend::getQid, (questionExtend) -> questionExtend));
        }
        for(Question question:questions){
            String stem = "";
            int difficult = 0;//主观题没有难度，设置为0
            if (question instanceof GenericQuestion) {
                stem = ((GenericQuestion) question).getStem();
                difficult = ((GenericQuestion) question).getDifficult();
            } else if (question instanceof CompositeQuestion) {
                stem = question.getMaterial();
            } else if (question instanceof GenericSubjectiveQuestion) {
                stem = ((GenericSubjectiveQuestion) question).getStem();
            } else if (question instanceof CompositeSubjectiveQuestion) {
                stem = ((CompositeSubjectiveQuestion) question).getMaterials().get(0);
            }
            stem = (String) questionService.preTreat(stem);
            String areaName = AreaConstants.getFullAreaNmae(question.getArea());
            String format =  "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat sf = new SimpleDateFormat(format);
            String creatTime = sf.format(new Date(question.getCreateTime()));
            int moduleId = 0;
            if(!questionExtendMap.isEmpty()&&questionExtendMap.containsKey(question.getId())){//若该试题有extend表
                moduleId = questionExtendMap.get(question.getId()).getModuleId();
                QuestionMin questionMin = QuestionMin.builder()
                        .stem(stem)
                        .difficult(difficult)
                        .id(question.getId())
                        .area(areaName)
                        .status(question.getStatus())
                        .mode(question.getMode())
                        .type(question.getType())
                        .createTime(creatTime)
                        .moduleId(moduleId)
                        .build();
                questionMins.add(questionMin);
            }
        }
        return questionMins;
    }


    public void release(int id) throws IllegalQuestionException {
        questionScatteredDao.editStatus(id,2);
        Question question = questionScatteredDao.findById(id);
//        questionDubboService.update(question);//Dubbo中更新试题
        questionService.updateQuestion(question,-1);
    }

    /**
     * 删除试题
     * @param id
     * @param status
     */
    public void del(int questionId,int status,String account,int id) throws BizException, IOException, BadElementException {
        if(status == QuestionStatus.CREATED||status == QuestionStatus.AUDIT_REJECT){//直接删除试题，即将试题状态置为删除状态（4）
            questionScatteredDao.editStatus(questionId,4);
        }else{
            Map<String, Object> map = Maps.newHashMap();
            Question question = questionService.findQuestinbyId(questionId);
            List<Integer> ids = new ArrayList<>();
            ids.add(questionId);
            QuestionExtend questionExtend = questionScatteredDao.findExtendByIds(ids,-1).get(0);
            map.put("questionId",questionId);
            map.put("type",question.getType());
            map.put("module",questionExtend.getModuleId());
            map.put("subject",question.getSubject());
            map.put("paperId",-1);
            map.put("subSign",questionId);
            questionService.editApplyQuestion(JsonUtil.toJson(map),account,id);
        }
    }

    public void review(int id,String description,int type){
        if(type==1){//是拒绝该散题通过
            questionScatteredDao.insertRefuseInfo(id,description);
            questionScatteredDao.editStatus(id,3);
        }else{//该散题通过
            questionScatteredDao.editStatus(id,5);
            questionScatteredDao.deleteRefuseInfo(id);//删除以前的拒绝信息
        }
    }

    public Object findRefuseInfo(int id){
        return questionScatteredDao.findRefuseInfo(id);
    }
}
