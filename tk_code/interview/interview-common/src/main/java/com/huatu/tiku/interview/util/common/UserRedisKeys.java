package com.huatu.tiku.interview.util.common;

import java.util.Calendar;

/**
 * 用户所有redis key
 * Created by shaojieyue
 * Created time 2016-06-03 17:08
 */
public class UserRedisKeys {

    /**
     * 用户验证码 redis key
     */
    public static final String CAPTCHA_MOBILE = "captcha_%s";

    /**
     * 用户查看活动中心key
     */
    public static final String USER_ACT_READ = "act_read_%s";

    /**
     *发送验证码标记 key
     */
    public static final String USER_CAPTCHA_MARK = "captcha_mark_%s";

    /**
     * 拒绝的手机号
     */
    public static final String REJECT_MOBILES = "reject_mobiles";

    /**
     * 拒绝的ip
     */
    public static final String REJECT_IPS = "reject_ips";

    /**
     *记录用户设置id的zset
     * @param userId
     * @return
     */
    public static final String getLastConfigIdSet(long userId) {
        return "last_config_id_set_" + userId;
    }

    /**
     * 用户查看的消息id set
     * @param userId
     * @param catgory
     * @return
     */
    public static final String getUserMsgSetKey(long userId, int catgory) {
        return new StringBuilder("msg_read_set_")
                .append(userId).append("_")
                .append(catgory).toString();
    }

    /**
     * 每日积分
     * hash结构
     *
     * @param userId
     * @return
     */
    public static String getDayRewardKey(long userId) {
        int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        return new StringBuilder("reward_day_")
                .append(userId).append("_")
                .append(day)
                .toString();
    }

    /**
     * 每周积分
     * hash结构
     *
     * @param userId
     * @return
     */
    public static String getWeekRewardKey(long userId) {
        int week = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
        return new StringBuilder("reward_week_")
                .append(userId).append("_")
                .append(week)
                .toString();
    }
}
