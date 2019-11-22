package com.huatu.tiku.match.enums;

import com.huatu.tiku.match.enums.util.EnumCommon;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by lijun on 2018/10/19
 */
public enum PaperInfoEnum {
    ;

    @AllArgsConstructor
    @Getter
    public enum PaperTypeEnum implements EnumCommon {

        DEFAULT(0),
        /**
         * 真题卷
         */
        TRUE_PAPER(1),

        /**
         * 万人模考
         */
        CUSTOM_PAPER(2),

        /**
         * 作业试卷,
         */
        HOMEWORK_PAPER(3),

        /**
         * 定期模考
         */
        REGULAR_PAPER(4),

        /**
         * 估分试卷
         */
        ESTIMATE_PAPER(8),

        /**
         * 模考大赛
         */
        MATCH(9),

        /**
         * 往期模考(虚拟的试卷类型，数据库中不会出现该类型的试卷，但是如果模考大赛作为往期模考被使用时，他的答题卡类型便是这个)
         */
        MATCH_AFTER(14);

        private Integer code;


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
