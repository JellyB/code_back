package com.huatu.ztk.paper.service.mock;

import com.google.common.collect.Lists;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.bo.SmallEstimateHeaderBo;
import com.huatu.ztk.paper.bo.SmallEstimateReportBo;
import com.huatu.ztk.paper.bo.SmallEstimateSimpleReportBo;
import com.huatu.ztk.paper.bo.StandardCardSimpleBo;
import com.huatu.ztk.paper.common.*;
import com.huatu.ztk.paper.service.PaperService;
import com.huatu.ztk.question.util.PageUtil;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by huangqingpeng on 2019/2/15.
 */
@Service
public class SmallPaperMockServiceImpl {

    @Autowired
    PaperService paperService;

    /**
     * 小模考首页数据--为参加考试数据
     *
     * @param subject
     * @param uid
     * @return
     */

    public List<SmallEstimateHeaderBo> getEstimateHeaderPage(int subject, long uid) {
        EstimatePaper estimatePaper = buildPaper(subject, uid);
        if (null == estimatePaper) {
            return Lists.newArrayList();
        }
        SmallEstimateHeaderBo smallEstimateHeaderBo = new SmallEstimateHeaderBo();
        smallEstimateHeaderBo.setPaperId(estimatePaper.getId());
        smallEstimateHeaderBo.setName(estimatePaper.getName());
        smallEstimateHeaderBo.setJoinCount(estimatePaper.getPaperMeta().getCardCounts());
        smallEstimateHeaderBo.setStartTime(estimatePaper.getStartTime());
        smallEstimateHeaderBo.setEndTime(estimatePaper.getEndTime());
        smallEstimateHeaderBo.setPointsName(estimatePaper.getPointsName());
        smallEstimateHeaderBo.setQcount(estimatePaper.getQcount());
        smallEstimateHeaderBo.setLimitTime(estimatePaper.getTime());
        smallEstimateHeaderBo.setDescription(estimatePaper.getDescrp());
        smallEstimateHeaderBo.setCourseId(estimatePaper.getCourseId());
        smallEstimateHeaderBo.setCourseName(estimatePaper.getCourseName());
        smallEstimateHeaderBo.setCourseInfo(estimatePaper.getCourseInfo());
        smallEstimateHeaderBo.setStatus(EstimateStatus.ONLINE);
        smallEstimateHeaderBo.setPracticeId(-1L);
        smallEstimateHeaderBo.setIdStr("-1");
        return Lists.newArrayList(smallEstimateHeaderBo);
    }

    public static void main(String[] args) {
        SmallPaperMockServiceImpl smallPaperMockService = new SmallPaperMockServiceImpl();
        AnswerCard answerCard = smallPaperMockService.getAnswerCard(1L, 1, 1, 1);

        System.out.println("this.getEstimateHeaderPage(1,1) = " + smallPaperMockService.handlerSmallEstimateReportBo((StandardCard) answerCard));
    }

    public EstimatePaper buildPaper(int subject, long uid) {

        EstimatePaper estimatePaper = new EstimatePaper();
        estimatePaper.setId(3528076);
        estimatePaper.setName("教育知识与能力");
        estimatePaper.setYear(2019);
        estimatePaper.setArea(-9);
        estimatePaper.setTime(7200);
        estimatePaper.setScore(100);
        estimatePaper.setPassScore(60);
        estimatePaper.setQcount(10);
        estimatePaper.setDifficulty(6);
        //试卷类型
        estimatePaper.setType(PaperType.SMALL_ESTIMATE);
        estimatePaper.setModules(Lists.newArrayList(Module.builder().category(0).name("教育").qcount(10).build()));
        estimatePaper.setStatus(PaperStatus.AUDIT_SUCCESS);
        estimatePaper.setCatgory(subject);
        estimatePaper.setQuestions(Lists.newArrayList(30006327, 30006329, 30006330, 30006331, 30006332, 30006333, 30006335, 30006337, 30006338, 30006340));
        PaperUserMeta build = PaperUserMeta.builder().id(estimatePaper.getId() + "_" + uid).currentPracticeId(558961935925641283L).paperId(estimatePaper.getId())
                .uid(uid).finishCount(2).practiceIds(Lists.newArrayList()).build();
        estimatePaper.setUserMeta(build);
        estimatePaper.setPaperMeta(PaperMeta.builder().cardCounts(5).build());
        estimatePaper.setPointsName("知识点1，知识点2，知识点3，知识点4");

        estimatePaper.setBigQuestions(Lists.newArrayList(30006327, 30006329, 30006330, 30006331, 30006332, 30006333, 30006335, 30006337, 30006338, 30006340));
        estimatePaper.setCourseId(1234);
        estimatePaper.setCourseName("解析课名称");
        estimatePaper.setCourseInfo("解析课程说明信息");
        estimatePaper.setDescrp("考试说明什么的,没有~");

        return estimatePaper;
    }

    public StandardCard createAnswerCard(int id, int subject, long userId, int terminal) {
        StandardCard standardCard = new StandardCard();
//        Paper paper = paperService.findById(id);
        Paper paper = null;
        if (null == paper) {
            paper = buildPaper(subject, userId);
        }
        standardCard.setPaper(paper);
        standardCard.setId(111111111111L);
        standardCard.setIdStr("111111111111");
        long currentTimeMillis = System.currentTimeMillis();
        standardCard.setCurrentTime(currentTimeMillis);
        standardCard.setUserId(userId);
        standardCard.setSubject(subject);
        standardCard.setCatgory(1);
        standardCard.setScore(0);
        standardCard.setDifficulty(6);
        standardCard.setName(paper.getName());
        standardCard.setRcount(0);
        standardCard.setWcount(0);
        standardCard.setUcount(20);
        standardCard.setStatus(AnswerCardStatus.CREATE);
        standardCard.setType(AnswerCardType.SMALL_ESTIMATE);
        standardCard.setTerminal(1);
        standardCard.setExpendTime(0);
        standardCard.setSpeed(0);
        standardCard.setCreateTime(currentTimeMillis);
        standardCard.setLastIndex(0);
        standardCard.setRemainingTime(7200);
        standardCard.setCardCreateTime(currentTimeMillis);
        standardCard.setCorrects(new int[paper.getQcount()]);
        List<String> collect = IntStream.range(0, paper.getQcount()).mapToObj(i -> "0").collect(Collectors.toList());
        String[] answers = new String[paper.getQcount()];
        collect.toArray(answers);
        standardCard.setAnswers(answers);
        standardCard.setTimes(new int[paper.getQcount()]);
        standardCard.setDoubts(new int[paper.getQcount()]);
        standardCard.setHasGift(0);
        return standardCard;
    }

    public StandardCardSimpleBo handlerAnswerCardSimpleBo(StandardCard standardCard) {
        StandardCardSimpleBo standardCardSimpleBo = StandardCardSimpleBo.builder()
                .practiceId(standardCard.getId())
                .name(standardCard.getName())
                .remainTime(standardCard.getRemainingTime())
                .modules(standardCard.getPaper().getModules())
                .questions(standardCard.getPaper().getQuestions())
                .corrects(standardCard.getCorrects())
                .answers(standardCard.getAnswers())
                .doubts(standardCard.getDoubts())
                .build();
        return standardCardSimpleBo;

    }

    /**
     * 继续答题或者查看报告的demo数据 --- 以查看报告为主
     *
     * @param practiceId
     * @param userId
     * @param terminal
     * @param subject
     * @return
     */
    public AnswerCard getAnswerCard(Long practiceId, long userId, int terminal, int subject) {
        StandardCard answerCard = createAnswerCard(1111, subject, userId, terminal);
        answerCard.setId(practiceId);
        answerCard.setIdStr(practiceId + "");

        //TODO 答题卡报告数据填充
        answerCard.setScore(15);
        answerCard.setRcount(3);
        answerCard.setWcount(17);
        answerCard.setUcount(0);
        answerCard.setStatus(3);
        answerCard.setExpendTime(22);
        answerCard.setSpeed(1);
        answerCard.setCreateTime(System.currentTimeMillis());
        answerCard.setLastIndex(19);
        answerCard.setRemainingTime(3578);
        int[] corrects = new int[]{2, 2, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2};
        String[] answers = new String[]{"3", "3", "3", "3", "3", "3", "3", "3", "4", "3", "3", "4", "3", "4", "3", "3", "3", "3", "4", "3"};
        int[] times = new int[]{2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1};
        answerCard.setCorrects(corrects);
        answerCard.setAnswers(answers);
        answerCard.setTimes(times);
        QuestionPointTree questionPoint = QuestionPointTree.builder().id(413).name("中国史").qnum(20).rnum(3).wnum(17).unum(0).accuracy(15).times(22).speed(1).level(2).build();
        answerCard.setPoints(Lists.newArrayList(questionPoint));
        answerCard.setDoubts(new int[20]);
        CardUserMeta cardUserMeta = CardUserMeta.builder().rank(3).total(4).average(56).beatRate(25).max(100)
                .rNumAverage(11).rNumMax(20).submitCount(4).submitRank(4).reportTime(System.currentTimeMillis()).build();
        answerCard.setCardUserMeta(cardUserMeta);
        return answerCard;
    }

    public SmallEstimateReportBo handlerSmallEstimateReportBo(StandardCard standardCard) {
        Long cardCreateTime = standardCard.getCardCreateTime();
        String format = DateFormatUtils.ISO_DATE_FORMAT.format(cardCreateTime);
        SmallEstimateReportBo smallEstimateReportBo = SmallEstimateReportBo.builder()
                .practiceId(standardCard.getId())
                .idStr(standardCard.getId() + "")
                .name(standardCard.getName())
                .qCount(standardCard.getPaper().getQcount())
                .submitCount(standardCard.getCardUserMeta().getSubmitCount())
                .beatRate(standardCard.getCardUserMeta().getBeatRate())
                .typeInfo("小模考 " + format.replace("-", "."))
                .submitTime(standardCard.getCreateTime())
                .rNum(standardCard.getRcount())
                .rNumMax(standardCard.getCardUserMeta().getRNumMax())
                .rNumAverage(standardCard.getCardUserMeta().getRNumAverage())
                .submitSort(standardCard.getCardUserMeta().getSubmitRank())
                .rank(standardCard.getCardUserMeta().getRank())
                .joinCount(standardCard.getPaper().getPaperMeta().getCardCounts())
                .reportTime(standardCard.getCardUserMeta().getReportTime())
                .expendTime(standardCard.getExpendTime())
                .remainTime(standardCard.getRemainingTime())
                .modules(standardCard.getPaper().getModules())
                .questions(standardCard.getPaper().getQuestions())
                .corrects(standardCard.getCorrects())
                .answers(standardCard.getAnswers())
                .doubts(standardCard.getDoubts())
                .times(standardCard.getTimes())
                .questionPointTrees(standardCard.getPoints())
                .score(standardCard.getScore())
                .build();
        return smallEstimateReportBo;

    }

    /**
     * 查询用户小模考报告列表数据
     *
     * @param subject
     * @param uid
     * @return
     */
    public Object getEstimateReportPage(int subject, long uid) {
        SmallEstimateSimpleReportBo reportBo = new SmallEstimateSimpleReportBo();
        reportBo.setName("小模考测试-1");
        reportBo.setPracticeId(111111111111L);
        reportBo.setIdStr("111111111111L");
        reportBo.setQCount(20);
        reportBo.setSubmitCount(100);
        reportBo.setBeatRate(0);
        PageUtil<List<SmallEstimateSimpleReportBo>> pageUtil = new PageUtil<>();
        pageUtil.setResult(Lists.newArrayList(reportBo));
        pageUtil.setTotal(1);
        pageUtil.setTotalPage(1);
        pageUtil.setNext(0);
        return pageUtil;
    }
}
