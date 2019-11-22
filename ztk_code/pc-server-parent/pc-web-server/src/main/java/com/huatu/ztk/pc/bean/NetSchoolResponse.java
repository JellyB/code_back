package com.huatu.ztk.pc.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 网校接口返回值 bean
 * Created by linkang on 11/25/16.
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class NetSchoolResponse implements Serializable{
    private static final long serialVersionUID = 1L;

    private int code;
    private String msg;
    private Object data;
}
