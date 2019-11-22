package com.huatu.ztk.backend.teacher.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: xuhuiqiang
 * Time: 2017-05-19  14:07 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class UrlBean {
    private String url;
}
