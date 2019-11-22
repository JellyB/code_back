package com.huatu.tiku.schedule.biz.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author wangjian
 **/
@Data
public class CourseVideoCancelDto implements Serializable {

    private static final long serialVersionUID = 5269269412209509384L;

    @NotNull(message = "ID不能为空")
    private Long id;

    private String reason;
}
