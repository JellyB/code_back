package com.huatu.tiku.match.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-17 下午1:24
 **/

@Getter
@AllArgsConstructor
public enum  EstimateStatusEnum {

    TRUE_STATUS(1),
    FALSE_STATUS(0);

    private int value;
}
