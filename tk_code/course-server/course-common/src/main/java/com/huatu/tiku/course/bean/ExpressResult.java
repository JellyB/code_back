package com.huatu.tiku.course.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 物流详情返回值bean
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class ExpressResult {
    private int code;
    private String msg;
    private ExpressData data;
}
