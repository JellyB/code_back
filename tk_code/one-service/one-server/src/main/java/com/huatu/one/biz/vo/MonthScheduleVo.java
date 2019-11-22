package com.huatu.one.biz.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课表
 *
 * @author geek-s
 * @date 2019-08-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthScheduleVo {
    /**
     * 上课时间
     */
    private String value;
}
