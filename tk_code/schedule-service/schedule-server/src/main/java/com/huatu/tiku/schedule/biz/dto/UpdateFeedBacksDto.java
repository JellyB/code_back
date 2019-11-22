package com.huatu.tiku.schedule.biz.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author wangjian
 **/
@Getter
@Setter
public class UpdateFeedBacksDto implements Serializable{

    private static final long serialVersionUID = -2501413605237467433L;

    @NotNull(message = "id不能为空")
    private List<Long> ids;

    @NotNull(message = "状态不能为空")
    private Boolean status;
}
