package com.huatu.ztk.question.controller;

import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionRabbitMqKeys;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2019/1/6
 */
@RestController
@RequestMapping(value = "/_util", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class QuestionUtilController {

    private final static Logger logger = LoggerFactory.getLogger(QuestionUtilController.class);

    @Autowired
    private QuestionDubboService questionDubboService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RequestMapping(value = "/{ids}", method = RequestMethod.PUT)
    public Object deleteCache(@PathVariable("ids") String ids, @RequestParam(defaultValue = "1") int deleted) {
        if (StringUtils.isBlank(ids)) {
            return SuccessMessage.create("操作成功");
        }
        List<Integer> idList = Arrays.asList(ids.split(",")).stream()
                .map(Integer::valueOf)
                .collect(Collectors.toList());
        List<Question> questionList = questionDubboService.findBath(idList);
        //查询出的试题信息
        logger.info("questionList info = {}", questionList);
        if (deleted == 1) {
            //idList.forEach(QuestionCache::remove);
            idList.forEach(questionId -> {
                Map<String, Integer> data = new HashMap<>();
                data.put("qid", questionId);
                rabbitTemplate.convertAndSend(QuestionRabbitMqKeys.QUESTION_UPDATE_EXCHANGE, "", data);
            });
        }
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HashMap<String, List<Question>> hashMap = new HashMap<>();
        hashMap.put("oldData", questionList);
        List<Question> questions = questionDubboService.findBath(idList);
        hashMap.put("newData", questions);

        return hashMap;
    }
}
