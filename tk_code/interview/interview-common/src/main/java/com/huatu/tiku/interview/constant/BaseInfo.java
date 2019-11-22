package com.huatu.tiku.interview.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * 试卷信息-考题信息 系统常量信息
 * Created by junli on 2018/4/11.
 */
public enum BaseInfo {
    ;

    /**
     * 试卷/试题 类型
     */
    @AllArgsConstructor
    @Getter
    public enum PAPER_TYPE {
        //1单选 2多选 3排序 4简答
        RADIO(1, "单选"),
        MULTI_SELECT(2, "多选"),
        SORT(3, "排序"),
        ANSWER(4, "简答");

        private int type;
        private String name;

        /**
         * 判断是否合法
         *
         * @param code 需要校验类型
         * @return 合法 返回 true
         */
        public static boolean isIllegal(int code) {
            return Stream.of(PAPER_TYPE.values()).anyMatch((value) -> value.getType() == code);
        }

        /**
         * 判断当前类型是否需要 进行答案信息统计
         * @param code
         * @return
         */
        public static boolean choiceMeta(int code){
            return code == RADIO.getType()
                    || code == MULTI_SELECT.getType();
        }

    }

    /**
     * 试卷状态
     */
    @AllArgsConstructor
    @Getter
    public enum PAPER_STATUS {
        PUSHED(-1),
        UN_PUSHED(1);
        private int state;
    }

}
