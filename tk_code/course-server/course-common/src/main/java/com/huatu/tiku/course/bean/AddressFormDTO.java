package com.huatu.tiku.course.bean;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author hanchao
 * @date 2017/9/19 13:44
 */
@Data
public class AddressFormDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotBlank
    private String address;
    @NotBlank
    private String area;
    private int areaId;
    private String city;
    private int cityId;
    private String consignee;
    @Min(0)
    @Max(1)
    private int isDefault;
    @NotNull
    private String phone;
    private String province;
    private int provinceId;
}
