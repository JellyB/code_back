package com.huatu.tiku.match.enums;

import com.huatu.tiku.match.enums.util.EnumCommon;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 模考大赛 数据状态（模考各个阶段）
 *
 * @author biguodong
 * Create time 2018-10-16 下午1:21
 */
@AllArgsConstructor
@Getter
public enum MatchStatusEnum implements EnumCommon {
    DEFAULT(0, "未知状态"),
    UN_ENROLL(1, "未报名"),
    ENROLL(2, "已报名"),
    START_UNAVAILABLE(3, "开始考试-置灰-不可用"),
    START_AVAILABLE(4, "开始考试"),
    MATCH_UNAVAILABLE(5, "无法考试"),
    REPORT_AVAILABLE(6, "可查看报告"),
    REPORT_UNAVAILABLE(7, "未出报告"),
    NOT_SUBMIT(8, "未交卷，可以继续做题"),
    PASS_UP_ENROLL(9, "未报名且错过报名"),
    WHITE_PRE_LOOK(10,"白名单预览状态");

    private int key;
    private String value;

    /**
     * 默认状态值
     */
    @Override
    public EnumCommon getDefault() {
        return DEFAULT;
    }

    /**
     * 判断一个 key 是否存在 列举的枚举中
     */
    public static boolean in(int key, MatchStatusEnum... statusEnums) {
        MatchStatusEnum[] matchStatusEnums = statusEnums.clone();
        return (Arrays.stream(matchStatusEnums).anyMatch(k -> k.valueEquals(key)));
    }

    /**
     * 获取 模考大赛 数据状态
     *
     * @param isEnroll        用户是否已报名
     * @param isStartPractice 用户是否已经开始答题
     * @param isSubmit        用户是否已经提交试卷
     * @param isCancel        用户答题卡是否已经被处理
     * @param startTime       开始时间
     * @param endTime         结束时间
     * @return 模考大赛数据状态
     */
    public static MatchStatusEnum getMatchStatusForTest(
            boolean isEnroll, boolean isStartPractice, boolean isSubmit, boolean isCancel,
            long startTime, long endTime) {
        long currentTime = System.currentTimeMillis();
        if (isEnroll) {
            //用户已经报名
            if (!isStartPractice) {
                //还未开始答题
                return getMatchStatusForTest(startTime, endTime);
            } else {
                if (isSubmit) {//已提交
                    if (isCancel) {
                        //答题卡已经被处理
                        return REPORT_AVAILABLE;
                    }
                    //答题卡未被处理
                    return REPORT_UNAVAILABLE;

                } else if (currentTime < endTime) {
                    //未提交 未结束
                    return NOT_SUBMIT;
                } else {
                    //未提交 已结束
                    return REPORT_UNAVAILABLE;
                }
            }
        } else {
            //用户未报名
            if (currentTime - startTime >= TimeUnit.MINUTES.toMillis(30) || currentTime > endTime) {
                //超过开始 三十分钟、未结束
                return MatchStatusEnum.PASS_UP_ENROLL;
            }
            return UN_ENROLL;
        }
    }

    /**
     * 当前时间于一个模考大赛的考试状态
     */
    public static MatchStatusEnum getMatchStatusForTest(long startTime, long endTime) {
        long currentTime = System.currentTimeMillis();
        //还未开始答题
        if (startTime - currentTime >= TimeUnit.HOURS.toMillis(1)) {
            //当前时间 距开始大于一个小时
            return ENROLL;
        } else if (startTime - currentTime >= TimeUnit.MINUTES.toMillis(5)) {
            //当前时间 距开始小于一个小时,大于5分钟
            return START_UNAVAILABLE;
        } else if (currentTime - startTime < TimeUnit.MINUTES.toMillis(30) && currentTime < endTime) {
            //当前时间 距开始小于5分钟,且距离开始后小于30分钟
            return START_AVAILABLE;
        } else {
            //当前时间 距已经开始30分钟
            return MATCH_UNAVAILABLE;
        }
    }
}
