package com.huatu.tiku.match.service.v1.reward;

/**
 * Created by huangqingpeng on 2019/2/28.
 */
public interface PaperRewardService {

    /**
     * 报名加积分队列消息推送
     * @param userId
     * @param uname
     * @param paperId
     */
    void sendEnrollMsg(long userId, String uname, int paperId);


    /**
     * 模考大赛交卷加积分队列消息推送
     * @param userId
     * @param uname
     * @param practiceId
     */
    void sendMatchSubmitMsg(long userId, String uname, long practiceId);
}
