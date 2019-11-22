package com.huatu.tiku.position.biz.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author wangjian
 **/
@Getter
@Setter
public class WxDecryptDto implements Serializable{

    private static final long serialVersionUID = 7144453458787963634L;

    private String phoneNumber;
    private String purePhoneNumber;
    private String countryCode;
    private List watermark;
}
