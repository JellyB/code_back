package com.huatu.ztk.knowledge.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/6/11
 * @描述
 */

public enum QuestionMetaFlag {
    ;

    /**
     * 用户最后一次做题对错表示（0对1错
     */
    @AllArgsConstructor
    @Getter
    public enum erroFlag {
        ERROR(1, "做错"),
        right(0, "做对");

        private int key;
        private String value;
    }


}

