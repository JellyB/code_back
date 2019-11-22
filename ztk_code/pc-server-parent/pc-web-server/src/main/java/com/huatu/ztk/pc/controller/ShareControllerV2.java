package com.huatu.ztk.pc.controller;

import com.google.common.collect.Maps;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.spring.annotation.RequestToken;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.pc.bean.Share;
import com.huatu.ztk.pc.service.HuatuShareService;
import com.huatu.ztk.pc.service.ShareRewardService;
import com.huatu.ztk.pc.service.ShareService;
import com.huatu.ztk.pc.util.TemplateHashModelUtil;
import com.huatu.ztk.pc.util.WxChatShareUtil;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionType;
import com.huatu.ztk.user.service.UserSessionService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;


@Controller
@RequestMapping(value = "/v2/share")
public class ShareControllerV2 {
    private static final Logger logger = LoggerFactory.getLogger(ShareControllerV2.class);
/*  分享模考大赛的前 MATCH_REWARD_NUMBER 用户 可以获取奖励 分享信息存在redis中  */
    private static  final  int MATCH_SHARE_REWARD_NUMBER = 100;
    private static  final  String MATCH_SHARE_REWARD_KEY = "matchShareKey";

    @Autowired
    private HuatuShareService huatuShareService;

    @Autowired
    private QuestionDubboService questionDubboService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private ShareService shareService;

    @Autowired
    private ShareRewardService shareRewardService;

    /**
     * 分享试题
     *
     * @param qid 试题id
     * @return
     */
    @RequestMapping(value = "question/{qid}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object question(@PathVariable int qid) {
        final Share share = huatuShareService.shareQuestion(qid,true);

        logger.info("share={}", JsonUtil.toJson(share));
        return share;
    }

    /**
     * 查询分享试题
     *
     * @param qid 试题id
     * @return
     */
    @RequestMapping(value = "question/{qid}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public Object queryQuestionShare(@PathVariable int qid, @RequestToken(required = false) String token, Model model) throws BizException {
        final Question question = questionDubboService.findById(qid);
        final Share share = huatuShareService.shareQuestion(qid, false);
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
        model.addAttribute("question", question);
        model.addAttribute("QuestionType", TemplateHashModelUtil.useStaticPackage("com.huatu.ztk.question.common.QuestionType"));
        model.addAttribute("QuestionUtil", TemplateHashModelUtil.useStaticPackage("com.huatu.ztk.question.util.QuestionUtil"));
        if (QuestionType.SINGLE_SUBJECTIVE == question.getType() || QuestionType.MULTI_SUBJECTIVE == question.getType()) { //主观题
            return "share_huatu/question_subject";
        }
        return "share_huatu/question";
    }

    /**
     * 分享练习
     *
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
        Share share = huatuShareService.sharePractice(practiceId, uid, -1);
        return share;
    }

    /**
     * 查看分享练习
     *
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "practice/{id}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public Object querySharePractice(@PathVariable String id, Model model) throws BizException {
        Map data = shareService.findSharePracticeModel(id);
        if (data == null) {//分享未找到
            return ResponseEntity.notFound().build();
        }
        model.addAllAttributes(data);
        if (data.get("matchMark") != null) {
            return "share_huatu/practice_match";
        } else {
                return "share_huatu/practice";
        }
    }

    /**
     * 分享我的练习报告
     *
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "myreport", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object report(@RequestToken String token) throws BizException {
        userSessionService.assertSession(token);
        final long uid = userSessionService.getUid(token);
        final int subject = userSessionService.getSubject(token);
        final int area = userSessionService.getArea(token);
        Share share = huatuShareService.shareReport(uid, subject, area);
        return share;
    }

    /**
     * 查看练习报告分享页面
     *
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "myreport/{id}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public Object queryReport(@PathVariable String id, Model model) throws BizException {
        Map data = shareService.findShareReportModel(id);
        if (data == null) {//分享我的练习报告
            return ResponseEntity.notFound().build();
        }
        model.addAllAttributes(data);
        return "share_huatu/report";
    }

    /**
     * 分享课程
     * + 游客登录
     *
     * @param token
     * @param courseId 课程id
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "course", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
	public Object course(@RequestToken(defaultValue = "") String token, @RequestParam long courseId)
			throws BizException {
		Long uid = -1L;
		if (StringUtils.isNoneEmpty(token)) {
			userSessionService.assertSession(token);
			uid = userSessionService.getUid(token);
		}
		Share share = huatuShareService.shareCourse(courseId, uid, -1);
		return share;
	}

    /**
     * 查看分享的课程
     *
     * @param shareId
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "course/{shareId}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public Object queryShareCourse(@PathVariable String shareId, Model model) throws BizException {
        Map data = shareService.findShareCourse(shareId);
        if (data == null) {//分享我的课程未找到
            return ResponseEntity.notFound().build();
        }
        data.put("handheld_huatu_url",shareService.getUrlForCourseDetail(data));
        model.addAllAttributes(data);
        System.out.println("queryShareCourse = " + JsonUtil.toJson(data));
        return "share_huatu/cousrse";
    }

    /**
     * 分享竞技场战绩
     *
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "arena/summary", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object arenaSummary(@RequestToken String token) throws BizException {
        userSessionService.assertSession(token);
        Long uid = userSessionService.getUid(token);
        Share share = huatuShareService.shareArenaSummary(uid);
        return share;
    }

    /**
     * 查看分享的竞技练习的战绩
     *
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "arena/summary/{id}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public Object queryArenaSummary(@PathVariable String id, Model model) throws BizException {
        Map data = shareService.findArenaSummary(id);
        if (data == null) {//分享竞技练习的战绩未找到
            return ResponseEntity.notFound().build();
        }
        model.addAllAttributes(data);
        return "share_huatu/arena_summary";
    }

    /**
     * 分享竞技场成绩统计
     *
     * @param token
     * @param id
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "arena/record", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object arenaRecord(@RequestToken String token, @RequestParam long id) throws BizException {
        userSessionService.assertSession(token);
        Long uid = userSessionService.getUid(token);
        Share share = huatuShareService.shareArenaRecord(uid, id);
        return share;
    }

    /**
     * 查看分享的我的竞技成绩统计
     *
     * @param id    分享id
     * @param model
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "arena/record/{id}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public Object queryArenaRecord(@PathVariable String id, Model model) throws BizException {
        Map data = shareService.findArenaRecord(id);
        if (data == null) {//分享竞技的成绩统计未找到
            return ResponseEntity.notFound().build();
        }
        model.addAllAttributes(data);
        if (Integer.parseInt(data.get("isWin").toString()) > 0) { //大于0 表示竞技成功，否则失败
            return "share_huatu/arena_record_win";
        }
        return "share_huatu/arena_record_fail";
    }

    /**
     * 分享今日排行
     *
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "arena/todayrank", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object arenaTodayRank(@RequestToken String token) throws BizException {
        userSessionService.assertSession(token);
        Long uid = userSessionService.getUid(token);
        Share share = huatuShareService.shareArenaTodayRank(uid);
        return share;
    }

    /**
     * 查看分享的今日排行
     *
     * @param id
     * @param model
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "arena/todayrank/{id}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public Object queryArenaTodayRank(@PathVariable String id, Model model) throws BizException {
        Map data = shareService.findArenaTodayRank(id);
        if (data == null) {//竞技的今日排行分享未找到
            return ResponseEntity.notFound().build();
        }
        model.addAllAttributes(data);
        return "share_huatu/arena_todayrank";
    }

    /**
     * 模考大赛(这个不是分享报告)
     * 模考大赛分享界面内容传递
     * @param model
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "match/{paperId}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public Object getMatch(@PathVariable int paperId, Model model) throws BizException {
        Match match = huatuShareService.findMatchById(paperId);
        if(match.getEssayPaperId()>0){
            packageTimeInfo(match);
        }
        Map data = Maps.newHashMap();
        data.putAll(JsonUtil.toMap(JsonUtil.toJson(match)));
        Share share = huatuShareService.shareMatch(paperId,false);
        WxChatShareUtil.assertWeiXinInfo(data,share);
        model.addAllAttributes(data);
        return "share_huatu/match";
    }

    /**
     * 模考大赛
     *
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "match", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object match(@RequestParam int paperId, @RequestToken String token) throws BizException {
        long uid = userSessionService.getUid(token);
        String username = userSessionService.getUname(token);
        Share share = huatuShareService.shareMatch(paperId,true);
        //记录分享的用户id
        shareRewardService.recordShareMatchUser(username,paperId,MATCH_SHARE_REWARD_KEY,MATCH_SHARE_REWARD_NUMBER);
        return share;
    }

    /**
     * 分享加积分
     * @param id
     * @param token
     * @throws BizException
     */
    @RequestMapping(value = "reward", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object sendReward(@RequestParam String id, @RequestToken String token) throws BizException{
        logger.info("id={},token={}", id, token);
        userSessionService.assertSession(token);
        long uid = userSessionService.getUid(token);
        String uname = userSessionService.getUname(token);

        return shareRewardService.sendShareMsg(uid, uname, id);
    }

    /**
     *  模考大赛联合考试时间
     * @param match
     */
    private void packageTimeInfo(Match match) {
        long startTime = match.getStartTime();
        long endTime = match.getEndTime();
        long essayStartTime = match.getEssayStartTime();
        long essayEndTime = match.getEssayEndTime();
        //h5界面使用br标签实现回车
        String timeInfo = "行测"+getTimeInfo(startTime,endTime)+"</br>申论"+getTimeInfo(essayStartTime,essayEndTime);
        match.setTimeInfo(timeInfo);
    }
    private String getTimeInfo(long startTime, long endTime) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date(startTime));

        int day = instance.get(Calendar.DAY_OF_WEEK);

        //考试时间：2017年8月20日（周日）09:00-11:00
        String timeInfo = DateFormatUtils.format(startTime, "M月d日") + "（%s）%s-%s";
        String dayString = "";
        switch (day) {
            case Calendar.SUNDAY:
                dayString = "周日";
                break;

            case Calendar.MONDAY:
                dayString = "周一";
                break;

            case Calendar.TUESDAY:
                dayString = "周二";
                break;
            case Calendar.WEDNESDAY:
                dayString = "周三";
                break;
            case Calendar.THURSDAY:
                dayString = "周四";
                break;
            case Calendar.FRIDAY:
                dayString = "周五";
                break;

            case Calendar.SATURDAY:
                dayString = "周六";
                break;
        }

        timeInfo = String.format(timeInfo, dayString, DateFormatUtils.format(startTime, "HH:mm"),
                DateFormatUtils.format(endTime, "HH:mm"));

        return "：" + timeInfo;
    }
}
