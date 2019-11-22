package com.huatu.ztk.pc.controller;

import com.huatu.ztk.commons.*;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.commons.spring.annotation.RequestToken;
import com.huatu.ztk.knowledge.api.QuestionStrategyDubboService;
import com.huatu.ztk.knowledge.bean.QuestionStrategy;
import com.huatu.ztk.paper.api.PracticeCardDubboService;
import com.huatu.ztk.paper.api.PracticeDubboService;
import com.huatu.ztk.paper.bean.Answer;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.PracticeCard;
import com.huatu.ztk.paper.bean.PracticePaper;
import com.huatu.ztk.pc.bean.ShenlunPaper;
import com.huatu.ztk.pc.bean.ShenlunSummary;
import com.huatu.ztk.pc.common.ModuleConvert;
import com.huatu.ztk.pc.service.ShenlunService;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.user.dubbo.VersionDubboService;
import com.huatu.ztk.user.service.UserSessionService;
import com.self.generator.core.WaitException;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-09-09 08:45
 */


@Controller
@RequestMapping(value = "/wechat/")
public class WechatController {
    private static final Logger logger = LoggerFactory.getLogger(WechatController.class);
    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private QuestionDubboService questionDubboService;

    @Autowired
    private PracticeCardDubboService practiceCardDubboService;

    @Autowired
    private PracticeDubboService practiceDubboService;

    @Autowired
    private ShenlunService shenlunService;

    @Autowired
    private VersionDubboService versionDubboService;

    @Autowired
    private QuestionStrategyDubboService  questionStrategyDubboService;

    @RequestMapping(value = "index", method = RequestMethod.GET)
    public String dologin(@RequestToken String token,
                          @RequestHeader(value = "User-Agent") String userAgent,
                          Model model) throws BizException {
        try {
            //用户id
            userSessionService.assertSession(token);
        }catch (Exception e){
            return "redirect:/user/login";
        }
        long userid = userSessionService.getUid(token);
        String appUrl = getAppUrl(userAgent);
        model.addAttribute("userid", userid);
        model.addAttribute("appUrl", appUrl);
        return "wechat/index";
    }

    /**
     * 提交答案
     *
     * @return
     */
    @RequestMapping(value = "cards/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object answer(@RequestToken String token, @PathVariable long id, String answers, Model model) throws BizException {
        List<Answer> answerList = JsonUtil.toList(answers, Answer.class);
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        final int area = userSessionService.getArea(token);
        AnswerCard answerCard = practiceCardDubboService.submitAnswers(id, userId, answerList, true, area,TerminalType.WEI_XIN,"");
        return SuccessMessage.create("交卷成功");
    }

    /**
     * 微信抽题，创建试卷
     * @param token
     * @param pointid
     * @param response
     * @param model
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "cards", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String exercise(@RequestToken String token, Integer pointid, HttpServletResponse response, Model model) throws BizException {
        try {
            //用户id
            userSessionService.assertSession(token);
        }catch (Exception e){
            return "redirect:/user/login";
        }
        long userid = userSessionService.getUid(token);
        Integer subject = userSessionService.getSubject(token);
        PracticePaper practicePaper = practiceDubboService.createWeixinPaper(userid, subject, pointid, 5);
        PracticeCard practiceCard = practiceCardDubboService.create(practicePaper, TerminalType.WEI_XIN, 11, userid);
        List<Question> questionList = questionDubboService.findBath(practicePaper.getQuestions());
        model.addAttribute("questionList", questionList);
        model.addAttribute("userid", userid);
        model.addAttribute("catname", ModuleConvert.knowledgeMap.get(pointid));
        model.addAttribute("pointid", pointid);
        model.addAttribute("paperid", practiceCard.getId() + "");
        return "wechat/answer";
    }

    /**
     * 我的答题卡列表
     *
     * @param token
     * @return
     */
    @RequestMapping(value = "cards", method = RequestMethod.GET)
    public String myrecord(@RequestToken String token, @RequestParam(defaultValue = Long.MAX_VALUE + "") long cursor,
                           @RequestParam(defaultValue = "") String cardTime,
                           @RequestHeader(value = "User-Agent") String userAgent,
                           Model model) throws BizException, WaitException {
        try {
            //用户id
            userSessionService.assertSession(token);
        }catch (Exception e){
            return "redirect:/user/login";
        }
        logger.info("userAgent=>{}",userAgent);
        //用户id
        long userId = userSessionService.getUid(token);
        if (cursor < 1) {//说明查询第一页，那么，cursor设置为最大值
            cursor = Long.MAX_VALUE;
        }
        final PageBean pageBean = practiceCardDubboService.findCards(userId,CatgoryType.GONG_WU_YUAN , cursor, 10, 11, cardTime);
        String appUrl = getAppUrl(userAgent);
        model.addAttribute("list_data", pageBean);
        model.addAttribute("udid", userId);
        model.addAttribute("appUrl", appUrl);
        return "wechat/record";
    }

    private String getAppUrl(String userAgent) {
        final OperatingSystem operatingSystem = UserAgent.parseUserAgentString(userAgent).getOperatingSystem().getGroup();
        String appUrl = "http://tiku.huatu.com/index.php?mod=administration&act=index";
        if (operatingSystem ==  OperatingSystem.ANDROID) {
            appUrl = versionDubboService.getLatestVersion(TerminalType.ANDROID, CatgoryType.GONG_WU_YUAN).getFull();
        }else if (operatingSystem == OperatingSystem.IOS) {
            appUrl = "http://a.app.qq.com/o/simple.jsp?pkgname=com.netschool.main.ui";
        }
        return appUrl;
    }

    /**
     * 获取我的答题记录
     * restful
     *
     * @param token
     * @return
     */
    @RequestMapping(value = "cards/{id}", method = RequestMethod.GET)
    public String myRecordDetail(@RequestToken String token, @PathVariable long id, Model model) throws BizException {
        try {
            //用户id
            userSessionService.assertSession(token);
        }catch (Exception e){
            return "redirect:/user/login";
        }
        long userid = userSessionService.getUid(token);
        AnswerCard answerCard = practiceCardDubboService.findById(id);
        PracticePaper paper = ((PracticeCard) answerCard).getPaper();
        List<Question> questionList = questionDubboService.findBath(paper.getQuestions());
        model.addAttribute("questionList", questionList);
        model.addAttribute("userid", answerCard.getUserId());
        model.addAttribute("answerCard", answerCard);
        model.addAttribute("catname", (paper.getModules().get(0)).getName());
        model.addAttribute("pointid", (paper.getModules().get(0)).getCategory());
        return "wechat/myrecord";
    }

    /**
     * 申论首页
     *
     * @return
     */
    @RequestMapping(value = "shenlun/index", method = RequestMethod.GET)
    public String shenlunIndex(@RequestHeader(value = "User-Agent") String userAgent, Model model, HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        System.out.println("remoteAddr = " + remoteAddr + "接口地址"+httpServletRequest.getRequestURL().toString());
        List<ShenlunSummary> summaryList = shenlunService.querySummary();
        String appUrl = getAppUrl(userAgent);
        model.addAttribute("appUrl", appUrl);
        model.addAttribute("summaryList", summaryList);
        return "wechat/shenlun_index";
    }

    /**
     * 按地区获取申论列表
     *
     * @param
     * @return
     */
    @RequestMapping(value = "shenlun/list", method = RequestMethod.GET)
    public String shunlunList(@RequestParam int areaId, Model model,HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        System.out.println("remoteAddr = " + remoteAddr + "接口地址"+httpServletRequest.getRequestURL().toString());
        List<ShenlunPaper> paperList = shenlunService.findByAreaId(areaId);
        //按区域id获取区域名称
        String areaName = AreaConstants.getArea(areaId).getName();
        model.addAttribute("paperList", paperList);
        model.addAttribute("areaName", areaName);
        return "wechat/shenlun_list";
    }

    /**
     * 申论真题试卷
     *
     * @return
     */
    @RequestMapping(value = "shenlun/{id}", method = RequestMethod.GET)
    public Object shenlunDetail(@PathVariable int id, Model model,HttpServletRequest httpServletRequest) throws IOException {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        System.out.println("remoteAddr = " + remoteAddr + "接口地址"+httpServletRequest.getRequestURL().toString());
        ShenlunPaper paper = shenlunService.findById(id);
        if (paper == null) {
            return CommonErrors.RESOURCE_NOT_FOUND;
        }
        model.addAttribute("paper", paper);
        return "wechat/shenlun_detail";
    }

    /**
     * 微信抽题
     * @param pointid
     * @param subject
     * @param size
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = "cardsWechat", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object exerciseWechat(@RequestParam(defaultValue = 0 + "")  Integer pointid,
                                 @RequestParam(defaultValue = 1 + "")int subject,
                                 @RequestParam(defaultValue = 10 + "")int size,
                                 HttpServletRequest httpServletRequest,
                                 HttpServletResponse response, Model model){
        String remoteAddr = httpServletRequest.getRemoteAddr();
        System.out.println("remoteAddr = " + remoteAddr + "接口地址"+httpServletRequest.getRequestURL().toString());
        QuestionStrategy questionStrategy=questionStrategyDubboService.randomStrategy(0,subject,pointid,size);
        List<Question> questionList = questionDubboService.findBath(questionStrategy.getQuestions());
        return questionList;
    }

}
