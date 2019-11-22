package com.huatu.ztk.knowledge.controller;

import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.knowledge.api.SubjectDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.service.QuestionErrorService;
import com.huatu.ztk.user.service.UserSessionService;
import com.huatu.ztk.user.util.UserTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-06-15 16:27
 */

@RestController
@RequestMapping(value = "/v1/errors",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class QuestionErrorController {
    private static final Logger logger = LoggerFactory.getLogger(QuestionErrorController.class);

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private QuestionErrorService questionErrorService;

    @Autowired
    private SubjectDubboService subjectDubboService;

    /**
     * 错题列表试题的最大数量
     */
    private static final int MAX_LIST_COUNT = 150;

    /**
     * 查看错题列表
     * @param token
     * @param pointId 知识点
     * @return
     */
    @RequestMapping(value = "/",method = RequestMethod.GET)
    public Object gets(@RequestHeader(required = false) String token,
                       @RequestParam int pointId) throws BizException {
        userSessionService.assertSession(token);
        //取得用户ID
        long userId = userSessionService.getUid(token);
        PageBean<Integer> pageBean = questionErrorService.findByPoint(pointId, userId, MAX_LIST_COUNT - 1);
        return pageBean;
    }

    /**
     * 查询错题列表知识点树
     * @param token
     * @return
     */
    @RequestMapping(value = "/trees",method = RequestMethod.GET)
    public Object pointTrees(@RequestHeader(required = false) String token,
                             @RequestParam(defaultValue = "-1") int subject) throws BizException{
        userSessionService.assertSession(token);
        //取得用户ID
        long userId = userSessionService.getUid(token);

        if (subject < 0) { //没有传科目id，从session取科目id
            subject= UserTokenUtil.getHeaderSubject(token, userSessionService::getSubject, subject);
        }

        int newSubject = subjectDubboService.getBankSubject(subject);
        List<QuestionPointTree> questionPointTrees = questionErrorService.queryErrorPointTrees(userId,newSubject);
        return questionPointTrees;
    }

    /**
     * 用户删除指定的错题
     * @param qid 试题id
     * @return
     */
    @RequestMapping(value = "/questions/{qid}",method = RequestMethod.DELETE)
    public Object deleteErrorQuestion(@PathVariable int qid,@RequestHeader(required = false) String token) throws BizException{
        userSessionService.assertSession(token);
        //取得用户ID
        long userId = userSessionService.getUid(token);
        int subject = userSessionService.getSubject(token);
        questionErrorService.deleteErrorQuestion(qid,userId,subject);


        return SuccessMessage.create("删除错题成功");
    }
}
