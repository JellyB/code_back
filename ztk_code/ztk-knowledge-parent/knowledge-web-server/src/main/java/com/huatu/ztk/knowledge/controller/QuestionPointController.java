package com.huatu.ztk.knowledge.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.api.SubjectDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.constant.CustomizeEnum;
import com.huatu.ztk.knowledge.service.QuestionPointService;
import com.huatu.ztk.user.service.UserSessionService;
import com.huatu.ztk.user.util.UserTokenUtil;

//import com.huatu.ztk.knowledge.task.UserPointTask;


/**
 * Created by shaojieyue
 * Created time 2016-05-06 18:18
 */
@RestController
@RequestMapping(value = "/v1/points", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class QuestionPointController {
    private static final Logger logger = LoggerFactory.getLogger(QuestionPointController.class);

    @Autowired
    private QuestionPointService questionPointService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private SubjectDubboService subjectDubboService;

   /* @Autowired
    private UserPointTask userPointTask;*/

    /**
     * 获取用户自身的知识点数
     *
     * @param token
     * @return
     */
	@RequestMapping(value = "", method = RequestMethod.GET)
	public Object collections(@RequestHeader(required = false) String token,
			@RequestParam(required = false, defaultValue = "0") int type, @RequestParam(defaultValue = "1") int flag)
			throws BizException {

		// long start = System.currentTimeMillis();
		// logger.info("zhouwei20181634" + start);
		userSessionService.assertSession(token);
		// 取得用户ID
		long userId = userSessionService.getUid(token);
		// 此处是否要给定默认值,session无效时
		int subject = userSessionService.getSubject(token);
		logger.info("知识树科目是:{}", subject);
		// logger.info("zhouwei20181611" + (System.currentTimeMillis() - start));

		int newSubject = subjectDubboService.getBankSubject(subject);
		CustomizeEnum.ModeEnum modeEnum = CustomizeEnum.ModeEnum.create(flag);
		List<QuestionPointTree> points = questionPointService.questionPointTree(userId, newSubject, modeEnum);

		// logger.info("zhouwei20181612" + (System.currentTimeMillis() - start));
		return points;
	}

    /**
     * 根据知识点ID 分层获取
     * update  2019年9月19日 新增游客模式
     *
     * @param parentId 父类ID
     */
    @RequestMapping(value = "collectionsByNode", method = RequestMethod.GET)
    public Object collectionsByNode(@RequestHeader(required = false) String token,
                                    @RequestParam(defaultValue = "0") int parentId,
                                    @RequestParam(defaultValue = "1") int flag,
                                    @RequestHeader int subject) throws BizException {

        if (StringUtils.isNotEmpty(token)) {
            userSessionService.assertSession(token);
            long userId = userSessionService.getUid(token);
            int headerSubject = UserTokenUtil.getHeaderSubject(token, userSessionService::getSubject, subject);
            int newSubject = subjectDubboService.getBankSubject(headerSubject);
            CustomizeEnum.ModeEnum modeEnum = CustomizeEnum.ModeEnum.create(flag);
            return questionPointService.questionPointTreeNode(userId, newSubject, parentId, modeEnum);
        } else {
            return questionPointService.getVisitorModeQuestionPointTreeNode(
                    subjectDubboService.getBankSubject(subject),
                    parentId);
        }
    }


    @RequestMapping(value = "/{subject}", method = RequestMethod.GET)
    public Object getPointTrees(@PathVariable int subject,
                                HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
        return questionPointService.getQuestionPoints(subject);
    }

   /* @RequestMapping(value = "/update/usr_ponit_question", method = RequestMethod.GET)
    public Object bathUpdateUserPoint(@RequestParam int subject,
                                      @RequestParam int userId,
                                      HttpServletRequest httpServletRequest) throws BizException {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
        long start = System.currentTimeMillis();
        userPointTask.getUserPointChangeCache(subject, 0, 100000);
        long start0 = System.currentTimeMillis();
        logger.info("1.0 用时={}", start0 - start);
        userPointTask.bathUpdateUserPoint(userId, subject);
        long end = System.currentTimeMillis();
        logger.info("usr_ponit_question 用时={}", (end - start0));
        return null;
    }

    @RequestMapping(value = "/init/user_point_fill", method = RequestMethod.GET)
    public Object initUserPoint(@RequestParam int subject,
                                @RequestParam int userId,
                                HttpServletRequest httpServletRequest) throws BizException {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
        long start = System.currentTimeMillis();
        userPointTask.fillPointQuestion(userId, subject);
        long end = System.currentTimeMillis();
        logger.info("user_point_fill 用时={}", (end - start));
        return null;
    }*/
}
