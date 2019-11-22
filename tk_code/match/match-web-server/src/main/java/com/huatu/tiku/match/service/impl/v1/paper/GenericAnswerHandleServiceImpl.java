package com.huatu.tiku.match.service.impl.v1.paper;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.huatu.tiku.match.bo.paper.AnswerResultBo;
import com.huatu.tiku.match.dto.paper.AnswerDTO;
import com.huatu.tiku.match.enums.AnswerCardInfoEnum;
import com.huatu.tiku.match.service.v1.paper.AnswerHandleService;
import com.huatu.tiku.match.service.v1.paper.QuestionService;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2019/1/3
 */
@RequiredArgsConstructor
@Service("GenericAnswerHandleServiceImpl")
public class GenericAnswerHandleServiceImpl implements AnswerHandleService {

    final QuestionService questionService;

    @Override
    public boolean handleQuestionAnswer(int questionId, String answer) {
        if (StringUtils.isBlank(answer)) {
            return false;
        }
        Question question = questionService.findQuestionCacheById(questionId);
        //试题类型不匹配
        if (null == question || !(question instanceof GenericQuestion)) {
            return false;
        }
        GenericQuestion genericQuestion = (GenericQuestion) question;
        if (answer.length() <= 1) {
            return String.valueOf(genericQuestion.getAnswer()).equals(answer);
        }
        return sortAnswer(String.valueOf(genericQuestion.getAnswer())).equals(sortAnswer(answer));
    }

    /**
     * 答案排序
     */
    private static String sortAnswer(String answer) {
        if (StringUtils.isBlank(answer)) {
            return answer;
        }
        String[] answerArray = answer.split(StringUtils.EMPTY);
        Arrays.sort(answerArray);
        return Arrays.asList(answerArray).stream().collect(Collectors.joining(StringUtils.EMPTY));
    }

    @Override
    public List<AnswerResultBo> handleQuestionAnswer(List<AnswerDTO> answerList) {
        if (CollectionUtils.isEmpty(answerList)) {
            return Lists.newArrayList();
        }
        List<AnswerResultBo> answerResultBoList = answerList.stream()
                .map(answerDTO -> {
                    Integer questionId = answerDTO.getQuestionId();
                    AnswerResultBo answerResultBo = new AnswerResultBo();
                    BeanUtils.copyProperties(answerDTO, answerResultBo);
                    //-1 不可答、 0 未答、1 正确、2 错误、 3已作答
                    //是否未答判断
                    if (StringUtils.isBlank(answerDTO.getAnswer()) || answerDTO.getAnswer().equals("0") || answerDTO.getAnswer().equals("null")) {
                        answerResultBo.setCorrect(AnswerCardInfoEnum.Result.UNDO.getCode());
                        return answerResultBo;
                    }
                    //做过的试题再做对错判断
                    boolean handleQuestionAnswerResult = handleQuestionAnswer(questionId, answerDTO.getAnswer());
                    answerResultBo.setCorrect(handleQuestionAnswerResult
                            ? AnswerCardInfoEnum.Result.RIGHT.getCode()
                            : AnswerCardInfoEnum.Result.WRONG.getCode());
                    return answerResultBo;
                })
                .collect(Collectors.toList());
        return answerResultBoList;
    }

    @Override
    public void saveAnswerInfoToAnswerCard(AnswerCard answerCard, List<AnswerResultBo> answerResultBoList, Function<AnswerCard, Double> getSource) {
        if (null == answerCard || !(answerCard instanceof StandardCard)) {
            return;
        }
        StandardCard standardCard = (StandardCard) answerCard;
        Paper paper = standardCard.getPaper();
        final List<Integer> paperQuestionList = paper.getQuestions();

        //答案、是否正确、是否有疑问、耗时
        final String[] answers = standardCard.getAnswers();
        final int[] corrects = standardCard.getCorrects();
        final int[] doubts = standardCard.getDoubts();
        final int[] times = standardCard.getTimes();

        answerResultBoList.forEach(answerResultBo -> {
            int index = paperQuestionList.indexOf(answerResultBo.getQuestionId());
            if (index > -1) {
                answers[index] = answerResultBo.getAnswer();
                corrects[index] = answerResultBo.getCorrect();
                doubts[index] = answerResultBo.getDoubt();
                times[index] = answerResultBo.getExpireTime();
            }
        });
        //耗时、正确数量、错误数量
        int expendTime, rightCountNum, wrongCountNum;
        expendTime = Arrays.stream(times).sum();
        final List<Integer> correctList = Collections.unmodifiableList(Ints.asList(corrects));
        rightCountNum = (int) correctList.stream().filter(correct -> AnswerCardInfoEnum.Result.RIGHT.valueEquals(correct)).count();
        wrongCountNum = (int) correctList.stream().filter(correct -> AnswerCardInfoEnum.Result.WRONG.valueEquals(correct)).count();
        //正确数量、错误数量、未做数量
        standardCard.setRcount(rightCountNum);
        standardCard.setWcount(wrongCountNum);
        standardCard.setUcount(corrects.length - rightCountNum - wrongCountNum);
        //消耗总时间、剩余时间、当前时间
        standardCard.setExpendTime(expendTime);
        buildRemainingTime(standardCard);
        standardCard.setCurrentTime(System.currentTimeMillis());
        //分数
        standardCard.setScore(getSource.apply(answerCard));
        //避免出现 满分却有错题 0分却有正确的题时候的 异常分数
        if (standardCard.getScore() == paper.getScore() && rightCountNum != corrects.length) {
            standardCard.setScore(paper.getScore() - 1);
        }
        if (standardCard.getScore() == NumberUtils.INTEGER_ZERO && rightCountNum != NumberUtils.INTEGER_ZERO) {
            standardCard.setScore(1);
        }
        //计算 lastIndex
        int lastIndex = NumberUtils.max(1,
                correctList.lastIndexOf(AnswerCardInfoEnum.Result.RIGHT.getCode()),
                correctList.lastIndexOf(AnswerCardInfoEnum.Result.WRONG.getCode()),
                correctList.lastIndexOf(AnswerCardInfoEnum.Result.DONE.getCode())
                );
        standardCard.setLastIndex(NumberUtils.min(lastIndex, correctList.size() - 1));
        //更细试卷提交时间
        standardCard.setCreateTime(System.currentTimeMillis());
    }

    /**
     * 构建剩余时间
     */
    private static void buildRemainingTime(StandardCard standardCard) {
        Paper paper = standardCard.getPaper();
        if (paper instanceof EstimatePaper) {
            EstimatePaper estimatePaper = (EstimatePaper) paper;
            long seconds = TimeUnit.MINUTES.toSeconds(System.currentTimeMillis() - estimatePaper.getStartTime());
            standardCard.setRemainingTime((int) NumberUtils.max(0, paper.getTime() - NumberUtils.max(0, seconds)));
        } else {
            standardCard.setRemainingTime(Math.max(NumberUtils.INTEGER_ZERO, paper.getTime() - standardCard.getExpendTime()));
        }
    }

}
