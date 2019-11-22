package com.huatu.ztk.paper.service.v4.impl;

import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.knowledge.util.GeneticAlgorithmUtil;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.question.common.QuestionCorrectType;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 做分数计算逻辑处理
 * Created by huangqingpeng on 2019/2/21.
 */
public class ComputeScoreUtil {

    private static final Logger logger = LoggerFactory.getLogger(ComputeScoreUtil.class);


    /**
     * 计算得分(招警机考)
     * <p>
     * 分值计算分为6部分如下：
     * 1、知觉：答对则得分，答错倒扣分，不答不得分。
     * 分值：60道题，每题0.2分，总分12分。
     * 2、常识：本部分20道题，每题0.5分，总分10分。
     * 3、言语：本部分40道题，每题0.6分，总分18分。
     * 4、数量：本部分10道题，每题1.2分，总分12分。
     * 5、判断：本部分40道题，每题0.7分，总分28分
     * 6、资料：本部分20道题，每题1.0分，总分20分
     *
     * @param answerCard
     * @return
     */
    public static double computeScoreCop(AnswerCard answerCard) {

        int totalScore = 100;
        double examScore = 0D;
        List<Module> modules = new LinkedList<>();
        if (answerCard instanceof StandardCard) {
            Paper paper = ((StandardCard) answerCard).getPaper();

            //试卷分数
            int paperScore = paper.getScore();
            if (paperScore >= 100) {
                totalScore = paperScore;
            }
            modules = paper.getModules();
        }
        int[] corrects = answerCard.getCorrects();
        List<Integer> correctList = new ArrayList<>(corrects.length);
        for (int c : corrects) {
            correctList.add(new Integer(c));
        }

        //按模块计算分数
        if (CollectionUtils.isNotEmpty(modules)) {
            int startIndex = 0;
            int endIndex = 0;
            //按模块取出答对的题目个数和打错的题目个数
            int moduleIndex = 0;
            for (Module module : modules) {
                double mScore = 0D;
                double rcount = 0;
                double wcount = 0;
                endIndex += module.getQcount();
                List<Integer> subList = correctList.subList(startIndex, endIndex);
                //遍历输出错题个数，和答对的题目个数

                for (Integer correct : subList) {
                    if (correct.equals(QuestionCorrectType.RIGHT)) {
                        rcount++;
                    } else if (correct.equals(QuestionCorrectType.WRONG)) {
                        wcount++;
                    }
                }
                //计算模块得分
                switch (moduleIndex) {
                    case 0:
                        mScore = 0.2 * rcount - 0.2 * wcount;
                        break;
                    case 1:
                        mScore = 0.5 * rcount;
                        break;
                    case 2:
                        mScore = 0.6 * rcount;

                        break;
                    case 3:
                        mScore = 1.2 * rcount;
                        break;
                    case 4:
                        mScore = 0.7 * rcount;
                        break;
                    case 5:
                        mScore = 1.0 * rcount;
                        break;
                }
                examScore += (mScore > 0 ? mScore : 0);
                moduleIndex++;
                startIndex += module.getQcount();
            }

        }
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        if (examScore != 0) {
            return Double.valueOf(decimalFormat.format(examScore));
        }
        return examScore;
    }


    /**
     * 计算得分(正常计算规则)
     * 判断试卷的评分规则，paper.scoreFlag=0或者null，使用平均分计算false
     * paper.scoreFlag=1,使用试题单个的分数true
     *
     * @param answerCard
     * @return
     */
    public static double computeScore(AnswerCard answerCard) {

        boolean scoreFlag = getScoreFlag(answerCard);       //false使用平均分计算， true使用单个试题分数计算
        if (!scoreFlag) {
            return computeScoreByAverage(answerCard);
        } else {
            return computeScoreBySingleScore(answerCard);
        }

    }

    /**
     * 根据单个分数计算总分
     *
     * @param answerCard
     * @return
     */
    public static double computeScoreBySingleScore(AnswerCard answerCard) {
        if (answerCard instanceof StandardCard) {
            List<Double> scores = ((StandardCard) answerCard).getPaper().getScores();
            int[] corrects = answerCard.getCorrects();
            if (CollectionUtils.isNotEmpty(scores) &&
                    scores.size() == corrects.length) {
                double score = 0D;
                for (int i = 0; i < corrects.length; i++) {
                    if (corrects[i] == 1) {
                        score += scores.get(i);
                    }
                }
                return score;
            }
        }
        return computeScoreByAverage(answerCard);
    }

    /**
     * 根据平均分计算总分
     *
     * @param answerCard
     * @return
     */
    public static double computeScoreByAverage(AnswerCard answerCard) {

        int totalScore = 100;
        if (answerCard instanceof StandardCard) {
            //试卷分数
            int paperScore = ((StandardCard) answerCard).getPaper().getScore();
            if (paperScore > 0) {
                totalScore = paperScore;
            }
        }

        final int wcount = answerCard.getWcount();
        final int rcount = answerCard.getRcount();
        final int ucount = answerCard.getUcount();
        int allCount = wcount + rcount + ucount;
        if (allCount == 0) {//防止被0除
            return 0;
        }
        //正确的数量 * 总分 /总试题数目
        //double score = rcount * totalScore / allCount;
        double mulTotalScore = rcount * totalScore;
        BigDecimal bigScore = new BigDecimal(mulTotalScore);
        BigDecimal bigCount = new BigDecimal(allCount);
        double mScore = bigScore.divide(bigCount, 1, BigDecimal.ROUND_HALF_UP).doubleValue();
        logger.info("平均分计算规则,答题卡是:{},分数是:{}", answerCard.getId(), mScore);
        return mScore;
    }

    public static boolean getScoreFlag(AnswerCard answerCard) {
        if (answerCard instanceof StandardCard) {
            Paper paper = ((StandardCard) answerCard).getPaper();
            if (null != paper &&
                    null != paper.getScoreFlag() &&
                    paper.getScoreFlag() > 0) {
                return true;
            }
        }
        return false;
    }
}
