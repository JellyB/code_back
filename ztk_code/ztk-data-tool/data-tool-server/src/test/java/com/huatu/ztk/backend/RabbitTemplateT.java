package com.huatu.ztk.backend;

import com.google.common.collect.Maps;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.question.dao.QuestionDao;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionRabbitMqKeys;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangqp on 2018\7\2 0002.
 */
public class RabbitTemplateT extends BaseTestW {
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    PaperDao paperDao;
    @Autowired
    QuestionDao questionDao;
    @Test
    public void test(){
        Map map = Maps.newHashMap();
        map.put("id",2001203);
        rabbitTemplate.convertAndSend("","sync_question_2_db",map);
    }

    @Test
    public void testPaper(){
        Map map = Maps.newHashMap();
        map.put("id",817);
        rabbitTemplate.convertAndSend("","sync_paper_2_db",map);
    }
    /**
     * 通过试卷id同步所属的所有试题
     */
    @Test
    public void testQuestionsByPaperId(){
        Paper paper = paperDao.findById(817);
        List<Integer> questions = paper.getQuestions();
        for (Integer question : questions) {
            Map map = Maps.newHashMap();
            map.put("id",question);
            rabbitTemplate.convertAndSend("","sync_question_2_db",map);
        }
    }

    @Test
    public void clearCache(){
        int index = 0;
        while (true) {
            List<Question> questions = questionDao.findByIdGtAndLimit(index, 1000);
            if (CollectionUtils.isEmpty(questions)) {
                break;
            }
            for (Question question : questions) {
//                if(question.getStatus()== QuestionStatus.DELETED){
                    Map<String, Integer> data = new HashMap<>();
                    data.put("qid", question.getId());
                    rabbitTemplate.convertAndSend(QuestionRabbitMqKeys.QUESTION_UPDATE_EXCHANGE, "", data);
//                }
            }
            index = questions.stream().map(Question::getId).max(Integer::compareTo).get();
            System.out.println("*****************" + index + "***************");
        }

    }


}

