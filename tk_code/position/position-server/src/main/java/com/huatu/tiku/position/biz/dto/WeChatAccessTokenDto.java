package com.huatu.tiku.position.biz.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author wangjian
 **/
@Getter
@Setter
public class WeChatAccessTokenDto implements Serializable {

    private static final long serialVersionUID = 5446943680531765785L;

    private String access_token;//获取到的凭证

    private String expires_in;//凭证有效时间，单位：秒

    private String errcode;//错误码

    private String errmsg;//错误信息
}
