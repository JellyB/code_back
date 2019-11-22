package com.huatu.ztk.knowledge.controller;

import com.huatu.ztk.knowledge.api.SubjectDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.constant.CustomizeEnum;
import com.huatu.ztk.knowledge.service.QuestionPointService;
import com.huatu.ztk.user.service.UserSessionService;
import com.huatu.ztk.user.util.UserTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * Created by zhaoxi
 * Created time 2016-05-06 18:18
 */
@RestController
@Slf4j
@RequestMapping(value = "/v2/points", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class QuestionPointControllerV2 {

    @Autowired
    private QuestionPointService questionPointService;
    @Autowired
    private UserSessionService userSessionService;
    @Autowired
    private SubjectDubboService subjectDubboService;

    /**
     * 根据考试类型返回科目树（PHP蓝色后台专用）
     *
     * @param category
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "/{category}", method = RequestMethod.GET)
    public Object getPointTrees(@PathVariable int category,
                                HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        log.info("tool interface:----{}-------host:---{}", url, remoteAddr);
        return questionPointService.getQuestionTreeV2(category);
    }


    @RequestMapping(value = "category/{category}", method = RequestMethod.GET)
    public Object getPointTreesByCategory(@PathVariable int category,
                                          HttpServletRequest httpServletRequest) {
        return questionPointService.getPointTreesByCategory(category);
    }


    /**
     * 专项练习首页,查询知识点树（走CDN,用户信息不查询）
     * 根据知识点ID 分层获取
     *
     * @param parentId 父类ID
     */
    @RequestMapping(value = "collectionsByNode", method = RequestMethod.GET)
    public Object collectionsByNode(@RequestHeader String token,
                                    @RequestParam int subject,
                                    @RequestParam(defaultValue = "0") int parentId) {
        int headerSubject = UserTokenUtil.getHeaderSubject(token, userSessionService::getSubject, subject);
        return questionPointService.questionPointTreeNode(subjectDubboService.getBankSubject(headerSubject), parentId);
    }


    /**
     * 专项练习首页,查询用户知识树
     *
     * @param token
     * @param pointIds
     * @return
     */
	@RequestMapping(value = "userCollectionByNode", method = RequestMethod.GET)
	public Object userCollectionByNode(@RequestHeader String token, @RequestParam(defaultValue = "0") String pointIds,
			@RequestParam(defaultValue = "1") int flag) {
		int subject = userSessionService.getSubject(token);
		long userId = userSessionService.getUid(token);
		CustomizeEnum.ModeEnum modeEnum = CustomizeEnum.ModeEnum.create(flag);
		List<QuestionPointTree> userQuestionPointTree = questionPointService.getUserQuestionPointTree(pointIds, userId,
				subjectDubboService.getBankSubject(subject), modeEnum);
		return userQuestionPointTree;
	}


    /**
     * 根据知识点ID 分层获取
     *
     * @param parentId 父类ID
     */
    @RequestMapping(value = "collectionsByNodeWithVisitorMode", method = RequestMethod.GET)
    public Object collectionsByNodeWithVisitorMode(@RequestHeader(required = false) String token,
                                                   @RequestParam(defaultValue = "0") int parentId,
                                                   @RequestHeader(defaultValue = "-1") int subject) {
        return null;
    }


}
