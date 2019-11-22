package com.huatu.ztk.report.common;

import com.huatu.ztk.commons.SubjectType;

/**
 * redis keys 列表
 * Created by shaojieyue
 * Created time 2016-05-28 21:00
 */
public class RedisReportKeys {

    /**
     * 用户预测分排名 key
     * @param area
     * @param subject
     * @return
     */
    public static final String getUserScoreZsetKey(int area, int subject){
        //此处这么判断是为了兼容老的key
        //因为之前并没有加入subject
        if (subject == SubjectType.GWY_XINGCE) {
           return new StringBuilder("user_pre_score_")
                    .append(area)
                    .toString();
        }

        return new StringBuilder("user_pre_score_")
                .append(area)
                .append("_")
                .append(subject)
                .toString();
    }

    /**
     * 每天训练 list key
     * 暂时不考虑科目
     */
    public static final String getUserDayPracticesIdListKey(long userId,int subject){

        //此处这么判断是为了兼容老的key
        //因为之前并没有加入subject
        if (subject == SubjectType.GWY_XINGCE) {
            return new StringBuilder("user_day_practice_")
                    .append(userId)
                    .toString();
        }

        return new StringBuilder("user_day_practice_")
                .append(userId)
                .append("_")
                .append(subject)
                .toString();
    }

}
