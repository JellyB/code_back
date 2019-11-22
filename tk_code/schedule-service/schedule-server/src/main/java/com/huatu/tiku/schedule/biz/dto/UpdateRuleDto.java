package com.huatu.tiku.schedule.biz.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wangjian
 **/
@Data
public class UpdateRuleDto extends CreatRuleDto implements Serializable {
    private static final long serialVersionUID = -6123932639523590093L;

    private Long id;

}
