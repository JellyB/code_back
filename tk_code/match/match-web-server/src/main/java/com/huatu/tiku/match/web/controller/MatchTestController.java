package com.huatu.tiku.match.web.controller;

import com.google.common.collect.Lists;
import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.match.bean.MatchTestBean;
import com.huatu.tiku.match.bo.paper.AnswerCardSimpleBo;
import com.huatu.tiku.match.dto.paper.AnswerDTO;
import com.huatu.tiku.match.service.MatchTestService;
import com.huatu.tiku.match.service.impl.v1.MatchTestServiceImpl;
import com.huatu.tiku.match.service.impl.v1.search.SearchHandler;
import com.huatu.tiku.match.service.v1.enroll.EnrollService;
import com.huatu.tiku.match.service.v1.paper.AnswerCardService;
import com.huatu.tiku.match.service.v1.paper.QuestionService;
import com.huatu.tiku.match.service.v1.practice.PracticeService;
import com.huatu.tiku.match.service.v1.report.ReportService;
import com.huatu.tiku.match.service.v1.reward.PaperRewardService;
import com.huatu.tiku.match.service.v1.share.ShareCreateServer;
import com.huatu.tiku.match.service.v1.tag.TagService;
import com.huatu.tiku.springboot.users.support.Token;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by huangqingpeng on 2019/3/1.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("ts")
@ApiVersion("v1")
public class MatchTestController {

    @Autowired
    private EnrollService enrollService;
    @Autowired
    private MatchTestService matchTestService;
    final AnswerCardService answerCardService;

    final PaperRewardService paperRewardService;

    /**
     * 模考大赛报名
     *
     * @param matchId
     * @param positionId 默认-9处理事业单位不选报名地区的问题
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/enroll/{matchId}", method = RequestMethod.POST)
    public Object enroll(@Token UserSession userSession,
                         @PathVariable int matchId,
                         @RequestParam(defaultValue = "-9") int positionId,
                         @RequestParam(defaultValue = "-1", required = false) Long schoolId,
                         @RequestParam(defaultValue = "", required = false) String schoolName) throws BizException {

        MatchTestBean matchTestBean = matchTestService.randomTest(MatchTestServiceImpl.Operate.Base);
        Object enroll = enrollService.enroll(matchId, matchTestBean.getUserId(), matchTestBean.getUname(), positionId, schoolId, schoolName);
        matchTestBean.setPositionId(positionId);
        matchTestService.saveMatchTestBean(matchTestBean, MatchTestServiceImpl.Operate.Enroll);
        return enroll;
    }


    /**
     * 创建答题卡
     */
    @LogPrint
    @PutMapping(value = "create/{paperId}")
    public Object createAnswerCard(@Token UserSession userSession, @RequestHeader int terminal, @PathVariable int paperId) throws BizException {
        MatchTestBean matchTestBean = matchTestService.randomTest(MatchTestServiceImpl.Operate.Enroll);
        userSession.setId(matchTestBean.getUserId());
        AnswerCardSimpleBo answerCardSimpleBo = answerCardService.createAnswerCard(userSession, paperId, terminal);
        matchTestBean.setPracticeId(answerCardSimpleBo.getId());
        matchTestService.saveMatchTestBean(matchTestBean, MatchTestServiceImpl.Operate.Create);
        return answerCardSimpleBo;
    }

    /**
     * 用户答题数据保存
     */
    @LogPrint
    @PostMapping(value = "{practiceId}/save")
    public Object save(@Token UserSession userSession, @PathVariable long practiceId, @RequestBody List<AnswerDTO> answerList) {
        MatchTestBean matchTestBean = matchTestService.randomTest(MatchTestServiceImpl.Operate.Create);
        long practiceId1 = matchTestBean.getPracticeId();
        if (practiceId1 > 0) {
            practiceId = practiceId1;
        }
        matchTestService.saveMatchTestBean(matchTestBean, MatchTestServiceImpl.Operate.Save);
        return answerCardService.save(matchTestBean.getUserId(), practiceId, answerList);
    }

    /**
     * 用户答题卡保存 -- 交卷
     */
    @LogPrint
    @PostMapping(value = "{practiceId}/submit")
    public Object submit(@Token UserSession userSession, @PathVariable long practiceId, @RequestBody List<AnswerDTO> answerList) {
        MatchTestBean matchTestBean = matchTestService.randomTest(MatchTestServiceImpl.Operate.Save);
        long practiceId1 = matchTestBean.getPracticeId();
        if (practiceId1 > 0) {
            practiceId = practiceId1;
        }
        answerCardService.submit(matchTestBean.getUserId(), practiceId, answerList);
        //积分添加
        paperRewardService.sendMatchSubmitMsg(matchTestBean.getUserId(), matchTestBean.getUname(), practiceId);
        matchTestService.saveMatchTestBean(matchTestBean, MatchTestServiceImpl.Operate.Submit);
        return SuccessMessage.create("操作成功");
    }

    /**
     * 获取错题解析试题详情
     */
    @LogPrint
    @GetMapping(value = "{practiceId}/getWrongQuestionAnalysis")
    public Object getWrongQuestionAnalysis(@PathVariable long practiceId) {
        MatchTestBean matchTestBean = matchTestService.randomTest(MatchTestServiceImpl.Operate.Submit);
        long practiceId1 = matchTestBean.getPracticeId();
        if (practiceId1 > 0) {
            practiceId = practiceId1;
        }
        return answerCardService.getWrongQuestionAnalysis(practiceId);
    }

    /**
     * 获取答题时试题详情
     */
    @LogPrint
    @GetMapping(value = "{practiceId}/getAllAnalysisInfo")
    public Object questionAnalysisInfo(@PathVariable long practiceId) {
        MatchTestBean matchTestBean = matchTestService.randomTest(MatchTestServiceImpl.Operate.Submit);
        long practiceId1 = matchTestBean.getPracticeId();
        if (practiceId1 > 0) {
            practiceId = practiceId1;
        }
        return answerCardService.getAllQuestionAnalysis(practiceId);
    }

    final QuestionService questionService;

    /**
     * 获取答题时试题详情
     */
    @GetMapping(value = "{paperId}/simpleInfo")
    public Object questionSimpleInfo(@PathVariable int paperId) {
        return questionService.findQuestionSimpleBoByPaperId(paperId);
    }

    @Autowired
    private PracticeService practiceService;


    /**
     * 查看报告
     *
     * @param paperId
     * @return
     * @throws BizException
     */
    @LogPrint
    @GetMapping(value = "report/{paperId}")
    public Object get(@Token UserSession userSession,
                      @PathVariable(value = "paperId") int paperId,
                      @RequestHeader(defaultValue = "7.1.140") String cv,
                      @RequestHeader int terminal) throws BizException {
        MatchTestBean matchTestBean = matchTestService.randomTest(MatchTestServiceImpl.Operate.Submit);
        String token = userSession.getToken();
        return practiceService.getUserAnswerCard(paperId, matchTestBean.getUserId(), matchTestBean.getUname(), token, cv, terminal);
    }

    @Autowired
    private ReportService reportService;

    /**
     * 获取当前tag 下我的模考报告
     *
     * @param userSession
     * @param tagId
     * @return
     * @throws BizException
     */
    @GetMapping(value = "report/{tagId}")
    public Object list(@Token UserSession userSession,
                       @PathVariable(value = "tagId") int tagId,
                       @RequestHeader(defaultValue = "-1") int subject,
                       @RequestHeader(defaultValue = "7.1.140")  String cv,
                       @RequestHeader int terminal) throws BizException {
        MatchTestBean matchTestBean = matchTestService.randomTest(MatchTestServiceImpl.Operate.Base);
        if (subject < 0) {
            subject = userSession.getSubject();
        }
        return reportService.myReportList(matchTestBean.getUserId(), tagId, subject, cv, terminal);
    }

    @Autowired
    private SearchHandler searchHandler;

    @GetMapping("search")
    public Object matches(@Token(required = false, check = false) UserSession userSession,
                          @RequestHeader(value = "subject", defaultValue = "-1") int subject,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "100") int size) throws BizException {
        MatchTestBean matchTestBean = matchTestService.randomTest(MatchTestServiceImpl.Operate.Base);
        userSession.setId(matchTestBean.getUserId());
        return searchHandler.dealSearch(userSession, subject, page, size,
                SearchHandler.DEFAULT_HEAD_FILTER);
    }

    @Autowired
    private ShareCreateServer shareCreateServer;

    @PostMapping("share")
    public Object matchPractice(@Token UserSession userSession,
                                @RequestParam int paperId,
                                @RequestHeader(defaultValue = "7.1.140")  String cv,
                                @RequestHeader int terminal) throws BizException {
        MatchTestBean matchTestBean = matchTestService.randomTest(MatchTestServiceImpl.Operate.Base);
        String token = userSession.getToken();
        return shareCreateServer.buildShareInfo(paperId, matchTestBean.getUserId(), matchTestBean.getUname(), token, cv, terminal);
    }

    @Autowired
    private TagService tagService;

    /**
     * 获取科目对应模考大赛的标签
     * --标签数据通过pandora接口生成(/pand/match/tag)
     * --申论标签查询可以只返回申论相关的标签（通过flag做的筛选）
     *
     * @param subject
     * @return
     * @throws BizException
     */
    @GetMapping("tagList/{subject}")
    public Object getTags(@PathVariable(value = "subject") int subject) throws BizException {
        if (subject < 0) {
            return Lists.newArrayList();
        }
        return tagService.getTags(subject);
    }
}
