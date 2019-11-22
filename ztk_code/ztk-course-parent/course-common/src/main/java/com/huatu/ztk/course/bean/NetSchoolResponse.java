package com.huatu.ztk.course.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 网校接口返回值 bean
 * Created by linkang on 11/25/16.
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class NetSchoolResponse {
    private int code;
    private String msg;
    private Object data;
}
