package com.huatu.tiku.schedule.biz.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

/**
 * @author wangjian
 **/
@Getter
@Setter
@ToString
public class UpdatePasswordDto implements Serializable{
    private static final long serialVersionUID = 300687405204926823L;

    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String password;
}
