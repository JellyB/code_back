package com.huatu.tiku.interview.entity.result;

import java.io.Serializable;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/12 9:08
 * @Modefied By:
 */
public interface BaseResult extends Serializable {
    Integer code = 20000;

    Integer getCode();
}
