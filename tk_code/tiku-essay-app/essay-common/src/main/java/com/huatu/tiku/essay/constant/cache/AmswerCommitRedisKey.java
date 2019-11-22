package com.huatu.tiku.essay.constant.cache;

/**
 * @author zhaoxi
 * @Description: 提交答题卡定时任务锁
 * @date 2018/8/7下午1:52
 */
public class AmswerCommitRedisKey {

    /**
     *
     * @return
     */
    public static String  getUnCommitAnswerLockKey() {
        return "un_commit_answer_lock";
    }

}
