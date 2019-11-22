package com.huatu.ztk.backend.question.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: xuhuiqiang
 * Time: 2017-06-19  10:35 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class RefuseInfo {
    private int id;//id
    private String description;//意见
}
