package com.huatu.ztk.paper.service.v4.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.chart.Line;
import com.huatu.ztk.chart.LineSeries;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.util.GeneticSupportUtil;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.bo.SmallEstimateSimpleReportBo;
import com.huatu.ztk.paper.util.VersionUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by huangqingpeng on 2019/2/20.
 */
public class AnswerCardUtil {


    private static final Logger logger = LoggerFactory.getLogger(AnswerCardUtil.class);

    /**
     * 分数使用小数（新版本）/整数（旧版本）的分界线
     */
    private static final String ANDROID_NEW_VERSION = "7.1.140";
    private static final String IOS_NEW_VERSION = "7.1.140";

    /**
     * 转换答题卡为考试需要的数据
     *
     * @param standardCard
     * @return
     */
    public static void transSmallEstimateReport(StandardCard standardCard) {
        List<QuestionPointTree> points = standardCard.getPoints();
        List<QuestionPointTree> afterPoints = Lists.newArrayList();
        for (QuestionPointTree point : points) {
            afterPoints.addAll(getUnderPoints(point));
        }
        standardCard.setPoints(afterPoints);
        //0分处理
        double score = standardCard.getScore();
        CardUserMeta cardUserMeta = standardCard.getCardUserMeta();
        cardUserMeta.setTotal(cardUserMeta.getSubmitCount());
        if (score == 0d) {
            cardUserMeta.setRank(cardUserMeta.getSubmitCount());
            cardUserMeta.setBeatRate(0);
        }
    }

    private static List<QuestionPointTree> getUnderPoints(QuestionPointTree point) {
        List<QuestionPointTree> children = point.getChildren();
        if (CollectionUtils.isEmpty(children)) {
            return Lists.newArrayList(point);
        }
        List<QuestionPointTree> result = Lists.newArrayList();
        for (QuestionPointTree child : children) {
            result.addAll(getUnderPoints(child));
        }
        return result;
    }

    /**
     * 转化为报告列表数据
     *
     * @param answerCards
     * @return
     */
    public static List<SmallEstimateSimpleReportBo> transSmallEstimateSimpleReport(List<AnswerCard> answerCards) {
        ArrayList<SmallEstimateSimpleReportBo> list = Lists.newArrayList();
        for (AnswerCard answerCard : answerCards) {
            SmallEstimateSimpleReportBo reportBo = new SmallEstimateSimpleReportBo();
            reportBo.setName(answerCard.getName());
            reportBo.setPracticeId(answerCard.getId());
            reportBo.setIdStr(answerCard.getId() + "");
            reportBo.setQCount(answerCard.getCorrects().length);
            if (answerCard instanceof StandardCard) {
                CardUserMeta cardUserMeta = ((StandardCard) answerCard).getCardUserMeta();
                if (null != cardUserMeta) {
//                    reportBo.setSubmitCount(cardUserMeta.getSubmitCount());
                    reportBo.setBeatRate(cardUserMeta.getBeatRate());
                }
                int cardCounts = ((StandardCard) answerCard).getPaper().getPaperMeta().getCardCounts();
                reportBo.setSubmitCount(cardCounts);
            }
            list.add(reportBo);
        }
        return list;
    }

    /**
     * 小模考特定的排名逻辑算法
     *
     * @param cardUserMeta
     * @param rank
     * @param total
     */
    public static void reBuildCardMeta(CardUserMeta cardUserMeta, Long rank, Long total) {
        if (null != cardUserMeta) {
            cardUserMeta.setTotal(total.intValue());
            cardUserMeta.setSubmitCount(total.intValue());
            cardUserMeta.setRank(rank.intValue());
            final int beatRate = (int) ((total - rank) * 100 / total);
            cardUserMeta.setBeatRate(beatRate);
        }
    }

    /**
     * answerCard对象补充IdStr属性，pc专用
     *
     * @param answerCard
     */
    public static void fillIdStr(AnswerCard answerCard) {
        if (null == answerCard) {
            return;
        }
        long id = answerCard.getId();
        if (answerCard instanceof StandardCard) {
            ((StandardCard) answerCard).setIdStr(id + "");
        } else if (answerCard instanceof PracticeCard) {
            ((PracticeCard) answerCard).setIdStr(id + "");
        }
    }

    /**
     * 阶段测试排名逻辑算法
     *
     * @param cardUserMeta
     * @param rank
     * @param total
     */
    public static void periodTestReBuildCardMeta(CardUserMeta cardUserMeta, Long rank, Long total) {
        if (null != cardUserMeta) {
            cardUserMeta.setTotal(total.intValue());
            cardUserMeta.setSubmitCount(total.intValue());
            cardUserMeta.setRank(rank.intValue());
            int beatRate = 0;
            if (total >= rank) {
                beatRate = (int) ((total - rank) * 100 / total);
            }
            cardUserMeta.setBeatRate(beatRate);
        }
    }

    /*
     * 课后作业答题卡信息筛选返回
     *
     * @param answerCard
     * @return
     */
    public static LinkedHashMap<String, Object> transCourseExercisesCardMap(AnswerCard answerCard) {
        LinkedHashMap<String, Object> questionMap = Maps.newLinkedHashMap();
        if (null != answerCard) {
            questionMap.put("status", answerCard.getStatus());
            questionMap.put("wcount", answerCard.getWcount());
            questionMap.put("ucount", answerCard.getUcount());
            questionMap.put("rcount", answerCard.getRcount());
            questionMap.put("qcount", answerCard.getCorrects().length);
            questionMap.put("id", String.valueOf(answerCard.getId()));
        }
        return questionMap;
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

//    /**
//     * 答题卡中分数转换为整型
//     *
//     * @param answerCard
//     */
//    public static void handlerIntScore(AnswerCard answerCard) {
//        answerCard.setScore(new Double(answerCard.getScore()).intValue());
//        if (answerCard instanceof StandardCard) {
//            CardUserMeta cardMeta = ((StandardCard) answerCard).getCardUserMeta();
//            if (cardMeta != null) {
//                //根据版本判断平均分,最高分是否展示小数
//                cardMeta.setAverage(new Double(cardMeta.getAverage()).intValue());
//                cardMeta.setMax(new Double(cardMeta.getMax()).intValue());
//                ((StandardCard) answerCard).setCardUserMeta(cardMeta);
//            }
//            MatchCardUserMeta matchMeta = ((StandardCard) answerCard).getMatchMeta();
//            if (matchMeta != null) {
//                //根据版本判断职位平均分,职位最高分是否展示小数
//                matchMeta.setPositionMax(new Double(matchMeta.getPositionMax()).intValue());
//                matchMeta.setPositionAverage(new Double(matchMeta.getPositionAverage()).intValue());
//                Line line = matchMeta.getScoreLine();
//                if (CollectionUtils.isNotEmpty(line.getSeries())) {
//                    for (LineSeries lineSeries : line.getSeries()) {
//                        List<Integer> list = Lists.newArrayList();
//                        for (Number score : lineSeries.getData()) {
//                            list.add(score.intValue());
//                        }
//                        lineSeries.setData(list);
//                    }
//                }
//            }
//            ((StandardCard) answerCard).setMatchMeta(matchMeta);
//        }
//    }

    public static void handlerLine(Line line, Function<Double, Double> function){
        if (CollectionUtils.isNotEmpty(line.getSeries())) {
            for (LineSeries lineSeries : line.getSeries()) {
                List<Double> list = Lists.newArrayList();
                List<String> strList = Lists.newArrayList();
                for (Number score : lineSeries.getData()) {
                    list.add(function.apply(score.doubleValue()));
                    strList.add(String.valueOf(score));
                }
                lineSeries.setData(list);
                lineSeries.setStrData(strList);
            }
        }

    }
    public static void handlerDoubleScore(AnswerCard answerCard, Function<Double, Double> function) {
        answerCard.setScore(function.apply(answerCard.getScore()));
        answerCard.setScoreStr(String.valueOf(answerCard.getScore()));
        if (answerCard instanceof StandardCard) {
            CardUserMeta cardMeta = ((StandardCard) answerCard).getCardUserMeta();
            if (cardMeta != null) {
                //根据版本判断平均分,最高分是否展示小数
                cardMeta.setAverage(function.apply(cardMeta.getAverage()));
                cardMeta.setMax(function.apply(cardMeta.getMax()));
                //分数转化为字符串
                cardMeta.setAverageStr(String.valueOf(cardMeta.getAverage()));
                cardMeta.setMaxStr(String.valueOf(cardMeta.getMax()));

                ((StandardCard) answerCard).setCardUserMeta(cardMeta);
                logger.info("用户答题信息:{}", cardMeta);
            }
            MatchCardUserMeta matchMeta = ((StandardCard) answerCard).getMatchMeta();
            if (matchMeta != null) {
                //根据版本判断职位平均分,职位最高分是否展示小数
                matchMeta.setPositionMax(function.apply(matchMeta.getPositionMax()));
                matchMeta.setPositionAverage(function.apply(matchMeta.getPositionAverage()));
                //模考分数转化为字符串展示
                matchMeta.setPositionAverageStr(String.valueOf(matchMeta.getPositionAverage()));
                matchMeta.setPositionMaxStr(String.valueOf(matchMeta.getPositionMax()));
                Line line = matchMeta.getScoreLine();
                handlerLine(line,function);
                logger.info("用户模考大赛职位分信息:{}", matchMeta);
            }
            ((StandardCard) answerCard).setMatchMeta(matchMeta);
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

    public static void main(String[] args) {
        double num = 10d / 3;
        System.out.println(num);
        System.out.println(transDouble.apply(num));
        System.out.println(transInt.apply(num));
    }
}
