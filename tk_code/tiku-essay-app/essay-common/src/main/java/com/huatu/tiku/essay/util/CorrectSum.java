package com.huatu.tiku.essay.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * @author jbzm
 * @date Create on 2018/2/26 13:50
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorrectSum<T>  {
    T result;
    int next;
    long total;
    long totalPage;
    /**
     * 批改总次数
     */
    private Integer correctSum;
}
