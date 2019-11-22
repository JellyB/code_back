package com.huatu.tiku.teacher.listener;

import com.google.common.collect.Lists;
import com.huatu.tiku.teacher.controller.util.InnerController;
import com.huatu.tiku.teacher.dao.mongo.OldPaperDao;
import com.huatu.tiku.teacher.dao.mongo.OldQuestionDao;
import com.huatu.tiku.teacher.service.question.SyncQuestionService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.GenericSubjectiveQuestion;
import com.huatu.ztk.question.bean.Question;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/9/5.
 */
@Slf4j
@Component
//@RabbitListener(queues = "sync_question_by_paper_queue")
public class SyncQuestionByPaperListener {
    @Autowired
    OldPaperDao oldPaperDao;
    @Autowired
    OldQuestionDao oldQuestionDao;
    @Autowired
    SyncQuestionService syncQuestionService;
    @Autowired
    InnerController innerController;
    @Autowired
    CommonQuestionServiceV1 commonQuestionServiceV1;
    @RabbitHandler
    public void onMessage(Map message) {
        try {
            log.info("message={}",message);
            Integer id = Integer.parseInt(String.valueOf(message.get("id")));
            innerController.deleteCompositeByPaperId(new Long(id));

            Paper paper = oldPaperDao.findById(id);
            if(null == paper){
                return;
            }
            List<Integer> questions = paper.getQuestions();
            if(CollectionUtils.isEmpty(questions)){
                return;
            }
            List<Question> questionList = oldQuestionDao.findByIds(questions);
            if(CollectionUtils.isEmpty(questionList)){
                return;
            }
            List<Integer> parents = Lists.newArrayList();
            List<Integer> children = Lists.newArrayList();
            for (Question question : questionList) {
                if(question instanceof GenericQuestion){
                    int parent = ((GenericQuestion) question).getParent();
                    if(parent>0){
                        parents.add(parent);
                        children.add(question.getId());
                    }
                }
                if(question instanceof GenericSubjectiveQuestion){
                    int parent = ((GenericSubjectiveQuestion) question).getParent();
                    if(parent>0){
                        parents.add(parent);
                        children.add(question.getId());
                    }
                }
            }
            parents = parents.stream().distinct().collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(parents)){
                for (Integer parent : parents) {
                    commonQuestionServiceV1.deleteQuestionPhysical(parent);
                    syncQuestionService.syncQuestion(parent);
                }
            }
            for(Integer child:children){
                commonQuestionServiceV1.deleteQuestionPhysical(child);
                syncQuestionService.syncQuestion(child);
            }

        } catch (Exception e) {
            log.error("消息消费异常。。。");
            e.printStackTrace();
        }
    }
}
