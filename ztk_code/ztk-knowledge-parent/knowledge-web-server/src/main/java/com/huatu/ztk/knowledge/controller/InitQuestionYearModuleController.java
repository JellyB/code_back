package com.huatu.ztk.knowledge.controller;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.service.InitService;
import com.huatu.ztk.paper.bean.PracticePaper;
import com.huatu.ztk.user.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-09-13  18:15 .
 */
@RestController
@RequestMapping(value = "/v1/init",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class InitQuestionYearModuleController {
    public static final Logger logger = LoggerFactory.getLogger(InitQuestionYearModuleController.class);

    @Autowired
    private InitService initService;

    @Autowired
    private UserSessionService userSessionService;

    @RequestMapping(value = "/init",method = RequestMethod.POST)
    public void save(HttpServletRequest httpServletRequest) throws BizException {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        initService.questionToRedis();
    }

    @RequestMapping(value = "/deleteQuestionFromRedis",method = RequestMethod.PUT)
    public void deleteQuestionFromRedis(@RequestParam int qid,HttpServletRequest httpServletRequest) throws BizException {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        initService.deleteQuestionFromRedis(qid);
    }

    @RequestMapping(value = "/addPaperInnerQuestionToRedis",method = RequestMethod.PUT)
    public void addPaperInnerQuestionToRedis(@RequestParam List<Integer> pids,HttpServletRequest httpServletRequest) throws BizException {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        logger.info("表示已经进来了----------------------------------------------------------------------------------------------------------------------------");
        initService.redisNum();
        logger.info("分界线---------------------------------------------------------------------------------------------------------标记未加人新题之前");
        logger.info("传输进来的pids={}",pids);
        initService.addPaperInnerQuestionsToRedis(pids);
        initService.redisNum();
        logger.info("分界线---------------------------------------------------------------------------------------------------------标记未加人新题之后");
    }

    @RequestMapping(value = "/findRedisNum",method = RequestMethod.PUT)
    public Object findRedisNum() throws BizException {
        return initService.redisNum1();
    }

    /**
     * 智能出题
     *
     * @return
     */
    @RequestMapping(value = "smarts", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object smarts(@RequestParam(defaultValue = "10") int size,
                         @RequestHeader int terminal,
                         @RequestHeader(required = false) String token) throws  BizException {
        userSessionService.assertSession(token);

        //用户id
        long userId = userSessionService.getUid(token);
        int subject = userSessionService.getSubject(token);
        long startTime = System.currentTimeMillis();
        PracticePaper practicePaper = initService.createSmartPaper(size, userId, subject);
        logger.info("总用时={}",System.currentTimeMillis()-startTime);
        return practicePaper;
    }
}
