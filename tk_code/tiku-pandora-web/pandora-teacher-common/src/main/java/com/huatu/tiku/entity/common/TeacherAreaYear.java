package com.huatu.tiku.entity.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Created by huangqp on 2018\5\9 0009.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TeacherAreaYear {
    @NotNull(message = "地区不能为空")
    private Long areaId;
    @NotNull(message = "年份不能为空")
    private Integer year;
}
