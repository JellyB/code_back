package com.huatu.tiku.course.spring.conf.aspect.mapParam;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by lijun on 2018/7/9
 */
@AllArgsConstructor
@Getter
public enum TokenType {
    ZTK(1),
    IC(2);
    private int type;
}
