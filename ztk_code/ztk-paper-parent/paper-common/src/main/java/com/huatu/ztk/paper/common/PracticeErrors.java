package com.huatu.ztk.paper.common;


import com.huatu.ztk.commons.exception.ErrorResult;

/**
 * 练习错误码表
 * Created by shaojieyue
 * Created time 2016-05-31 18:26
 */
public class PracticeErrors {


    /**
     * 重复提交试卷
     */
    public static final ErrorResult REPEAT_SUBMIT_PRACTICE = ErrorResult.create(10031001,"重复提交试卷");

    /**
     * 当用户对同一道题重复提交答案时报的错误
     */
    public static final ErrorResult REPEAT_SUBMIT_ANSWER = ErrorResult.create(10031002,"重复提交答案(当用户对同一道题重复提交答案时报的错误)");


    /**
     * 答案的试题不存在试卷内
     * 用户提交的答案,对应的试题不存在与该试卷
     */
    public static final ErrorResult SUBMIT_ANSWER_QUESTION_NO_EXIST = ErrorResult.create(10031003,"答案的试题不存在试卷内(用户提交的答案,对应的试题不存在于该试卷)");

    /**
     * 每日特训初始化
     */
    public static final ErrorResult DAY_TRAIN_SETTING_NO_INIT = ErrorResult.create(10041004,"请初始化每日特训设置");

    /**
     * 未做答的题,不允许提交答案
     */
    public static final ErrorResult UNDO_QUESTION_CANT_NOT_SUBMIT = ErrorResult.create(10031005,"未做答的题,不允许提交答案");


    /**
     * 题目数量不足
     */
    public static final ErrorResult QUESTION_COUNT_NOT_ENOUGH = ErrorResult.create(10031010,"组卷失败，题量不足");

}
