package com.huatu.ztk.knowledge.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.commons.Module;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.api.ModuleDubboService;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.api.SubjectDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.cacheTask.util.QuestionDataHandleState;
import com.huatu.ztk.knowledge.service.PoxyUtilService;
import com.huatu.ztk.knowledge.service.SubjectTreeService;
import com.huatu.ztk.knowledge.util.DebugCacheUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

/**
 * Created by linkang on 8/8/16.
 */
@RestController
public class ServerCheckController {

    @Autowired
    PoxyUtilService poxyUtilService;
    @Autowired
    ModuleDubboService moduleDubboService;
    @Autowired
    QuestionDataHandleState questionDataHandleState;
    @Autowired
    QuestionPointDubboService questionPointDubboService;
    @Autowired
    SubjectControllerV2 subjectControllerV2;
    @Autowired
    SubjectDubboService subjectDubboService;
    @Autowired
    SubjectTreeService subjectTreeService;

    private static final Logger logger = LoggerFactory.getLogger(ServerCheckController.class);
    /**
     * 空接口，检测服务器状态
     */
    @RequestMapping(value = "checkServer")
    public void check(HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
    }

    /**
     * 查询现有的guava缓存数据
     */
    @RequestMapping(value = "checkCache")
    public Object test(){
        HashMap<Object, Object> map = Maps.newHashMap();
        map.put("host",getServerIp());
        boolean flag = DebugCacheUtil.changeDebugFlag();
        map.put("debugFlag",flag);
        if(flag){
            DebugCacheUtil.doInt(()->poxyUtilService.getKnowledgeService().findById(392));
            DebugCacheUtil.doInt(()->moduleDubboService.findSubjectModules(1));
            DebugCacheUtil.doInt(()->questionDataHandleState.getQuestionInfoByPointId(392));
            DebugCacheUtil.doInt(()->questionPointDubboService.findPointsCount(1));
            DebugCacheUtil.doInt(()->poxyUtilService.getQuestionPointService(1).getAllQuestionPoints(1));
            DebugCacheUtil.doInt(()->poxyUtilService.getQuestionPointService(2).getAllQuestionPoints(1));
            DebugCacheUtil.doInt(()->poxyUtilService.getQuestionPointService(2).findById(392));
            DebugCacheUtil.doInt(()->poxyUtilService.getQuestionPointService(2).count(392));
            DebugCacheUtil.doInt(()->poxyUtilService.getQuestionPointService(2).countAll());
            DebugCacheUtil.doInt(()->poxyUtilService.getQuestionPointService(2).getQuestionIds(392));
            DebugCacheUtil.doInt(()->subjectControllerV2.staticWithCache(1));
            DebugCacheUtil.doInt(()->subjectDubboService.getCatgoryBySubject(1));
            DebugCacheUtil.doInt(()->subjectTreeService.findTree(Lists.newArrayList(1)));
            map.put("data",DebugCacheUtil.concurrentHashMap);
        }
        return map;
    }

    private static String getServerIp() {
        return System.getProperty("server_ip");
    }
}
