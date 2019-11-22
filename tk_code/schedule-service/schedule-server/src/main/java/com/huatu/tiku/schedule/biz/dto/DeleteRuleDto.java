package com.huatu.tiku.schedule.biz.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author wangjian
 **/
@Data
public class DeleteRuleDto implements Serializable{
    private static final long serialVersionUID = -7510606189499409526L;

    @NotNull(message = "规则ID不能为空")
    private Long id;

    @NotBlank(message = "验证码不能为空")
    private String code;
}
