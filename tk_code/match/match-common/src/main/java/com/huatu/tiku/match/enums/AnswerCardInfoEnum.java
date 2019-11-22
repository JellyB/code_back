package com.huatu.tiku.match.enums;

import com.huatu.tiku.match.enums.util.EnumCommon;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 答题卡信息
 * Created by lijun on 2018/10/31
 */
public enum AnswerCardInfoEnum {
    ;

    @AllArgsConstructor
    @Getter
    public enum Status implements EnumCommon {
        CREATE(1),
        UNDONE(2),
        FINISH(3),
        DELETED(4);
        private int code;


        @Override
        public int getKey() {
            return code;
        }

        @Override
        public String getValue() {
            return name();
        }
    }

    @AllArgsConstructor
    @Getter
    public enum Result implements EnumCommon {
        //-1不可作答
        UNABLEDO(-1),
        //未做
        UNDO(0),
        //正确
        RIGHT(1),
        //错误
        WRONG(2),
        //已作答(只保存答案，不做评分的试题，或者评分失败的试题)
        DONE(3);
        private int code;


        @Override
        public int getKey() {
            return code;
        }

        @Override
        public String getValue() {
            return name();
        }


    }

    @AllArgsConstructor
    @Getter
    public enum TypeEnum implements EnumCommon{
        /**
         * 默认类型
         */
        ANY_PAPER(0),
        /**
         * 智能出题 快速练习
         */
        SMART_PAPER(1),
        /**
         * 专项练习
         */
        CUSTOMIZE_PAPER(2),
        /**
         * 真题
         */
        TRUE_PAPER(3),
        /**
         * 模拟题,旧版的精准估分type
         */
        MOCK_PAPER(4),
        /**
         * 竞技练习
         */
        ARENA_PAPER(5),
        /**
         * 错题练习
         */
        WRONG_PAPER(6),
        /**
         * 每日训练
         */
        DAY_TRAIN(7),
        /**
         * 收藏练习
         */
        COLLECT_TRAIN(8),
        /**
         * 模考
         */
        SIMULATE(9),
        /**
         * 模考大赛
         */
        MATCH(12),
        /**
         * 估分
         */
        ESTIMATE(13),
        /**
         * 往期模考
         */
        MATCH_AFTER(14),
        ;

        private int code;

        @Override
        public int getKey() {
            return code;
        }

        @Override
        public String getValue() {
            return name();
        }
    }
}
