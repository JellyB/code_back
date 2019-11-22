package com.huatu.ztk.knowledge.servicePandora.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by lijun on 2018/8/22
 */
public enum SubjectInfoEnum {
    ;

    @AllArgsConstructor
    @Getter
    public enum LEVEL {
        CATEGORY(1),
        SUBJECT(2);
        private Integer level;
    }

}
