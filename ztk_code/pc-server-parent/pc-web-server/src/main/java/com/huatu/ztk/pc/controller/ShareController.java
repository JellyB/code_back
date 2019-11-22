package com.huatu.ztk.pc.controller;

import com.google.common.collect.Maps;
import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.spring.annotation.RequestToken;
import com.huatu.ztk.pc.bean.Share;
import com.huatu.ztk.pc.service.ShareService;
import com.huatu.ztk.pc.util.TemplateHashModelUtil;
import com.huatu.ztk.pc.util.WxChatShareUtil;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionType;
import com.huatu.ztk.user.service.UserSessionService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 分享控制层
 * Created by shaojieyue
 * Created time 2016-09-18 15:03
 */

@Controller
@RequestMapping(value = "/share/")
public class ShareController {
    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);

    @Autowired
    private ShareService shareService;

    @Autowired
    private QuestionDubboService questionDubboService;

    @Autowired
    private UserSessionService userSessionService;

    /**
     * 分享试题
     * @param qid 试题id
     * @return
     */
    @RequestMapping(value = "question/{qid}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object question(@PathVariable int qid){
        final Share share = shareService.shareQuestion(qid);
        return share;
    }

    /**
     * 查询分享试题
     * @param qid 试题id
     * @return
     */
    @RequestMapping(value = "question/{qid}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public Object queryQuestionShare(@PathVariable int qid,@RequestToken(required = false) String token, Model model) throws BizException {
        final Question question = questionDubboService.findById(qid);
        final Share share = shareService.shareQuestion(qid);
        int catgory=CatgoryType.GONG_WU_YUAN;
        if(StringUtils.isNotEmpty(token)){ //需个人信息
            userSessionService.assertSession(token);
            catgory=userSessionService.getCatgory(token);
        }
        Map<String,String> data = Maps.newHashMap();
        WxChatShareUtil.assertWeiXinInfo(data,share);
        logger.info("questionsId={}", qid);
        if(question instanceof GenericQuestion){
            String extend = ((GenericQuestion) question).getExtend();
            if(extend==null||StringUtils.isBlank(extend.replaceAll("<[^>]>","").trim())){
                ((GenericQuestion) question).setExtend(null);
            }
        }
        model.addAllAttributes(data);
        model.addAttribute("question",question);
        model.addAttribute("QuestionType", TemplateHashModelUtil.useStaticPackage("com.huatu.ztk.question.common.QuestionType"));
        model.addAttribute("QuestionUtil", TemplateHashModelUtil.useStaticPackage("com.huatu.ztk.question.util.QuestionUtil"));
        model.addAttribute("catgory",catgory);
        if(QuestionType.SINGLE_SUBJECTIVE==question.getType()||QuestionType.MULTI_SUBJECTIVE==question.getType()){ //主观题
          return "share/question_subject";
        }
        return "share/question";
    }

    /**
     * 分享练习
     * @param practiceId 练习id
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "practice", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object practice(@RequestParam long practiceId, @RequestToken String token) throws BizException {
        userSessionService.assertSession(token);
        final long uid = userSessionService.getUid(token);
        final int catgory=userSessionService.getCatgory(token);
        Share share = shareService.sharePractice(practiceId,uid,catgory);
        return share;
    }

    /**
     * 查看分享练习
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "practice/{id}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public Object querySharePractice(@PathVariable String id,Model model) throws BizException {
        Map data = shareService.findSharePracticeModel(id);
        if (data == null) {//分享未找到
            return ResponseEntity.notFound().build();
        }
        model.addAllAttributes(data);
        return "share/practice";
    }

    /**
     * 分享我的练习报告
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "myreport", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object report(@RequestToken String token) throws BizException {
        userSessionService.assertSession(token);
        final long uid = userSessionService.getUid(token);
        final int subject=userSessionService.getSubject(token);
        final int area=userSessionService.getArea(token);
        Share share = shareService.shareReport(uid,subject,area);
        return share;
    }

    /**
     * 分享我的练习报告
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "myreport/{id}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public Object queryReport(@PathVariable String id,Model model) throws BizException {
        Map data = shareService.findShareReportModel(id);
        if (data == null) {//分享我的练习报告
            return ResponseEntity.notFound().build();
        }
        model.addAllAttributes(data);
        return "share/report";
    }

    /**
     * 分享课程
     * @param token
     * @param courseId 课程id
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "course", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object course(@RequestToken String token,@RequestParam long courseId ) throws BizException {
        userSessionService.assertSession(token);
        Long uid=userSessionService.getUid(token);
        int catgory=userSessionService.getCatgory(token);
        Share share = shareService.shareCourse(courseId,uid,catgory);
        return share;
    }

    /**
     * 查看分享的课程
     * @param shareId
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "course/{shareId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object queryShareCourse(@PathVariable String shareId,Model model) throws BizException {
        Map data = shareService.findShareCourse(shareId);
        if (data == null) {//分享我的课程未找到
            return ResponseEntity.notFound().build();
        }
        model.addAllAttributes(data);
        return "share/cousrse";
    }

    /**
     * 分享竞技场战绩
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "arena/summary", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object arenaSummary(@RequestToken String token) throws BizException {
        userSessionService.assertSession(token);
        Long uid=userSessionService.getUid(token);
        Share share = shareService.shareArenaSummary(uid);
        return share;
    }

    /**
     * 查看分享的竞技练习的战绩
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "arena/summary/{id}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public Object queryArenaSummary(@PathVariable String id,Model model) throws BizException {
        Map data = shareService.findArenaSummary(id);
        if (data == null) {//分享竞技练习的战绩未找到
            return ResponseEntity.notFound().build();
        }
        model.addAllAttributes(data);
        return "share/arena_summary";
    }
    /**
     * 分享竞技场战绩
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "arena/htonline", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object arenaSummaryHtOnline(@RequestToken String token) throws BizException {
        userSessionService.assertSession(token);
        Long uid=userSessionService.getUid(token);
        Share share = shareService.shareArenaSummaryOnline(uid);
        return share;
    }

    /**
     * 查看分享的竞技练习的战绩
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "arena/htonline/{id}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public Object queryArenaSummaryHtOnline(@PathVariable String id,Model model) throws BizException {
        Map data = shareService.findArenaSummary(id);
        if (data == null) {//分享竞技练习的战绩未找到
            return ResponseEntity.notFound().build();
        }
        model.addAllAttributes(data);
        return "share/arena_summary_htOnline";
    }

    /**
     *分享竞技场成绩统计
     * @param token
     * @param id
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "arena/record", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object arenaRecord(@RequestToken String token,@RequestParam long id) throws BizException {
        userSessionService.assertSession(token);
        Long uid=userSessionService.getUid(token);
        Share share = shareService.shareArenaRecord(uid,id);
        return share;
    }

    /**
     * 查看分享的我的竞技成绩统计
     * @param id  分享id
     * @param model
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "arena/record/{id}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public Object queryArenaRecord(@PathVariable String id,Model model) throws BizException {
        Map data = shareService.findArenaRecord(id);
        if (data == null) {//分享竞技的成绩统计未找到
            return ResponseEntity.notFound().build();
        }
        model.addAllAttributes(data);
        if(Integer.parseInt(data.get("isWin").toString())>0){ //大于0 表示竞技成功，否则失败
           return "share/arena_record_win";
        }
        return "share/arena_record_fail";
    }

    /**
     * 分享今日排行
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "arena/todayrank", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object arenaTodayRank(@RequestToken String token)throws BizException{
        userSessionService.assertSession(token);
        Long uid=userSessionService.getUid(token);
        Share share = shareService.shareArenaTodayRank(uid);
        return share;
    }

    /**
     * 查看分享的今日排行
     * @param id
     * @param model
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "arena/todayrank/{id}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public Object queryArenaTodayRank(@PathVariable String id,Model model)throws  BizException{
        Map data = shareService.findArenaTodayRank(id);
        if (data == null) {//竞技的今日排行分享未找到
            return ResponseEntity.notFound().build();
        }
        model.addAllAttributes(data);
        return "share/arena_todayrank";
    }
}
