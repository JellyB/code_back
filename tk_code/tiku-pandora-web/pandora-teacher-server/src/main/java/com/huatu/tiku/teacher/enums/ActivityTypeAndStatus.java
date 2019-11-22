package com.huatu.tiku.teacher.enums;

import com.google.common.collect.Lists;
import com.huatu.tiku.constants.teacher.BackendPaperStatus;
import com.huatu.tiku.enums.EnumCommon;
import com.huatu.ztk.paper.common.PaperType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/7/24
 * @描述
 */

public class ActivityTypeAndStatus {

    @AllArgsConstructor
    @Getter
    public enum ActivityTypeEnum implements EnumCommon {
        //活动类型，对照paper服务中已经设定的值
        //未发布，已发布，已下线，已结束
        MATCH(PaperType.MATCH, "模考大赛", Lists.newArrayList(ActivityStatusEnum.getKeys()), false),

        ////未发布，已发布，已下线，已结束
        REGULAR_PAPER(PaperType.CUSTOM_PAPER, "专项模考", Lists.newArrayList(ActivityStatusEnum.getKeys()), false),

        //未发布，已发布，已下线，已结束
        ESTIMATE_PAPER(PaperType.ESTIMATE_PAPER, "精准估分", Lists.newArrayList(ActivityStatusEnum.getKeys()), false),

        //未发布，已发布，已下线
        TRUE_PAPER(PaperType.TRUE_PAPER, "真题演练", Lists.newArrayList(
                ActivityStatusEnum.ACTIVITY_NO_PUBLISH.getKey(),
                ActivityStatusEnum.ACTIVITY_PUBLISH.getKey(),
                ActivityStatusEnum.ACTIVITY_OFFLINE.getKey()), false),

        SMALL_ESTIMATE(PaperType.SMALL_ESTIMATE, "小模考", Lists.newArrayList(ActivityStatusEnum.getKeys()), false),

        FORMATIVE_TEST_ESTIMATE(PaperType.FORMATIVE_TEST_ESTIMATE, "阶段测试", Lists.newArrayList(ActivityStatusEnum.getKeys()), false),
        APPLETS_PAPER(PaperType.APPLETS_PAPER, "小程序", Lists.newArrayList(ActivityStatusEnum.getKeys()), false),

        UN_KNOW(-1, "未知", Lists.newArrayList(), false);

        private int key;
        private String values;
        private List<Integer> status;
        private Boolean questionScoreShowEnableFlag;


        @Override
        public int getKey() {
            return key;
        }

        public String getValue() {
            return values;
        }

        public List<Integer> getStatus() {
            return status;
        }

        public static ActivityTypeEnum create(int type) {
            ActivityTypeEnum[] values = ActivityTypeEnum.values();
            for (ActivityTypeEnum value : values) {
                if (value.getKey() == type) {
                    return value;
                }
            }
            return UN_KNOW;
        }

        public boolean equals(ActivityTypeEnum activityTypeEnum) {
            return activityTypeEnum.getKey() == this.getKey();
        }

        /**
         * 获取所有的key
         *
         * @return
         */
        public static List<Integer> getEnumKeys() {
            List<Integer> collect = Arrays.stream(ActivityTypeEnum.values()).map(ActivityTypeEnum::getKey).collect(Collectors.toList());
            return collect;
        }
    }

    @AllArgsConstructor
    @Getter
    public enum ActivityStatusEnum implements EnumCommon {

        ACTIVITY_NO_PUBLISH(BackendPaperStatus.CREATED, "未发布"),
        ACTIVITY_PUBLISH(BackendPaperStatus.ONLINE, "已发布"),
        ACTIVITY_OFFLINE(BackendPaperStatus.OFFLINE, "已下线"),
        ACTIVITY_END(BackendPaperStatus.END, "已结束");

        private int key;
        private String values;

        @Override
        public int getKey() {
            return key;
        }

        public String getValue() {
            return values;
        }


        public static ActivityStatusEnum create(int type) {
            ActivityStatusEnum[] values = ActivityStatusEnum.values();
            for (ActivityStatusEnum value : values) {
                if (value.getKey() == type) {
                    return value;
                }
            }

            return ACTIVITY_NO_PUBLISH;
        }

        public static List<Integer> getKeys() {
            ActivityStatusEnum[] values = ActivityStatusEnum.values();
            List<Integer> collect = Arrays.stream(values).map(activityStatus -> activityStatus.getKey()).collect(Collectors.toList());
            return collect;
        }
    }
}