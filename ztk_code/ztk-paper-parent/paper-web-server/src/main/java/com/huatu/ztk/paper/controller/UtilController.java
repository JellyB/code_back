package com.huatu.ztk.paper.controller;

import com.google.common.collect.Maps;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.paper.common.MatchRedisKeys;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import com.huatu.ztk.paper.common.RabbitMqConstants;
import com.huatu.ztk.user.service.UserSessionService;
import com.self.generator.core.WaitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.http.MediaType;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping(value = "/redis",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UtilController {
    private final static Logger logger = LoggerFactory.getLogger(UtilController.class);
    @Resource(name = "coreRedisTemplate")
    private ValueOperations valueOperations;
    @Autowired
    UserSessionService userSessionService;
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @RequestMapping(value =  "d1",method = RequestMethod.GET)
    public Object get(@RequestParam(defaultValue = "1") int subject,
                      HttpServletRequest httpServletRequest) throws WaitException, BizException {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        valueOperations.getOperations().delete(MatchRedisKeys.getMatchBySubjectRedisKey(subject));
        return SuccessMessage.create("牛逼的功能!");
    }

    @RequestMapping(value = "run", method = RequestMethod.GET)
    public Object runner(Integer type,@RequestParam(required = false,defaultValue = "102") Integer num,@RequestHeader(required = false) String token,HttpServletRequest httpServletRequest) throws WaitException, BizException {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:--zhouwei--{}-------host:---{}---{}",url,remoteAddr,System.currentTimeMillis());

        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        final int area = userSessionService.getArea(token);
        String uname = userSessionService.getUname(token);
        logger.info("--zhouwei-"+System.currentTimeMillis());
        if(type==null || type==1){
            for (int i = 100; i < num; i++) {
                SetOperations<String, String> opsForSet = redisTemplate.opsForSet();
                opsForSet.add("zhouwei", System.nanoTime() + "");

                HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
                String questionMetaKey = "zhouweihash";
                Map<String, String> metaMap = hashOperations.entries(questionMetaKey);
                hashOperations.put(questionMetaKey, i + "", System.nanoTime() + "");
                SetOperations<String, String> opsForSet1 = redisTemplate.opsForSet();
                opsForSet1.isMember("zhouwei", "135");
                opsForSet1.isMember("zhouwei", "35");
                if (opsForSet1.size("zhouwei") > 10000L) {
                    redisTemplate.delete("zhouwei");
                }
                HashOperations<String, String, String> hashOperations1 = redisTemplate.opsForHash();
                hashOperations1.entries(questionMetaKey);

            }

        }
        logger.info("--zhouwei2222-"+System.currentTimeMillis());

        return 200;
    }

    @RequestMapping(value =  "enroll/count",method = RequestMethod.GET)
    public Object getEnrollCount(@RequestParam(defaultValue = "4001482") int paperId){
        StopWatch stopWatch = new StopWatch("enrollCount");
        stopWatch.start();
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object o = valueOperations.get(MatchRedisKeys.getTotalEnrollCountKey(paperId));
        HashMap<Object, Object> map = Maps.newHashMap();
        map.put("count",o);
        stopWatch.stop();
        logger.info("enroll/count/{},耗时{}",paperId,stopWatch.getTotalTimeMillis());
        if(stopWatch.getTotalTimeMillis()>1000){
            logger.info("cone：{}",stopWatch.prettyPrint());
        }
        return map;
    }


    /**
     * 试卷算分规则改变，重新计算之前的答题卡的分数
     * @param paperId
     * @return
     */
    @RequestMapping(value = "recount", method = RequestMethod.POST)
    public Object reCountAnswerCard(@RequestParam(defaultValue = "-1") int paperId){
        Map data = new HashMap<>(4);
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();

        String paperPracticeIdSore = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        Set range = zSetOperations.range(paperPracticeIdSore, 0, -1);
        for (Object o : range) {
            data.put("id", o.toString());
            rabbitTemplate.convertAndSend(RabbitMqConstants.SUBMIT_PRACTICE_EXCHANGE, "", data);
            System.out.println("send message :" + JsonUtil.toJson(data));
        }
        return SuccessMessage.create("重新算分成功");
    }

}
