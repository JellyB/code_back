package com.huatu.tiku.match.enums;

import com.huatu.tiku.match.enums.util.EnumCommon;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by lijun on 2018/10/17
 */
public enum MatchInfoEnum {
    ;

    /**
     * 模考大赛管理后台状态枚举
     */
    @AllArgsConstructor
    @Getter
    public enum BackendStatusEnum implements EnumCommon {
        CREATE(1, "新建"),
        AUDIT_SUCCESS(2, "审核通过"),
        AUDIT_REJECT(3, "审核不通过"),
        DELETE(4, "已被删除"),
        OFFLINE(7, "已下线"),
        DEFAULT(0, "未知");

        private int key;
        private String value;

        @Override
        public EnumCommon getDefault() {
            return DEFAULT;
        }
    }

    /**
     * 模考大赛类型
     */
    @AllArgsConstructor
    @Getter
    public enum FlagEnum implements EnumCommon {
        /**
         * 只有行测报告
         */
        ONLY_TEST(1),

        /**
         * 只有申论报告
         */
        ONLY_ESSAY(2),

        /**
         * 行测申论 报告都有
         */
        TEST_AND_ESSAY(3),

        /**
         * 默认
         */
        DEFAULT(0);

        private int key;

        @Override
        public String getValue() {
            return name();
        }

        @Override
        public EnumCommon getDefault() {
            return DEFAULT;
        }
    }

    /**
     * 模考大赛阶段
     */
    @AllArgsConstructor
    @Getter
    public enum StageEnum implements EnumCommon {

        /**
         * 默认
         */
        DEFAULT(0),
        /**
         * 行测阶段
         */
        TEST(1),

        /**
         * 申论阶段
         */
        ESSAY(2),
        ;
        private int key;

        @Override
        public String getValue() {
            return name();
        }

    }

    /**
     * 报名方式
     */
    @AllArgsConstructor
    @Getter
    public enum EnrollFlagEnum implements EnumCommon{
        /**
         * 选择地区报名
         */
        HAS_AREA(0),

        /**
         * 无地区报名
         */
        NO_AREA(1);

        private int key;

        @Override
        public String getValue() {
            return name();
        }
    }

    /**
     * 交卷方式
     */
    @AllArgsConstructor
    @Getter
    public enum SubmitTypeEnum implements EnumCommon{
        /**
         * 手动交卷
         */
        MANUAL_SUBMIT(0),

        /**
         * 自动交卷
         */
        AUTO_SUBMIT(1),

        /**
         * 未交卷
         */
        NO_SUBMIT(-1);

        private int key;

        @Override
        public String getValue() {
            return name();
        }

        public static SubmitTypeEnum create(int key){
            for (SubmitTypeEnum submitTypeEnum : SubmitTypeEnum.values()) {
                if(submitTypeEnum.getKey() == key){
                    return submitTypeEnum;
                }
            }
            return NO_SUBMIT;
        }
    }

    /**
     * 答题情况
     */
    @AllArgsConstructor
    @Getter
    public enum AnswerStatus implements EnumCommon{
        /**
         * 未进入考试
         */
        NO_JOIN(0),

        /**
         * 未答题
         */
        NO_SUBMIT(1),

        /**
         * 答题
         */
        SUBMIT(2);

        private int key;

        @Override
        public String getValue() {
            return name();
        }
    }

}
