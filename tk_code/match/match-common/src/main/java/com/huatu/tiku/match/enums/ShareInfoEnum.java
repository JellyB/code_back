package com.huatu.tiku.match.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分享类型--与PC项目一致
 * Created by huangqingpeng on 2019/1/11.
 */
public enum ShareInfoEnum{
    ;
    @Getter
    @AllArgsConstructor
    public enum ShareTypeEnum {
        SHARE_QUESTION(1,"分享试题"),
        SHARE_PRACTICE(2,"分享练习结果"),
        SHARE_REPORT(3,"分享报告"),
        SHARE_COURSE(4,"分享课程"),
        SHARE_ARENA_SUMMARY(5,"分享竞技练习战绩"),
        SHARE_ARENA_RECORD(6,"分享竞技练习成绩统计"),
        SHARE_ARENA_TODAYRANK(7,"分享今日排行"),
        SHARE_RED_PACKAGE(9,"分享红包"),
        ;

        private int key;
        private String value;

    }

    @Getter
    @AllArgsConstructor
    public enum ShareReportTypeEnum{
        LINETESTONLY(1,"只分享行测"),
        ESSAYONLY(2,"只分享申论"),
        LINETESTWITHESSAY(3,"行测和申论总成绩单分享"),
        ;

        private int key;
        private String value;
    }
}

