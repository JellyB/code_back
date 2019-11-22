package com.huatu.ztk.knowledge.controller;

import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.knowledge.api.SubjectDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.common.analysis.event.AnalysisEvent;
import com.huatu.ztk.knowledge.common.analysis.model.EventEntity;
import com.huatu.ztk.knowledge.common.analysis.model.EventType;
import com.huatu.ztk.knowledge.common.analysis.publisher.SpringContextPublisher;
import com.huatu.ztk.knowledge.service.QuestionCollectService;
import com.huatu.ztk.user.service.UserSessionService;
import com.huatu.ztk.user.util.UserTokenUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 问题收藏控制器
 */
@RestController
@RequestMapping(value = "/v1/collects", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class QuestionCollectController {
    public static final Logger logger = LoggerFactory.getLogger(QuestionCollectController.class);

    @Autowired
    private QuestionCollectService questionCollectService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private SubjectDubboService subjectDubboService;
    @Autowired
    private SpringContextPublisher springContextPublisher;

    /**
     * 收藏试题
     *
     * @param questionId
     * @param token
     */
    @RequestMapping(value = "/{questionId}", method = RequestMethod.POST)
    public Object save(@PathVariable("questionId") int questionId, @RequestHeader(required = false) String token, @RequestHeader(required = false) int terminal) throws BizException {
        long stime = System.currentTimeMillis();
        StopWatch stopWatch = new StopWatch("收藏接口耗时");
        stopWatch.start("角色校验");
        userSessionService.assertSession(token);
        stopWatch.stop();
        stopWatch.start("获取用户信息");
        //取得用户ID
        long userId = userSessionService.getUid(token);
        int subject = userSessionService.getSubject(token);
        logger.info("collect userSessionService time={}", System.currentTimeMillis() - stime);
        long stime1 = System.currentTimeMillis();
        //成功更新用户收藏
        stopWatch.stop();
        stopWatch.start("收藏实现");
        questionCollectService.collect(questionId, userId, subject);
        stopWatch.stop();
        stopWatch.start("收藏埋点");
        logger.info("collect questionCollectService time={}", System.currentTimeMillis() - stime1);
        {
            //数据上报
            EventEntity.putProperties(EventType.Default.platform, EventType.TerminalType.getTerminalName(terminal));
            EventEntity.newInstance(EventType.HuaTuOnline_app_pc_HuaTuOnline_CollectTest);
            EventEntity.getInstance().setDistinctId(userSessionService.getUcId(token));
            EventEntity.putProperties(EventType.CollectTest.collect_operation, "收藏");
            EventEntity.putProperties(EventType.CollectTest.test_id, questionId);
            springContextPublisher.pushEvent(new AnalysisEvent(EventEntity.getInstance()));
        }
        stopWatch.stop();
        logger.info("save 收藏接口耗时:{}",stopWatch.prettyPrint());
        return SuccessMessage.create("收藏题目成功");
    }

    /**
     * 取消收藏试题
     *
     * @param questionId
     * @param token
     * @return
     */
    @RequestMapping(value = "/{questionId}", method = RequestMethod.DELETE)
    public Object delete(@PathVariable("questionId") int questionId, @RequestHeader(required = false) String token, @RequestHeader(required = false) int terminal) throws BizException {
        userSessionService.assertSession(token);
        //取得用户ID
        long userId = userSessionService.getUid(token);
        int subject = userSessionService.getSubject(token);
        questionCollectService.cancel(questionId, userId, subject);
        {
            //数据上报
            EventEntity.putProperties(EventType.Default.platform, EventType.TerminalType.getTerminalName(terminal));
            EventEntity.newInstance(EventType.HuaTuOnline_app_pc_HuaTuOnline_CollectTest);
            EventEntity.getInstance().setDistinctId(userSessionService.getUcId(token));
            EventEntity.putProperties(EventType.CollectTest.collect_operation, "取消收藏");
            EventEntity.putProperties(EventType.CollectTest.test_id, questionId);
            springContextPublisher.pushEvent(new AnalysisEvent(EventEntity.getInstance()));
        }
        return SuccessMessage.create("取消收藏成功");
    }

    /**
     * 通过userId查询该用户的收藏问题列表
     *
     * @param token
     * @return
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public Object query(@RequestHeader(required = false) String token,
                        @RequestParam int pointId) throws BizException {
        userSessionService.assertSession(token);
        //取得用户ID
        long userId = userSessionService.getUid(token);
        PageBean<Integer> pageBean = questionCollectService.findByPoint(pointId, userId);
        return pageBean;
    }

    /**
     * 查询收藏列表知识点树
     *
     * @param token
     * @return
     */
    @RequestMapping(value = "/trees", method = RequestMethod.GET)
    public Object pointTrees(@RequestHeader(required = false) String token,
                             @RequestParam(defaultValue = "-1") int subject) throws BizException {
        userSessionService.assertSession(token);
        //取得用户ID
        long userId = userSessionService.getUid(token);

        if (subject < 0) {
            subject = userSessionService.getSubject(token);
        }

        int newSubject = subjectDubboService.getBankSubject(subject);

        List<QuestionPointTree> questionPointTrees = questionCollectService.findCollectPointTrees(userId, newSubject);
        return questionPointTrees;
    }

    @RequestMapping(value = "/collect/switch", method = RequestMethod.GET)
    public Object queryBath(@RequestParam(defaultValue = "on") String abc) {
        if ("lijun".equals(abc)) {
            redisTemplate.delete("_question_collect_switch");
        }
        redisTemplate.opsForValue().set("_question_collect_switch", abc);
        return SuccessMessage.create(redisTemplate.opsForValue().get("_question_collect_switch"));
    }

    /**
     * 用户根据试题id列表查询哪些试题被收藏过
     *
     * @param qids 试题列表
     * @return
     */
    @RequestMapping(value = "/batch", method = RequestMethod.GET)
    public Object queryBath(@RequestHeader(required = false) String token,
                            @RequestHeader(defaultValue = "-1") int terminal,
                            @RequestParam String qids) throws BizException {
        //没有传入qids则直接返回 加开关
        if (StringUtils.isBlank(qids) || "on".equals(redisTemplate.opsForValue().get("_question_collect_switch"))) {
            return new int[0];
        }

        userSessionService.assertSession(token);
        //取得用户ID
        long userId = userSessionService.getUid(token);
        int subject = userSessionService.getSubject(token);

        if (StringUtils.isBlank(qids)) {//没有传入qids则直接返回
            return new int[0];
        }

        Collection<String> collects = questionCollectService.findCollectQuestions(Arrays.asList(qids.split(",")), userId, subject);
        //转换为int数组
        return collects.stream().mapToInt(Integer::parseInt).toArray();
    }

    @RequestMapping(value = "reset",method = RequestMethod.POST)
    public Object resetUserCollection(@RequestHeader String token,
                                      @RequestHeader(defaultValue = "-1") int subject) throws BizException {
        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);
        int newSubject = UserTokenUtil.getHeaderSubject(token, userSessionService::getSubject, subject);
        questionCollectService.resetCollection(userId,newSubject);
        return SuccessMessage.create("重置成功");
    }
}
