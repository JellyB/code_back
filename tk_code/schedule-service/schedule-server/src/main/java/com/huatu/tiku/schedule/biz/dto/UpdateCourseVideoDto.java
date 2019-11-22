package com.huatu.tiku.schedule.biz.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author wangjian
 **/
@Data
public class UpdateCourseVideoDto extends CreateCourseVideoDto {
    private static final long serialVersionUID = 2726941300184493887L;

    @NotNull(message = "录播ID不能为空")
    private Long id;
}
