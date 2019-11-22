package com.huatu.tiku.position.biz.dto;

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
public class UpdateSpecialtyDto implements Serializable{

    private static final long serialVersionUID = -3265924647508069203L;

//    @NotNull(message = "专业不能为空")
    private Long specialtyId;//专业

}
