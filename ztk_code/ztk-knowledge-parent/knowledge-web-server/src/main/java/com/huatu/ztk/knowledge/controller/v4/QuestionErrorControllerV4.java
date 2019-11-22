package com.huatu.ztk.knowledge.controller.v4;

import com.google.common.collect.Maps;
import com.huatu.tiku.entity.download.QuestionErrorDownloadTask;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.knowledge.api.SubjectDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.bean.SubjectTree;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.knowledge.service.QuestionErrorService;
import com.huatu.ztk.knowledge.service.QuestionPointService;
import com.huatu.ztk.knowledge.service.v1.impl.QuestionErrorServiceImplV1;
import com.huatu.ztk.user.service.UserSessionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/11/21.
 */
@RestController
@Slf4j
@RequestMapping(value = "/v4/errors",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class QuestionErrorControllerV4 {

    @Autowired
    UserSessionService userSessionService;

    @Autowired
    SubjectDubboService subjectDubboService;

    @Autowired
    QuestionErrorService questionErrorService;

    @Autowired
    QuestionPointService questionPointService;


    /**
     * 查询错题树结构
     *
     * @param token
     * @param terminal
     * @param subject
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/trees", method = RequestMethod.GET)
    public Object pointTrees(@RequestHeader(required = false) String token,
                             @RequestHeader int terminal,
                             @RequestParam(defaultValue = "-1") int subject,
                             HttpServletRequest httpServletRequest) throws BizException {
        log.info("token={},terminal={},subject={},url={}", token, terminal, subject, httpServletRequest.getRequestURL().toString());
        userSessionService.assertSession(token);
        //取得用户ID
        long userId = userSessionService.getUid(token);
        if (subject < 0) { //没有传科目id，从session取科目id
            subject = userSessionService.getSubject(token);
        }
        int newSubject = subjectDubboService.getBankSubject(subject);
        List<QuestionPointTree> questionPointTrees = questionErrorService.queryErrorPointTrees(userId, newSubject);
        if (CollectionUtils.isNotEmpty(questionPointTrees)) {
            System.out.println("questionPointTrees.get(0).getAccuracy().before = " + questionPointTrees.get(0).getAccuracy());
            questionPointService.handleAccuracy(questionPointTrees, userId);
            System.out.println("questionPointTrees.get(0).getAccuracy().after = " + questionPointTrees.get(0).getAccuracy());
        }
        HashMap<Object, Object> map = Maps.newHashMap();
        int total = 0;
        for (QuestionPointTree questionPointTree : questionPointTrees) {
            total += questionPointTree.getWnum();
        }
        map.put("tree", questionPointTrees);
        map.put("total", total);
        return map;
    }


    /**
     * 用户删除指定的错题
     *
     * @param qid 试题id
     * @return
     */
    @RequestMapping(value = "/questions/{qid}", method = RequestMethod.DELETE)
    public Object deleteErrorQuestion(@PathVariable int qid,
                                      @RequestHeader(required = false) String token,
                                      @RequestHeader int terminal,
                                      HttpServletRequest httpServletRequest) throws BizException {
        log.info("token={},qid={}，terminal={},url={}", token, qid, terminal, httpServletRequest.getRequestURL().toString());
        userSessionService.assertSession(token);
        //取得用户ID
        long userId = userSessionService.getUid(token);
        int subject = userSessionService.getSubject(token);
        questionErrorService.deleteErrorQuestion(qid, userId, subject);
        return SuccessMessage.create("删除错题成功");
    }

    /**
     * 清空错题本
     *
     * @return
     */
    @RequestMapping(value = "/clear", method = RequestMethod.DELETE)
    public Object clearErrorQuestion(@RequestHeader(required = false) String token,
                                     @RequestHeader int terminal,
                                     @RequestParam(defaultValue = "-1") int subject,
                                     HttpServletRequest httpServletRequest) throws BizException {
        log.info("token={},terminal={},subject={},url={}", token, terminal, subject, httpServletRequest.getRequestURL().toString());
        userSessionService.assertSession(token);
        //取得用户ID
        long userId = userSessionService.getUid(token);
        if (subject < 0) {
            subject = userSessionService.getSubject(token);
        }
        questionErrorService.clearErrorQuestion(userId, subject);

        return SuccessMessage.create("错题本清空成功");
    }

    @RequestMapping(value = "down/description", method = RequestMethod.GET)
    public Object createPreDownloadSimple(){
        return questionErrorService.getDownDescription();
    }
    /**
     * 预下载逻辑结果
     *
     * @return
     */
    @RequestMapping(value = "down/pre", method = RequestMethod.POST)
    public Object createPreDownloadInfo(@RequestHeader String token,
                                        @RequestHeader int terminal,
                                        @RequestHeader String cv,
                                        @RequestParam(defaultValue = "-1") int num,
                                        @RequestParam(defaultValue = "-1") String pointIds) throws BizException {
        log.info("createPreDownloadInfo method:{},{},{},{}", token, terminal, cv, pointIds);
        userSessionService.assertSession(token);
        long uid = userSessionService.getUid(token);
        int subject = userSessionService.getNewSubject(token);
        return questionErrorService.createPreDownloadInfo(uid, pointIds, subject,num);
    }

    /**
     * 创建错题导出任务，获取需要导出的试题ID,创建答题卡，并返回导出任务的信息（名称，试题信息，答题卡ID等）
     *
     * @param token
     * @param terminal
     * @param cv
     * @param pointIds
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "down/task", method = RequestMethod.POST)
    public Object createDownloadTask(@RequestHeader String token,
                                     @RequestHeader int terminal,
                                     @RequestHeader String cv,
                                     @RequestParam String pointIds) throws BizException {
        log.info("createDownloadTask method:{},{},{},{}", token, terminal, cv, pointIds);
        userSessionService.assertSession(token);
        long uid = userSessionService.getUid(token);
        int realSubject = userSessionService.getSubject(token);
        String userName = userSessionService.getUname(token);
        List<SubjectTree> subjectTree = subjectDubboService.getSubjectTree();
        Optional<SubjectTree> any = subjectTree.stream().flatMap(i -> i.getChildrens().stream().map(a -> {
            a.setName(i.getName() + a.getName());
            return a;
        })).filter(i -> i.getId() == realSubject).findAny();        //获取用户的考试类型科目信息
        if (!any.isPresent()) {
            throw new BizException(ErrorResult.create(10022212,"科目信息有误"));
        }
        int newSubject = userSessionService.getNewSubject(token);
        return questionErrorService.createDownloadTask(uid,userName, pointIds, newSubject,any.get());

    }


    /**
     * 下载任务列表
     * @param token
     * @param terminal
     * @param cv
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "down/list",method = RequestMethod.GET)
    public Object downList(@RequestHeader String token,
                           @RequestHeader int terminal,
                           @RequestHeader String cv,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "20") int size) throws BizException {
        userSessionService.assertSession(token);
        long uid = userSessionService.getUid(token);
        int subject = userSessionService.getSubject(token);
        return questionErrorService.findDownList(uid,subject,page,size);

    }

    @RequestMapping(value = "down/info",method = RequestMethod.GET)
    public Object downInfo(@RequestHeader String token,
                           @RequestParam Long taskId) throws BizException {
        userSessionService.assertSession(token);
        return questionErrorService.findDownInfo(taskId);

    }


    @RequestMapping(value = "down/order/list",method = RequestMethod.GET)
    public Object getOrderDetail(@RequestHeader String token,
                                 @RequestHeader int terminal,
                                 @RequestHeader String cv,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "20") int size) throws BizException {
        userSessionService.assertSession(token);
        long uid = userSessionService.getUid(token);
        return questionErrorService.findOrderByUserId(uid,page,size);
    }


    @RequestMapping(value = "down/remove",method = RequestMethod.DELETE)
    public Object deleteDownTask(@RequestHeader String token,
                                 @RequestHeader int terminal,
                                 @RequestHeader String cv,
                                 @RequestParam(defaultValue = "") String taskIds) throws BizException {
        userSessionService.assertSession(token);
        long uid = userSessionService.getUid(token);
        List<Long> collect = Arrays.stream(taskIds.split(",")).filter(NumberUtils::isDigits).map(Long::parseLong).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(collect)){
            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }
        return questionErrorService.deleteDownTask(uid,collect);
    }


    @RequestMapping(value = "reset/{userId}")
    public Object restTree(@PathVariable long userId){
        questionErrorService.restCheckLock(userId);
        return SuccessMessage.create("重置错题本成功");
    }
}
