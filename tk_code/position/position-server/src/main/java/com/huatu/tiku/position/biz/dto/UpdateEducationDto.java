package com.huatu.tiku.position.biz.dto;

import com.huatu.tiku.position.biz.enums.Education;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author wangjian
 **/
@Getter
@Setter
public class UpdateEducationDto implements Serializable{

    private static final long serialVersionUID = -2349039641958317907L;

    @NotNull(message = "学历不能为空")
    private Education education;//学历

}
