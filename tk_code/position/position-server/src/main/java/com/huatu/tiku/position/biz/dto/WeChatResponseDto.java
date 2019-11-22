package com.huatu.tiku.position.biz.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author wangjian
 **/
@Getter
@Setter
public class WeChatResponseDto implements Serializable{

    private static final long serialVersionUID = 2019234646855781777L;

    private String openid;

    private String session_key;

    private String unionid;

    private String errcode;

    private String errmsg;
}
