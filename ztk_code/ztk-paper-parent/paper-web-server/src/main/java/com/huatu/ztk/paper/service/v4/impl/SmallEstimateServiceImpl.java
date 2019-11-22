package com.huatu.ztk.paper.service.v4.impl;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.bo.SmallEstimateHeaderBo;
import com.huatu.ztk.paper.bo.SmallEstimateSimpleReportBo;
import com.huatu.ztk.paper.common.AnswerCardStatus;
import com.huatu.ztk.paper.common.AnswerCardType;
import com.huatu.ztk.paper.common.EstimateStatus;
import com.huatu.ztk.paper.dao.AnswerCardDao;
import com.huatu.ztk.paper.enums.ScoreSortEnum;
import com.huatu.ztk.paper.service.PaperAnswerCardService;
import com.huatu.ztk.paper.service.PaperUserMetaService;
import com.huatu.ztk.paper.service.v4.HandlerMetaService;
import com.huatu.ztk.paper.service.v4.PaperServiceV4;
import com.huatu.ztk.paper.service.v4.ScoreSortService;
import com.huatu.ztk.paper.service.v4.SmallEstimateService;
import com.huatu.ztk.paper.util.DateUtil;
import com.huatu.ztk.question.util.PageUtil;
import com.self.generator.core.WaitException;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 小模考相关实现
 * Created by huangqingpeng on 2019/2/13.
 */
@Service
public class SmallEstimateServiceImpl implements SmallEstimateService {
    private final static Logger logger = LoggerFactory.getLogger(SmallEstimateServiceImpl.class);
    public static final int SMALL_DELAY_TIME = 5 * 60;  //小模考可延时交卷的时间（分钟）
    @Autowired
    private PaperServiceV4 paperService;

    @Autowired
    private PaperAnswerCardService paperAnswerCardService;

    @Autowired
    private AnswerCardDao answerCardDao;

    @Autowired
    private PaperUserMetaService paperUserMetaService;

    @Autowired
    HandlerMetaService handlerMetaService;

    @Autowired
    ScoreSortService scoreSortService;

    /**
     * 查询小模考首页实现
     *
     * @param subject
     * @param uid
     * @return
     * @throws BizException
     */
    @Override
    public List<SmallEstimateHeaderBo> findTodaySmallEstimateInfo(int subject, long uid) throws BizException {
        //科目对应的当天的小模考
        List<EstimatePaper> papers = paperService.getTodaySmallEstimatePaper(subject);
        //用户做题数据
        if (CollectionUtils.isNotEmpty(papers)) {
            for (EstimatePaper paper : papers) {                //试卷统计信息并入
                //用户答题情况填充(状态+答题卡数据)
                fillPaperUserMeta(paper, uid);
                //试卷作答次数统计
                int joinCount = handlerMetaService.getJoinCount(paper.getId());
                paper.setPaperMeta(PaperMeta.builder().cardCounts(joinCount).build());
            }
            List<SmallEstimateHeaderBo> results = papers.stream()
                    .map(EstimatePaperUtil::transSmallEstimateHeaderBo)         //转换形式
                    .collect(Collectors.toList());
            return results;
        }
        return Lists.newArrayList();
    }

    /**
     * 创建小模考答题卡实现（如果已经有答题卡，则返回存在的答题卡）
     *
     * @param paperId
     * @param subject
     * @param userId
     * @param terminal
     * @return
     * @throws WaitException
     * @throws BizException
     */
    @Override
    public StandardCard create(int paperId, int subject, long userId, int terminal) throws WaitException, BizException {
        PaperUserMeta userMeta = paperUserMetaService.findById(userId, paperId);
        if (null == userMeta) {
            final Paper paper = paperService.findById(paperId);
            StandardCard practicePaper = paperAnswerCardService.create(paper, subject, userId, terminal);
            //创建答题卡--添加创建记录(参加人数递增)
            handlerMetaService.incrementJoinCount(paperId);
            paperUserMetaService.addUndoPractice(userId, paperId, practicePaper.getId());
            return practicePaper;
        }
        long currentPracticeId = EstimatePaperUtil.getUserPracticeId(userMeta, EstimateStatus.CONTINUE_AVAILABLE);
        AnswerCard answerCard = answerCardDao.findById(currentPracticeId);
        long remainTime = countRemainTime(answerCard);
        answerCard.setRemainingTime(new Long(remainTime).intValue());
        return (StandardCard) answerCard;
    }

    @Override
    public AnswerCard findAnswerCardDetail(Long practiceId, long userId, int terminal, String cv) throws BizException {
        StandardCard report = handlerMetaService.getReportCache(practiceId);
        AnswerCard answerCard = answerCardDao.findById(practiceId);
        if (null != report) {
            //JsonUtil.toObject转换丢失EstimatePaper类的属性，只保留了父类Paper的属性
            if (answerCard instanceof StandardCard) {
                report.setPaper(((StandardCard) answerCard).getPaper());
            }
            logger.info("查询结果为缓存数据：{}", practiceId);
            return report;
        }
        if (null == answerCard) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        checkReportTime(answerCard);
        if (answerCard.getStatus() == AnswerCardStatus.FINISH) {
            //获得普遍的统计数据
            AnswerCard answerCardResult = paperAnswerCardService.findAnswerCardDetail(practiceId, userId, terminal, cv);
            // 统计小模考特定数据
            handlerMetaService.fillSmallEstimateReportInfo(answerCardResult);
            handlerMetaService.putCache((StandardCard) answerCardResult);
            logger.info("查询结果为重新统计数据：{}", practiceId);
            return answerCardResult;
        } else {
            //系统交卷获得报告数据
            AnswerCard answerCard1 = submitAnswer(practiceId, userId, Lists.newArrayList(), -1, terminal, cv);
            logger.info("查询结果为提交试卷数据：{}", practiceId);
            return answerCard1;
        }
    }

    /**
     * 是否可以查看报告
     *
     * @param answerCard
     * @throws BizException
     */
    private void checkReportTime(AnswerCard answerCard) throws BizException {
        if (answerCard instanceof StandardCard) {
            Paper paper = ((StandardCard) answerCard).getPaper();
            int time = paper.getTime();
            Long cardCreateTime = answerCard.getCardCreateTime();
            long endTime = cardCreateTime.longValue() + TimeUnit.SECONDS.toMillis(time);
            long currentTimeMillis = System.currentTimeMillis();
            if (answerCard.getStatus() != AnswerCardStatus.FINISH && endTime > currentTimeMillis) {
                throw new BizException(ErrorResult.create(1000001, "小模考尚未交卷，无法查看报告"));
            }
        }
    }

    /**
     * 保存答案 -- 附带时间判断
     *
     * @param practiceId
     * @param userId
     * @param answers
     * @return
     * @throws BizException
     */
    @Override
    public AnswerCard saveAnswers(Long practiceId, long userId, List<Answer> answers) throws BizException {
        final AnswerCard answerCard = answerCardDao.findById(practiceId);

        if (answerCard == null) {//答题卡未找到
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND, "答题卡不存在");
        }
        validSmallEstimateTime(answerCard, answers);
        return paperAnswerCardService.handlerAnswerCardInfo(userId, answers, -1, false, answerCard);
    }

    /**
     * 交卷  --- 附带报告数据返回
     *
     * @param practiceId
     * @param userId
     * @param answers
     * @param area
     * @return
     * @throws BizException
     */
    @Override
    public AnswerCard submitAnswer(Long practiceId, long userId, List<Answer> answers, int area, int terminal, String cv) throws BizException {
        //校验阶段
        AnswerCard beforeCard = answerCardDao.findById(practiceId);
        validSmallEstimateTime(beforeCard, answers);
        //特定统计数据添加阶段
        handlerMetaService.handlerSubmitInfo(beforeCard);
        //提交阶段
        AnswerCard answerCard = paperAnswerCardService.submitPractice(practiceId, userId, answers, area, terminal, cv);
        //按分数+交卷顺序排序
        scoreSortService.addScoreSort((StandardCard) answerCard, ScoreSortEnum.SCORE_SUBMIT_SORT);
        // 统计小模考特定数据
        handlerMetaService.fillSmallEstimateReportInfo(answerCard);
        handlerMetaService.putCache((StandardCard) answerCard);
        return answerCard;
    }

    @Override
    public PageUtil<List<SmallEstimateSimpleReportBo>> getEstimateReportPage(int subject, long uid, long startTime, long endTime, int page, int size) {
        if (endTime <= 0) {
            endTime = Long.MAX_VALUE;
        }
        PageBean<AnswerCard> pageBean = answerCardDao.findByTypeForPage(subject, uid, AnswerCardType.SMALL_ESTIMATE, startTime, endTime, page, size);
        PageUtil<List<SmallEstimateSimpleReportBo>> pageUtil = new PageUtil<>();
        int total = pageBean.getTotal();
        List<AnswerCard> results = pageBean.getResutls();
        //检查当天的考试是否展示报告
        checkTodayAnswerStaus(results);
        //填充统计信息
        results.forEach(answerCard -> {
            if (answerCard instanceof StandardCard) {
                CardUserMeta cardUserMeta = handlerMetaService.getCardUserMeta((StandardCard) answerCard);
                scoreSortService.reSort(answerCard, ScoreSortEnum.SCORE_SUBMIT_SORT);
                Paper paper = ((StandardCard) answerCard).getPaper();
                PaperMeta meta = PaperMeta.builder().cardCounts(handlerMetaService.getJoinCount(paper.getId())).build();
                paper.setPaperMeta(meta);
                ((StandardCard) answerCard).setCardUserMeta(cardUserMeta);
            }
        });
        //转换返回数据结构
        List<SmallEstimateSimpleReportBo> smallEstimateSimpleReportBos = AnswerCardUtil.transSmallEstimateSimpleReport(results);
        pageUtil.setResult(smallEstimateSimpleReportBos);
        pageUtil.setTotal(total);
        int totalPage = total % size == 0 ? total / size : total / size + 1;
        pageUtil.setTotalPage(totalPage);
        pageUtil.setNext(totalPage > page ? 1 : 0);
        return pageUtil;
    }

    private void checkTodayAnswerStaus(List<AnswerCard> answerCards) {
        if (CollectionUtils.isEmpty(answerCards)) {
            return;
        }
        AnswerCard answerCard = answerCards.get(0);
        Long cardCreateTime = answerCard.getCardCreateTime();
        long todayStartMillions = DateUtil.getTodayStartMillions();
        if (todayStartMillions < cardCreateTime) {        //当天的试卷
            if (answerCard.getStatus() != AnswerCardStatus.FINISH) {  // 未完成状态
                if (answerCard instanceof StandardCard) {
                    long remainTime = countRemainTime(answerCard);    //剩余时间
                    if (remainTime > 0) {
                        answerCards.remove(0);
                    }
                }
            }
        }
    }

    /**
     * 答题时间校验（超时的答题信息，不做保存）
     *
     * @param answerCard
     * @param answers
     * @throws BizException
     */
    private void validSmallEstimateTime(AnswerCard answerCard, List<Answer> answers) throws BizException {
        StandardCard standardCard = (StandardCard) answerCard;
        Long cardCreateTime = standardCard.getCardCreateTime();
        int time = standardCard.getPaper().getTime();
        long endTime = cardCreateTime + TimeUnit.SECONDS.toMillis(time) + TimeUnit.SECONDS.toMillis(SMALL_DELAY_TIME);
        long currentTimeMillis = System.currentTimeMillis();
        if (endTime < currentTimeMillis && CollectionUtils.isNotEmpty(answers)) {
            throw new BizException(ErrorResult.create(100013123, "已超过交卷时间，不能再保存试卷"));
        }
    }

    /**
     * 试卷用户做题状态和用户做题统计数据填充
     *
     * @param paper
     * @param uid
     * @throws BizException
     */
    private void fillPaperUserMeta(EstimatePaper paper, long uid) throws BizException {
        int paperId = paper.getId();
        PaperUserMeta userMeta = paperUserMetaService.findById(uid, paperId);
        //已创建答题卡
        if (userMeta != null) {
            //用户已经交卷
            if (userMeta.getCurrentPracticeId() == -1) {
                paper.setStatus(EstimateStatus.REPORT_AVAILABLE);
            } else {  //当前答题卡未交卷
                long remainTime = getRemainTime(userMeta.getCurrentPracticeId(), paper);
                if (remainTime > 0) {
                    paper.setStatus(EstimateStatus.CONTINUE_AVAILABLE);
                } else {
                    paper.setStatus(EstimateStatus.REPORT_AVAILABLE);
                }
            }
        }
        paper.setUserMeta(userMeta);
    }

    /**
     * 判断是否还有答题时间
     *
     * @param currentPracticeId
     * @param paper
     * @return
     * @throws BizException
     */
    private long getRemainTime(long currentPracticeId, EstimatePaper paper) throws BizException {
        AnswerCard answerCard = answerCardDao.findById(currentPracticeId);
        if (null == answerCard) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        return countRemainTime(answerCard, paper);
    }

    private long countRemainTime(AnswerCard answerCard) {
        if (answerCard instanceof StandardCard) {
            Paper paper = ((StandardCard) answerCard).getPaper();
            if (paper instanceof EstimatePaper) {
                return countRemainTime(answerCard, (EstimatePaper) paper);
            }
        }
        return -1;
    }

    private long countRemainTime(AnswerCard answerCard, EstimatePaper paper) {
        Long cardCreateTime = answerCard.getCardCreateTime();
        long currentTimeMillis = System.currentTimeMillis();
        long expandTime = currentTimeMillis - cardCreateTime;
        long remainTime = paper.getTime() - (expandTime / 1000);
        logger.info("expandTime={},paper.getTime={},remainTime={}", expandTime / 1000, paper.getTime(), remainTime);
        return remainTime;
    }
}
