package com.huatu.tiku.springboot.basic.reward.event;

import com.huatu.tiku.springboot.basic.reward.RewardAction;
import lombok.Data;
import org.springframework.context.ApplicationEvent;

/**
 * 完成任务事件
 * @author hanchao
 * @date 2017/10/13 13:26
 */
@Data
public class RewardActionEvent extends ApplicationEvent {
    private RewardAction.ActionType action;//动作名称,必填
    private String bizId;//业务id，可不填，不填的情况下默认使用随机的md5，只能排除消息的重复，无法排除事件重复
    private int gold;//赠送金币数量,0为不固定，推荐不填，不填默认使用动作定义中的值
    private int experience;//经验值,0为不固定，推荐不填，不填默认使用动作定义中的值
    private int uid;//必填，用户id
    private String uname;//必填，用户名

    public RewardActionEvent(Object source) {
        super(source);
    }

    public RewardAction.ActionType getAction() {
        return action;
    }

    public RewardActionEvent setAction(RewardAction.ActionType action) {
        this.action = action;
        return this;
    }

    public String getBizId() {
        return bizId;
    }

    public RewardActionEvent setBizId(String bizId) {
        this.bizId = bizId;
        return this;
    }

    public int getGold() {
        return gold;
    }

    public RewardActionEvent setGold(int gold) {
        this.gold = gold;
        return this;
    }

    public int getExperience() {
        return experience;
    }

    public RewardActionEvent setExperience(int experience) {
        this.experience = experience;
        return this;
    }

    public int getUid() {
        return uid;
    }

    public RewardActionEvent setUid(int uid) {
        this.uid = uid;
        return this;
    }

    public String getUname() {
        return uname;
    }

    public RewardActionEvent setUname(String uname) {
        this.uname = uname;
        return this;
    }

}
