package com.huatu.tiku.schedule.biz.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author wangjian
 **/
@Data
public class UpdateModuleDto implements Serializable{

    private static final long serialVersionUID = -7510128223233475614L;

    @NotNull(message = "courseLiveTeacherId不能为空")
    private Long courseLiveTeacherId;

    /**
     * 模块id
     */
    private Long moduleId;
}
