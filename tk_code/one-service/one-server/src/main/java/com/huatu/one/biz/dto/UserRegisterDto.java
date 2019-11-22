package com.huatu.one.biz.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;

/**
 * 用户注册
 *
 * @author geek-s
 * @date 2019-08-28
 */
@Data
public class UserRegisterDto {

    /**
     * 姓名
     */
    @NotEmpty(message = "姓名不能为空")
    private String username;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[0-9]{10}$", message = "手机号格式错误")
    private String mobile;
}
