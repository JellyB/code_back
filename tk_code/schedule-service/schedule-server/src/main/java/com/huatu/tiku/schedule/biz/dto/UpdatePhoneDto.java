package com.huatu.tiku.schedule.biz.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

/**
 * @author wangjian
 **/
@Data
public class UpdatePhoneDto implements Serializable{
    private static final long serialVersionUID = 153181768768709942L;

    @NotBlank(message = "验证码不能为空")
    private String code;

    @NotBlank(message = "手机号码不能为空")
    private String phone;

    private Long teacherId;
}
