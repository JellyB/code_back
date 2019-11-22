package com.huatu.ztk.paper.controller;

import com.alibaba.fastjson.JSONObject;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.Answer;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.service.PaperAnswerCardService;
import com.huatu.ztk.user.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-03-07 6:25 PM
 **/

@CrossOrigin
@RestController
@RequestMapping(value = "/v1/courseWork", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class CourseExerciseController {
    private static Logger logger = LoggerFactory.getLogger(CourseExerciseController.class);

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private PaperAnswerCardService paperAnswerCardService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 提交答题卡（专项练习，错题练习，每日特训，智能出题，真题试卷）
     *
     * @param practiceId
     * @param answers
     * @return
     * @throws BizException
     */
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @RequestMapping(value = "/{practiceId}", method = RequestMethod.PUT, consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object submitPractice(@RequestHeader(required = false) String token,
                                 @PathVariable long practiceId,
                                 @RequestBody List<Answer> answers,
                                 @RequestParam(defaultValue = "-1") int time,
                                 @RequestHeader int terminal,
                                 @RequestHeader(value = "cv",defaultValue = "1.0") String cv) throws BizException {
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        final int area = userSessionService.getArea(token);
        final AnswerCard answerCard = paperAnswerCardService.submitPractice(practiceId, userId, answers, area, terminal, cv);
        logger.info("课后作业 - 答题卡信息:答题卡{},用户id:{},提交答案:{}", practiceId, userId, JSONObject.toJSONString(answers));
        rabbitTemplate.convertAndSend("", "course_work_submit_card_info", JSONObject.toJSONString(answerCard));
        return answerCard;
    }
}
