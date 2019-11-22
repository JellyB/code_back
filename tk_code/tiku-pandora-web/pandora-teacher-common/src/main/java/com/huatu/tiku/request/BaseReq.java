package com.huatu.tiku.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by duanxiangchao on 2018/5/4
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseReq implements Serializable {

    private static final long serialVersionUID = 1L;


}
