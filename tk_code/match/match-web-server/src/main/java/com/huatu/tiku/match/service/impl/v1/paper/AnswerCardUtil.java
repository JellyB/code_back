package com.huatu.tiku.match.service.impl.v1.paper;

import com.google.common.collect.Lists;
import com.huatu.common.utils.date.TimeUtil;
import com.huatu.tiku.match.bo.paper.AnswerCardSimpleBo;
import com.huatu.tiku.match.bo.paper.StandAnswerCardSimpleBo;
import com.huatu.tiku.match.common.MatchConfig;
import com.huatu.tiku.match.common.PaperErrorInfo;
import com.huatu.tiku.match.enums.MatchStatusEnum;
import com.huatu.tiku.match.enums.PaperInfoEnum;
import com.huatu.tiku.match.enums.util.EnumUtil;
import com.huatu.ztk.chart.Line;
import com.huatu.ztk.chart.LineSeries;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.util.VersionUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created by lijun on 2018/11/1
 */
public class AnswerCardUtil {

    private static final int DELAY_TIME = 5;  //5分钟的答案提交延迟容忍

    /**
     * 分数使用小数（新版本）/整数（旧版本）的分界线
     */
    private static final String ANDROID_NEW_VERSION = "7.1.140";
    private static final String IOS_NEW_VERSION = "7.1.150";


    /**
     * 试卷当前是否可以创建答题卡
     */
    public static Boolean isEnableCreateCard(Paper paper) {
        if (null == paper) {
            return false;
        }
        switch (EnumUtil.create(paper.getType(), PaperInfoEnum.PaperTypeEnum.class)) {
            case MATCH:
                EstimatePaper estimatePaper = (EstimatePaper) paper;
                List<Integer> questions = paper.getQuestions();
                if (CollectionUtils.isEmpty(questions)) {
                    return false;
                }
                MatchStatusEnum status = MatchStatusEnum.getMatchStatusForTest(estimatePaper.getStartTime(), estimatePaper.getEndTime());
                return MatchStatusEnum.START_AVAILABLE.valueEquals(status.getKey());
        }
        return true;
    }

    /**
     * 试卷当前是否可以保存答案
     */
    public static Boolean isEnabledSaveAnswerCard(AnswerCard answerCard) {
        if (null == answerCard) {
            return false;
        }
        if (answerCard instanceof StandardCard && null != ((StandardCard) answerCard).getPaper()) {
            Paper paper = ((StandardCard) answerCard).getPaper();
            switch (EnumUtil.create(paper.getType(), PaperInfoEnum.PaperTypeEnum.class)) {
                case MATCH:
                    EstimatePaper estimatePaper = (EstimatePaper) paper;
                    long currentTimeMillis = System.currentTimeMillis();
                    return currentTimeMillis >= estimatePaper.getStartTime() && currentTimeMillis <= estimatePaper.getEndTime() + TimeUnit.MINUTES.toMillis(DELAY_TIME);
            }
        }
        return false;
    }

    /**
     * 试卷当前是否可以提交答案
     */
    public static Boolean isEnabledSubmitAnswerCard(AnswerCard answerCard) {
        return isEnabledSaveAnswerCard(answerCard);
    }

    /**
     * 转换成 答题时需要的答题卡信息
     *
     * @param answerCard 原始答题卡
     * @return
     */
    public static AnswerCardSimpleBo buildStandAnswerCardSimpleBo(AnswerCard answerCard) {
        StandAnswerCardSimpleBo answerCardSimpleBo = new StandAnswerCardSimpleBo();
        BeanUtils.copyProperties(answerCard, answerCardSimpleBo);
        int totalTime = 0;
        if (answerCard instanceof StandardCard) {
            Paper paper = ((StandardCard) answerCard).getPaper();
            answerCardSimpleBo.setModules(paper.getModules());
            answerCardSimpleBo.setPaperId(paper.getId());
            if (paper instanceof EstimatePaper) {
                EstimatePaper estimatePaper = (EstimatePaper) paper;
                answerCardSimpleBo.setStartTime(estimatePaper.getStartTime());
                answerCardSimpleBo.setEndTime(estimatePaper.getEndTime());
                totalTime = estimatePaper.getTime();
            }

        }
        answerCardSimpleBo.setMatchErrorPath(MatchConfig.getInstance().getMatchErrorPath());
        answerCardSimpleBo.setCurrentTime(System.currentTimeMillis());
        long remainTime = answerCardSimpleBo.getEndTime() - answerCardSimpleBo.getCurrentTime();
        if (remainTime <= 0) {
            PaperErrorInfo.AnswerCard.CREATE_ERROR.exception();
        }
        int time = new Long(remainTime / TimeUtil.MILLS_SECOND).intValue();
        answerCardSimpleBo.setRemainingTime(Math.min(time, totalTime));
        return answerCardSimpleBo;
    }

    public static UserAnswers getUserAnswersInfo(StandardCard answerCard) {
        String[] answers = answerCard.getAnswers();
        int[] corrects = answerCard.getCorrects();
        int[] doubts = answerCard.getDoubts();
        int[] times = answerCard.getTimes();
        List<Integer> questions = answerCard.getPaper().getQuestions();
        ArrayList<Answer> newAnswers = Lists.newArrayListWithCapacity(answers.length);
        for (int i = 0; i < answers.length; i++) {
            Answer answerBean = new Answer();
            answerBean.setAnswer(answers[i]);
            answerBean.setCorrect(corrects[i]);
            answerBean.setDoubt(doubts[i]);
            answerBean.setTime(times[i]);
            answerBean.setQuestionId(questions.get(i));
            newAnswers.add(answerBean);
        }
        final UserAnswers userAnswers = UserAnswers.builder()
                .uid(answerCard.getUserId())
                .practiceId(answerCard.getId())
                .area(-9)
                .subject(answerCard.getSubject())
                .catgory(answerCard.getCatgory())
                .submitTime(System.currentTimeMillis())
                .answers(newAnswers)
                .build();
        return userAnswers;
    }

    public static double getAnswerTotalScore(Paper paper) {
        Integer flag = paper.getScoreFlag();
        if (null == flag || flag.intValue() == 0) {
            return paper.getScore();
        }
        double total = 0d;
        for (Double score : paper.getScores()) {
            total += score;
        }
        return total;
    }


    /**
     * 判断版本是否可以使用小数类型分数
     *
     * @param terminal
     * @param userCv
     * @return
     */
    public static Boolean judgeUserCv(int terminal, String userCv) {
        if (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD) {
            return VersionUtil.compare(userCv, IOS_NEW_VERSION) >= 0;
        } else if (terminal == TerminalType.ANDROID || terminal == TerminalType.ANDROID_IPAD) {
            return VersionUtil.compare(userCv, ANDROID_NEW_VERSION) >= 0;
        } else if (terminal == TerminalType.PC || terminal == TerminalType.WEI_XIN || terminal == TerminalType.WEI_XIN_APPLET) {
            //非移动端手机默认是支持小数点分数
            return true;
        }
        return false;
    }

    public static void handlerLine(Line line, Function<Double, Double> function) {
        if (CollectionUtils.isNotEmpty(line.getSeries())) {
            for (LineSeries lineSeries : line.getSeries()) {
                List<Double> list = Lists.newArrayList();
                List<String> strList = Lists.newArrayList();
                for (Number score : lineSeries.getData()) {
                    double newScore = function.apply(score.doubleValue());
                    list.add(newScore);
                    strList.add(String.valueOf(newScore));
                }
                lineSeries.setData(list);
                lineSeries.setStrData(strList);
            }
        }

    }


    public static final Function<Double, Double> transDouble = (doubleNum -> {
        BigDecimal bigDecimal = new BigDecimal(doubleNum);
        double scale = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        return scale;
    });

    public static final Function<Double, Double> transInt = (doubleNum -> {
        BigDecimal bigDecimal = new BigDecimal(doubleNum);
        return bigDecimal.setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
    });


}
