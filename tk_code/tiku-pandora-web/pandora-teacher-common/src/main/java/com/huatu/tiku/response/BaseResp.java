package com.huatu.tiku.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

/**
 * Created by duanxiangchao on 2018/5/4
 */

@JsonInclude(JsonInclude.Include.ALWAYS)
public class BaseResp implements Serializable {

    private static final long serialVersionUID = 1L;

}
