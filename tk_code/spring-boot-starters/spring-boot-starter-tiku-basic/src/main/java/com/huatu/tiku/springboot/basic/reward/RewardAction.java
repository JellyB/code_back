package com.huatu.tiku.springboot.basic.reward;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.ToString;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * @author hanchao
 * @date 2017/10/12 15:48
 */
@ToString
public class RewardAction implements Serializable {
    private static final long serialVersionUID = 1L;
    private ActionType action;//动作名称
    private Strategy strategy;//赠送策略
    private int timesLimit;//最多多少次,策略优先
    private int gold;//赠送金币数量,0为不固定
    private int experience;//经验值,0为不固定

    public ActionType getAction() {
        return action;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public int getTimesLimit() {
        return timesLimit;
    }

    public int getGold() {
        return gold;
    }

    public int getExperience() {
        return experience;
    }

    public RewardAction(ActionType action, Strategy strategy, int timesLimit, int gold, int experience) {
        this.action = action;
        this.strategy = strategy;
        this.timesLimit = timesLimit;
        this.gold = gold;
        this.experience = experience;
    }

    public RewardAction() {

    }

    public enum Strategy {
        NONE,//不限制
        ONCE,
        DAILY,//每日赠送
        WEEKELY,//
        MONTHLY,
        YEARLY;
    }

    public enum ActionType {
        REGISTER("注册成功"),//注册
        ATTENDANCE("签到成功"),//签到
        MATCH_ENROLL("模考大赛报名成功"),//模考报名
        MATCH_ENTER("模考大赛交卷成功"),//参加
        MATCH_PRIZE("模考大赛-进入排名"),//获得前五名
        ARENA_WIN("竞技赛场每日首胜"),//竞技场每日首胜
        TRAIN_DAILY("完成每日特训"),//每日特训
        TRAIN_SPECIAL("专项练习交卷成功"),//专项练习
        TRAIN_INTELLIGENCE("智能刷题交卷成功"),//智能练习
        TRAIN_MISTAKE("错题重练交卷成功"),//错题重练
        ANSWER_CORRECTION("试题纠错采纳"),//试题纠错,
        COURSE_BUY("购课赠送"),
        WATCH_FREE("学习免费课"),//观看免费课
        WATCH_PAY("学习付费课"),//观看付费课
        EVALUATE_AFTER("学习课程后完成评价"),//观看完成后评价
        EVALUATE("完成课程评价"),//评价课程
        SHARE("分享成功"),//分享
        CHARGE("充值"),//充值
        ACTIVTY(""),//运营活动
        SL_CORR_SINGLE("申论单题批改"),
        SL_CORR_STANDARD("申论标准答案批改"),
        SL_CORR_ARGUMENT("申论文章写作批改"),
        SL_CORR_SET("申论套题批改"),
        COURSE_PRACTICE_RIGHT("随堂练赠送金币"),
        COURSE_AFTER_PRACTICE_RIGHT("课后练习赠送金币"),
        ESSAY_CORRECT_FEEDBACK("批改评价成功"),
        APP_STORE_EVALUTION("appStore评价");

        private String bizName;

        ActionType(String bizName) {
            this.bizName = bizName;
        }

        public String getBizName() {
            return bizName;
        }
    }


    public static void main(String[] args) throws IOException {
        List<RewardAction> list = Lists.newArrayList();
        list.add(new RewardAction(ActionType.REGISTER, Strategy.ONCE, 1, 50, 50));
        list.add(new RewardAction(ActionType.ATTENDANCE, Strategy.DAILY, 1, 0, 0));
        list.add(new RewardAction(ActionType.MATCH_ENROLL, Strategy.WEEKELY, 1, 10, 10));
        list.add(new RewardAction(ActionType.MATCH_ENTER, Strategy.WEEKELY, 1, 20, 20));
        list.add(new RewardAction(ActionType.MATCH_PRIZE, Strategy.WEEKELY, 1, 50, 50));
        list.add(new RewardAction(ActionType.ARENA_WIN, Strategy.DAILY, 1, 20, 20));
        list.add(new RewardAction(ActionType.TRAIN_DAILY, Strategy.DAILY, 1, 20, 20));
        list.add(new RewardAction(ActionType.TRAIN_SPECIAL, Strategy.DAILY, 1, 10, 10));
        list.add(new RewardAction(ActionType.TRAIN_INTELLIGENCE, Strategy.DAILY, 1, 10, 10));
        list.add(new RewardAction(ActionType.TRAIN_MISTAKE, Strategy.DAILY, 1, 10, 10));
        list.add(new RewardAction(ActionType.ANSWER_CORRECTION, Strategy.WEEKELY, 5, 10, 10));
        list.add(new RewardAction(ActionType.COURSE_BUY, Strategy.NONE, 0, 0, 0));
        list.add(new RewardAction(ActionType.WATCH_FREE, Strategy.DAILY, 1, 10, 10));
        list.add(new RewardAction(ActionType.WATCH_PAY, Strategy.DAILY, 1, 20, 20));
        list.add(new RewardAction(ActionType.EVALUATE_AFTER, Strategy.DAILY, 1, 30, 30));
        list.add(new RewardAction(ActionType.EVALUATE, Strategy.DAILY, 1, 10, 10));
        list.add(new RewardAction(ActionType.SHARE, Strategy.DAILY, 2, 10, 10));
        list.add(new RewardAction(ActionType.CHARGE, Strategy.NONE, 0, 0, 0));
        list.add(new RewardAction(ActionType.ACTIVTY, Strategy.NONE, 0, 0, 0));
        list.add(new RewardAction(ActionType.SL_CORR_SINGLE, Strategy.DAILY, 1, 10, 10));
        list.add(new RewardAction(ActionType.SL_CORR_SET, Strategy.DAILY, 1, 20, 20));
        list.add(new RewardAction(ActionType.SL_CORR_STANDARD, Strategy.DAILY, 1, 10, 10));
        list.add(new RewardAction(ActionType.SL_CORR_ARGUMENT, Strategy.DAILY, 1, 10, 10));
        list.add(new RewardAction(ActionType.ESSAY_CORRECT_FEEDBACK, Strategy.NONE, 1, 10, 1));

        System.out.println(JSON.toJSONString(list));
    }
}
