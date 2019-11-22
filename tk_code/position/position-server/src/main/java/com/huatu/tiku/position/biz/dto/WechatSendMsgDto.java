package com.huatu.tiku.position.biz.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author wangjian
 **/
@Getter
@Setter
public class WechatSendMsgDto implements Serializable{

    private static final long serialVersionUID = 4018026382160579812L;

    private String errcode;
    private String errmsg;
    private String msgid;
}
