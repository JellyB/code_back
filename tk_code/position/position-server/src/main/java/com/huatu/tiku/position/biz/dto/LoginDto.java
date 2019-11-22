package com.huatu.tiku.position.biz.dto;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;

/**登录
 * @author wangjian
 **/
@Getter
@Setter
public class LoginDto implements Serializable{

    private static final long serialVersionUID = -8898804911485629941L;

    @NotEmpty(message = "电话不能为空")
    private String phone;

    @NotEmpty(message = "验证码不能为空")
    private String code;

//    @NotEmpty(message = "报考地点不能为空")
//    private String openId;

}
