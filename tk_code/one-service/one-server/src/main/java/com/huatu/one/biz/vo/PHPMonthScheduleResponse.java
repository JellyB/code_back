package com.huatu.one.biz.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * PHP接口返回月课表日期
 *
 * @author geek-s
 * @date 2019-08-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PHPMonthScheduleResponse {
    /**
     * 上课时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date time;
}
