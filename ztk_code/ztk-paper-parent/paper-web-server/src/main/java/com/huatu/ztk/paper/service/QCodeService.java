package com.huatu.ztk.paper.service;

import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.common.*;
import com.huatu.ztk.paper.dao.AnswerCardDao;
import com.huatu.ztk.paper.util.MatchResponseUtil;
import com.huatu.ztk.paper.util.VersionUtil;
import com.self.generator.core.WaitException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by lijun on 2018/11/6
 */
@Service
public class QCodeService {

    private static final Logger logger = LoggerFactory.getLogger(QCodeService.class);
    private static final int DEFAULT_CODE = 5000000;

    @Autowired
    private AnswerCardDao answerCardDao;

    @Autowired
    private PaperService paperService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private PaperAnswerCardService paperAnswerCardService;

    @Autowired
    private PaperUserMetaService paperUserMetaService;

    @Autowired
    MatchChangeConfig matchChangeConfig;

    /**
     * 根据 试卷ID返回信息
     *
     * @param paperId 试卷ID
     * @param userId  用户ID
     * @param cv
     * @param token
     * @return 答题卡信息
     */
    public Object getInfoByPaperId(int paperId, int subject, long userId, int terminal, String cv, String token) throws BizException, WaitException {
        Paper paper = paperService.findById(paperId);
        if (null == paper) {
            throwException("试卷信息不存在");
        }
        if (paper.getStatus() != PaperStatus.AUDIT_SUCCESS) {
            throwException("试卷已删除");
        }
        if (subject < 0) {
            subject = paper.getCatgory();
        }
        switch (paper.getType()) {
            case PaperType.MATCH:
                return handleMatchWithVersion(paper, subject, userId, terminal, cv, token);
            case PaperType.TRUE_PAPER:
            case PaperType.APPLETS_PAPER:
                //真题演练，任何时间都可以创建
                return handelOnlyCreatePaper(paper, subject, userId, terminal);
            case PaperType.ESTIMATE_PAPER:
                //精准估分
                return handelCreateOnePaperAndReturnInfo(paper, subject, userId, terminal, cv);
            case PaperType.CUSTOM_PAPER:
                //专项模考
                return handelCreateOnePaperAndReturnInfo(paper, subject, userId, terminal, cv);
        }
        logger.info(" qCode error = 类型未匹配，= {}", paper.getType());
        return null;
    }

    /**
     * 根据版本号做判断
     *
     * @param paper
     * @param subject
     * @param userId
     * @param terminal
     * @param cv
     * @param token
     * @return
     * @throws BizException
     * @throws WaitException
     */
    private Object handleMatchWithVersion(Paper paper, int subject, long userId, int terminal, String cv, String token) throws BizException, WaitException {
        if (paper.getCatgory() != subject) {
            throwException("试卷" + paper.getName() + "不在此科目下，请切换到指定科目后扫码");
        }
        //版本号判断
        if (terminal == TerminalType.ANDROID || terminal == TerminalType.ANDROID_IPAD) {
            if (VersionUtil.compare(cv, matchChangeConfig.getMatchAndroidCvDeadline()) < 0) {      //版本低,直接返回默认值
                return handleMatchOldVersion(paper, subject, userId, terminal);
            } else if (cv.equals(matchChangeConfig.getMatchAndroidCvDeadline())) {
                throwException("当前版本不支持模考大赛和往期模考的扫描功能，请升级版本");
            }
        } else if (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD) {
            if (VersionUtil.compare(cv, matchChangeConfig.getMatchIphoneCvDeadline()) < 0) {       //版本低,直接返回默认值
                return handleMatchOldVersion(paper, subject, userId, terminal);
            } else if (cv.equals(matchChangeConfig.getMatchIphoneCvDeadline())) {
                throwException("当前版本不支持模考大赛和往期模考的扫描功能，请升级版本");
            }
        }
        //科目判断
        boolean oldFlagForSubject = matchService.isOldFlagForSubject(subject);
        if (oldFlagForSubject) {
            return handleMatchOldVersion(paper, subject, userId, terminal);
        } else {
            return handleMatchNewVersion(paper, subject, userId, terminal, token);
        }
    }

    /**
     * 新版本的模考大赛的扫码逻辑处理
     *
     * @param paper
     * @param subject
     * @param userId
     * @param terminal
     * @param token
     * @return
     */
    private Object handleMatchNewVersion(Paper paper, int subject, long userId, int terminal, String token) throws BizException, WaitException {
        List<LinkedHashMap> matches = MatchResponseUtil.getMatches(token, subject);
        //模考大赛、往期模考
        //获取当前的模考大赛
        boolean isMatch;
        if (CollectionUtils.isEmpty(matches)) {
            isMatch = false;
        } else {
            isMatch = matches.stream().anyMatch(match -> MapUtils.getInteger(match, "matchId") == paper.getId());
        }
        if (isMatch) {
            LinkedHashMap match = matches.stream().filter(map -> MapUtils.getInteger(map, "matchId") == paper.getId()).findAny().get();
            return handleNewMatch(paper, terminal, token, match);
        } else {
            return handelOnlyCreatePaper(paper, subject, userId, terminal);
        }
    }


    /**
     * 旧版本模考大赛的扫码逻辑处理
     *
     * @param paper
     * @param subject
     * @param userId
     * @param terminal
     * @return
     * @throws BizException
     * @throws WaitException
     */
    private Object handleMatchOldVersion(Paper paper, int subject, long userId, int terminal) throws BizException, WaitException {
        int paperId = paper.getId();
        //模考大赛、往期模考
        //获取当前的模考大赛
        boolean isMatch;
        try {
            //如果当前ID 未在展示的模考大赛中出现，认为是往期模考
            List<Match> matchList = matchService.getMatchesWithEssay(userId, subject);
            isMatch = matchList.stream().anyMatch(match -> match.getPaperId() == paperId);
            logger.info("当前模考大赛 = {}，paperId = {},isMatch = {}", matchList, paperId, isMatch);
        } catch (BizException e) {
            //当前模考大赛信息 为 空
            if (e.getErrorResult().getCode() == MatchErrors.NO_MATCH.getCode()) {
                isMatch = false;
            } else {
                throw e;
            }
        }
        if (isMatch) {
            return handelMatch(paperId, subject, userId, terminal);
        } else {
            return handelOnlyCreatePaper(paper, subject, userId, terminal);
        }
    }

    /**
     * 当前正在进行的模考大赛类型试卷处理（模考大赛）
     *
     * @param paperId  试卷ID
     * @param subject  科目ID
     * @param userId   用户ID
     * @param terminal 设备类型
     * @return 答题卡 or 答题报告
     * @throws BizException
     * @throws WaitException
     */
    private Object handelMatch(int paperId, int subject, long userId, int terminal) throws BizException, WaitException {
        //1.如果当前是模考大赛
        //如果当前时间在模考大赛之前，创建答题卡中会提示 未在答题时间，如果当前时间在模考大赛之后直接返回答题卡or提示错过考试
        StandardCard standardCard = matchService.createPractice(paperId, subject, userId, terminal);
        if (null == standardCard) {
            throwException("模考大赛答题卡创建失败");
        }
        EstimatePaper paper = (EstimatePaper) standardCard.getPaper();
        if (standardCard.getStatus() != AnswerCardStatus.FINISH) {
            //试卷状态未完成
            if (System.currentTimeMillis() > paper.getEndTime()) {
                //如果是模考大赛，避免在超出答题时间依旧答题的情况
                throw new BizException(MatchErrors.MISSING_MATCH);
            }
            return standardCard;
        } else {
            //模考大赛 目前不考虑 查看答题报告策略，默认考试结束后查看。
            if (paper.getEndTime() > System.currentTimeMillis()) {
                //如果当前已经完成，判断是否可以查看模考大赛报告
                return SuccessMessage.create("请在本次模考活动结束后查看报告");
            }
            return paperAnswerCardService.getUserMatchAnswerCardMetaInfo(paperId, userId);
        }
    }

    /**
     * 当前正在进行的新模考大赛试卷逻辑处理（match项目外部接口引入）
     *
     * @param paper
     * @param terminal
     * @param token
     * @param match
     * @return
     */
    private Object handleNewMatch(Paper paper, int terminal, String token, LinkedHashMap match) throws BizException {
        Integer status = MapUtils.getInteger(match, "status");
        try {
            switch (status) {
                case MatchStatus.UN_ENROLL:         //未报名
                    throw new BizException(MatchErrors.NOT_ENROLL);
                case MatchStatus.ENROLL:            //报名成功
                case MatchStatus.START_UNAVILABLE:          //还未开始考试
                    throw new BizException(MatchErrors.NOT_START);
                case MatchStatus.NOT_SUBMIT:                //为交卷，继续答题
                case MatchStatus.START_AVILABLE:            //开始考试
                    return MatchResponseUtil.createAnswerCard(token, paper, terminal);
                case MatchStatus.MATCH_UNAVILABLE:          //无法考试
                    throw new BizException(MatchErrors.MISSING_MATCH);
                case MatchStatus.REPORT_AVAILABLE:             //可以查看报告
                    return MatchResponseUtil.getReport(token, paper.getId());
                case MatchStatus.REPORT_UNAVILABLE:             //无法查看报告
                    throwException("模考大赛报告还未生成");
                default:
                    throwException("无效的模考大赛状态");
            }
        } catch (com.huatu.common.exception.BizException e) {
            throwException(e.getMessage());
        }

        return null;
    }

    /**
     * 直接创建答题卡(真题演练、往期模考)
     *
     * @param paper    试卷信息
     * @param subject  科目ID
     * @param userId   用户ID
     * @param terminal 设备类型
     * @return 答题卡
     */
    private Object handelOnlyCreatePaper(Paper paper, int subject, long userId, int terminal) throws WaitException, BizException {
        AnswerCard undoCard = paperAnswerCardService.findUndoCard(userId, paper.getId());
        if (null != undoCard) {
            return undoCard;
        }
        StandardCard standardCard = paperAnswerCardService.create(paper, subject, userId, terminal);
        if (null == standardCard) {
            return CommonErrors.RESOURCE_NOT_FOUND;
        }
        paperUserMetaService.addUndoPractice(userId, paper.getId(), standardCard.getId());
        return standardCard;
    }

    /**
     * 在特定时间创建答题卡、只能创建一次、特定条件查看报告（精准估分、往期模考）
     *
     * @param paper    试卷信息
     * @param subject  科目ID
     * @param userId   用户ID
     * @param terminal 设备类型
     * @return 答题卡 or 答题报告
     */
    private Object handelCreateOnePaperAndReturnInfo(Paper paper, int subject, long userId, int terminal, String cv) throws BizException, WaitException {
        AnswerCard undoCard = paperAnswerCardService.findUndoCard(userId, paper.getId());
        if (null != undoCard) {
            //未做完
            return undoCard;
        } else {
            AnswerCard answerCard = answerCardDao.findAnswerCards(userId, paper.getId());
            if (null == answerCard) {
                return createLimitCard(paper, subject, userId, terminal);
            }
            StandardCard standardCard = (StandardCard) answerCard;
            if (AnswerCardStatus.FINISH == standardCard.getStatus()) {
                EstimatePaper standardCardPaper = (EstimatePaper) standardCard.getPaper();
                if (standardCardPaper.getLookParseTime() == 1) {
                    return paperAnswerCardService.findAnswerCardDetail(standardCard.getId(), userId, terminal, cv);
                } else if (System.currentTimeMillis() >= standardCardPaper.getEndTime() && System.currentTimeMillis() <= standardCardPaper.getOfflineTime()) {
                    return paperAnswerCardService.findAnswerCardDetail(standardCard.getId(), userId, terminal, cv);
                } else if (System.currentTimeMillis() > standardCardPaper.getOfflineTime()) {
                    return SuccessMessage.create("试卷已下线");
                } else {
                    return SuccessMessage.create("请在本次活动结束后查看报告");
                }
            }
            logger.info(" qCode error = 精准估分、往期模考无未完成，答题卡状态为= {}", standardCard.getStatus());
            return null;
        }
    }

    /**
     * 有开始时间显示的答题卡创建
     *
     * @throws WaitException
     * @throws BizException
     */
    private StandardCard createLimitCard(Paper paper, int subject, long userId, int terminal) throws WaitException, BizException {
        EstimatePaper estimatePaper = (EstimatePaper) paper;
        if (System.currentTimeMillis() >= estimatePaper.getStartTime() && System.currentTimeMillis() <= estimatePaper.getEndTime()) {
            StandardCard standardCard = paperAnswerCardService.create(paper, subject, userId, terminal);
            if (null == standardCard) {
                throwException("答题卡创建失败");
            }
            paperUserMetaService.addUndoPractice(userId, paper.getId(), standardCard.getId());
            return standardCard;
        }
        logger.info(" qCode error =有开始时间显示的答题卡创建 = {}", estimatePaper);
        return null;
    }


    private static void throwException(String message) throws BizException {
        throw new BizException(ErrorResult.create(DEFAULT_CODE, message));
    }
}
